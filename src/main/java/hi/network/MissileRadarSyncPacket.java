package hi.network;

import hi.CreateTheAirWarsMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record MissileRadarSyncPacket(List<MissileData> missiles) implements CustomPacketPayload {
    public static final Type<MissileRadarSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "missile_radar_sync_packet"));

    public static final StreamCodec<FriendlyByteBuf, MissileRadarSyncPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> {
            buf.writeInt(packet.missiles.size());
            for (MissileData m : packet.missiles) {
                buf.writeUUID(m.uuid());
                buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(m.type()));
                buf.writeDouble(m.x());
                buf.writeDouble(m.y());
                buf.writeDouble(m.z());
                buf.writeDouble(m.vx());
                buf.writeDouble(m.vy());
                buf.writeDouble(m.vz());
                buf.writeBoolean(m.alive());
            }
        },
        buf -> {
            int count = buf.readInt();
            List<MissileData> list = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                UUID uuid = buf.readUUID();
                ResourceLocation typeLoc = buf.readResourceLocation();
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(typeLoc);
                double x = buf.readDouble();
                double y = buf.readDouble();
                double z = buf.readDouble();
                double vx = buf.readDouble();
                double vy = buf.readDouble();
                double vz = buf.readDouble();
                boolean alive = buf.readBoolean();
                list.add(new MissileData(uuid, type, x, y, z, vx, vy, vz, alive));
            }
            return new MissileRadarSyncPacket(list);
        }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(final MissileRadarSyncPacket message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            hi.client.radar.ClientMissileTracker.handleSyncPacket(message);
        });
    }

    public record MissileData(UUID uuid, EntityType<?> type, double x, double y, double z, double vx, double vy, double vz, boolean alive) {}
}
