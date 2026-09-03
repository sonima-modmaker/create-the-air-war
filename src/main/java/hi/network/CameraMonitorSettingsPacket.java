package hi.network;

import hi.CreateTheAirWarsMod;
import hi.client.camera.CameraFeedRuntimeSettings;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CameraMonitorSettingsPacket(int feedFps, int feedResolution, int feedViewDistance) implements CustomPacketPayload {
    public static final Type<CameraMonitorSettingsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "camera_monitor_settings"));
    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, CameraMonitorSettingsPacket> STREAM_CODEC =
        StreamCodec.of((buf, msg) -> msg.write(buf), CameraMonitorSettingsPacket::new);

    public CameraMonitorSettingsPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.feedFps);
        buf.writeVarInt(this.feedResolution);
        buf.writeVarInt(this.feedViewDistance);
    }

    public static void handleData(final CameraMonitorSettingsPacket message, final IPayloadContext context) {
        context.enqueueWork(() -> CameraFeedRuntimeSettings.applyServerSettings(
            message.feedFps(),
            message.feedResolution(),
            message.feedViewDistance()
        ));
    }
}
