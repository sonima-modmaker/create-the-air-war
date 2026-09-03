package hi.network;

import hi.CreateTheAirWarsMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ScreenshakePacket(double time, double radius, double amplitude, double x, double y, double z) implements CustomPacketPayload {
    public static final Type<ScreenshakePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "screenshake_packet"));
    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ScreenshakePacket> STREAM_CODEC =
        StreamCodec.of((buf, msg) -> msg.write(buf), ScreenshakePacket::new);

    public ScreenshakePacket(FriendlyByteBuf buf) {
        this(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeDouble(this.time);
        buf.writeDouble(this.radius);
        buf.writeDouble(this.amplitude);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
    }

    public static void handleData(final ScreenshakePacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            hi.client.ScreenshakeHandler.shakeTime = message.time();
            hi.client.ScreenshakeHandler.shakeRadius = message.radius();
            hi.client.ScreenshakeHandler.shakeAmplitude = message.amplitude() * (Math.PI / 180.0);
            hi.client.ScreenshakeHandler.shakePos[0] = message.x();
            hi.client.ScreenshakeHandler.shakePos[1] = message.y();
            hi.client.ScreenshakeHandler.shakePos[2] = message.z();
            hi.client.ScreenshakeHandler.shakeType = 2.0 * (Math.random() - 0.5);
        });
    }
}
