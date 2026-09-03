package hi.network;

import hi.CreateTheAirWarsMod;
import hi.block.entity.CameraBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CameraAdjustPacket(BlockPos cameraPos, float yawDelta, float pitchDelta) implements CustomPacketPayload {
    public static final Type<CameraAdjustPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "camera_adjust"));
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, CameraAdjustPacket> STREAM_CODEC = net.minecraft.network.codec.StreamCodec.of((buf, msg) -> msg.write(buf), CameraAdjustPacket::new);

    public CameraAdjustPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readFloat(), buf.readFloat());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(cameraPos);
        buf.writeFloat(yawDelta);
        buf.writeFloat(pitchDelta);
    }

    public static void handleData(final CameraAdjustPacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null || !(player.level() instanceof ServerLevel level)) {
                return;
            }
            BlockEntity be = level.getBlockEntity(message.cameraPos());
            if (be instanceof CameraBlockEntity camera) {
                camera.adjustTargetRotation(message.yawDelta(), message.pitchDelta());
            }
        });
    }
}
