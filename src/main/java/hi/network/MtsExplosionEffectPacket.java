package hi.network;

import hi.CreateTheAirWarsMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MtsExplosionEffectPacket(String profile, double x, double y, double z,
                                       float yaw, float pitch, boolean blockHit, String material)
    implements CustomPacketPayload {
    public static final Type<MtsExplosionEffectPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "mts_explosion_effect"));
    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, MtsExplosionEffectPacket> STREAM_CODEC =
        StreamCodec.of((buf, msg) -> msg.write(buf), MtsExplosionEffectPacket::new);

    public MtsExplosionEffectPacket(FriendlyByteBuf buf) {
        this(buf.readUtf(64), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat(), buf.readBoolean(), buf.readUtf(32));
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUtf(profile, 64);
        buf.writeDouble(x); buf.writeDouble(y); buf.writeDouble(z);
        buf.writeFloat(yaw); buf.writeFloat(pitch);
        buf.writeBoolean(blockHit); buf.writeUtf(material, 32);
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleData(MtsExplosionEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> hi.client.particle.MtsExplosionEffectEngine.spawn(packet));
    }
}
