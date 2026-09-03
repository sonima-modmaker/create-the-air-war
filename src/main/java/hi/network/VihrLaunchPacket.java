package hi.network;

import hi.CreateTheAirWarsMod;
import hi.block.entity.CameraBlockEntity;
import hi.block.entity.MonitorLaunchableBlockEntity;
import hi.block.entity.MonitorBlockEntity;
import hi.block.entity.X25mlBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VihrLaunchPacket(BlockPos monitorPos) implements CustomPacketPayload {
    public static final Type<VihrLaunchPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "vihr_launch"));
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, VihrLaunchPacket> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((buf, msg) -> msg.write(buf), VihrLaunchPacket::new);

    public VihrLaunchPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(monitorPos);
    }

    public static void handleData(final VihrLaunchPacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null || !(player.level() instanceof ServerLevel level)) {
                return;
            }
            BlockEntity monitorBe = level.getBlockEntity(message.monitorPos());
            if (!(monitorBe instanceof MonitorBlockEntity monitor)) {
                player.displayClientMessage(Component.literal("VIHR: monitor missing"), true);
                return;
            }
            if (!monitor.hasLinkedCamera()) {
                player.displayClientMessage(Component.literal("VIHR: camera not linked"), true);
                return;
            }
            if (!monitor.hasLinkedVihr()) {
                player.displayClientMessage(Component.literal("VIHR: launcher not linked"), true);
                return;
            }
            BlockEntity cameraBe = level.getBlockEntity(monitor.getLinkedCameraPos());
            if (!(cameraBe instanceof CameraBlockEntity camera)) {
                player.displayClientMessage(Component.literal("VIHR: camera block invalid"), true);
                return;
            }
            MonitorLaunchableBlockEntity launcher = null;
            int linkedCount = monitor.getLinkedVihrPositions().size();
            for (int i = 0; i < linkedCount; i++) {
                BlockPos candidatePos = monitor.selectNextLinkedVihrPos();
                if (candidatePos == null) {
                    break;
                }
                BlockEntity launcherBe = level.getBlockEntity(candidatePos);
                if (launcherBe instanceof MonitorLaunchableBlockEntity candidate && candidate.hasLaunchPayload()) {
                    launcher = candidate;
                    break;
                }
            }
            if (launcher == null) {
                player.displayClientMessage(Component.literal("VIHR: no rockets loaded"), true);
                return;
            }
            if (!launcher.launch(camera)) {
                player.displayClientMessage(Component.literal("VIHR: launch failed"), true);
                return;
            }
            player.displayClientMessage(getLaunchMessage(launcher), true);
        });
    }

    private static Component getLaunchMessage(MonitorLaunchableBlockEntity launcher) {
        if (launcher instanceof X25mlBlockEntity) {
            return Component.translatable("message.create_the_air_wars.x25ml.launched");
        }
        return Component.translatable("message.create_the_air_wars.vihr.launched");
    }
}
