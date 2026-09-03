package hi.network;

import hi.CreateTheAirWarsMod;
import hi.entity.FpvDroneEntity;
import hi.item.DroneControllerItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record FpvDroneControlPacket(UUID droneId, float mouseYaw, float mousePitch, float yawInput, float pitchInput, float throttleDelta) implements CustomPacketPayload {
    public static final Type<FpvDroneControlPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "fpv_drone_control"));
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, FpvDroneControlPacket> STREAM_CODEC =
        net.minecraft.network.codec.StreamCodec.of((buf, msg) -> msg.write(buf), FpvDroneControlPacket::new);

    public FpvDroneControlPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(droneId);
        buf.writeFloat(mouseYaw);
        buf.writeFloat(mousePitch);
        buf.writeFloat(yawInput);
        buf.writeFloat(pitchInput);
        buf.writeFloat(throttleDelta);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(FpvDroneControlPacket message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            ItemStack controller = findController(player);
            if (!DroneControllerItem.isController(controller) || !DroneControllerItem.isControlling(controller)) {
                return;
            }
            UUID linked = DroneControllerItem.getLinkedDroneId(controller);
            if (linked == null || !linked.equals(message.droneId)) {
                return;
            }
            Entity entity = player.serverLevel().getEntity(message.droneId);
            if (entity instanceof FpvDroneEntity drone) {
                drone.applyControllerInput(message.mouseYaw, message.mousePitch, message.yawInput, message.pitchInput, message.throttleDelta);
            } else {
                DroneControllerItem.clearDrone(controller);
            }
        });
    }

    private static ItemStack findController(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (DroneControllerItem.isController(main)) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        return DroneControllerItem.isController(off) ? off : ItemStack.EMPTY;
    }
}
