package hi.network;

import hi.CreateTheAirWarsMod;
import hi.block.entity.MonitorBlockEntity;
import hi.block.entity.VihrLauncherBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VihrReloadPacket(BlockPos monitorPos) implements CustomPacketPayload {
    public static final Type<VihrReloadPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "vihr_reload"));
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, VihrReloadPacket> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((buf, msg) -> msg.write(buf), VihrReloadPacket::new);

    public VihrReloadPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.monitorPos);
    }

    public static void handleData(final VihrReloadPacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null || !(player.level() instanceof ServerLevel level)) {
                return;
            }
            BlockEntity monitorBe = level.getBlockEntity(message.monitorPos());
            if (!(monitorBe instanceof MonitorBlockEntity monitor) || !monitor.hasLinkedVihr()) {
                return;
            }
            int linkedCount = monitor.getLinkedVihrPositions().size();
            for (int i = 0; i < linkedCount; i++) {
                BlockPos launcherPos = monitor.selectNextLinkedVihrPos();
                if (launcherPos == null) {
                    break;
                }
                BlockEntity launcherBe = level.getBlockEntity(launcherPos);
                if (launcherBe instanceof VihrLauncherBlockEntity launcher && launcher.getRocketCount() < 6) {
                    launcher.addRocket();
                    return;
                }
            }
        });
    }
}
