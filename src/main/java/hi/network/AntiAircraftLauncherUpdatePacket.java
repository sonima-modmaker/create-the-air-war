package hi.network;

import hi.block.entity.AntiAircraftLauncherBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import hi.CreateTheAirWarsMod;

import java.util.ArrayList;
import java.util.List;

public record AntiAircraftLauncherUpdatePacket(BlockPos pos, boolean isBlacklist, List<String> targets) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AntiAircraftLauncherUpdatePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "anti_aircraft_launcher_update_packet"));
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, AntiAircraftLauncherUpdatePacket> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((buf, msg) -> msg.write(buf), AntiAircraftLauncherUpdatePacket::new);

    public AntiAircraftLauncherUpdatePacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readBoolean(), readStringList(buf));
    }

    private static List<String> readStringList(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(buf.readUtf());
        }
        return list;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(isBlacklist);
        buf.writeVarInt(targets.size());
        for (String target : targets) {
            buf.writeUtf(target);
        }
    }

    public static void handleData(final AntiAircraftLauncherUpdatePacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null && player.level() != null) {
                BlockEntity be = player.level().getBlockEntity(message.pos);
                if (be instanceof AntiAircraftLauncherBlockEntity launcher) {
                    launcher.setBlacklist(message.isBlacklist);
                    launcher.clearTargets();
                    for (String target : message.targets) {
                        launcher.addTarget(target);
                    }
                    launcher.setChanged();
                    player.level().sendBlockUpdated(message.pos, be.getBlockState(), be.getBlockState(), 3);
                }
            }
        });
    }
}
