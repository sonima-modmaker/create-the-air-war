package hi.client.radar;

import hi.network.MissileRadarSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public final class ClientMissileTracker {
    public static final String RADAR_VIRTUAL_TAG = "CTAW_RadarVirtual";
    private static final Map<UUID, ClientMissileEntry> TRACKED_MISSILES = new HashMap<>();
    private static boolean isEvaluating = false;

    private ClientMissileTracker() {}

    public static void handleSyncPacket(MissileRadarSyncPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Map<UUID, Boolean> receivedUUIDs = new HashMap<>();
        for (MissileRadarSyncPacket.MissileData data : packet.missiles()) {
            receivedUUIDs.put(data.uuid(), true);
            if (!data.alive()) {
                TRACKED_MISSILES.remove(data.uuid());
                continue;
            }

            ClientMissileEntry entry = TRACKED_MISSILES.get(data.uuid());
            if (entry == null) {
                Entity virtualEntity = data.type().create(mc.level);
                if (virtualEntity != null) {
                    virtualEntity.setUUID(data.uuid());
                    virtualEntity.getPersistentData().putBoolean(RADAR_VIRTUAL_TAG, true);
                    entry = new ClientMissileEntry(virtualEntity);
                    TRACKED_MISSILES.put(data.uuid(), entry);
                }
            }

            if (entry != null && entry.entity != null) {
                entry.entity.setPos(data.x(), data.y(), data.z());
                entry.entity.setDeltaMovement(data.vx(), data.vy(), data.vz());
                float yaw = (float) Math.toDegrees(Math.atan2(data.vx(), data.vz()));
                float pitch = (float) Math.toDegrees(Math.atan2(data.vy(), Math.hypot(data.vx(), data.vz())));
                entry.entity.setYRot(yaw);
                entry.entity.setXRot(pitch);
                entry.lastUpdateTick = mc.level.getGameTime();
            }
        }

        // Clean up stale missiles no longer present on server
        TRACKED_MISSILES.keySet().removeIf(uuid -> !receivedUUIDs.containsKey(uuid));
    }

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            TRACKED_MISSILES.clear();
            return;
        }

        for (ClientMissileEntry entry : TRACKED_MISSILES.values()) {
            if (entry.entity != null) {
                entry.entity.setPos(
                    entry.entity.getX() + entry.entity.getDeltaMovement().x,
                    entry.entity.getY() + entry.entity.getDeltaMovement().y,
                    entry.entity.getZ() + entry.entity.getDeltaMovement().z
                );
            }
        }
    }

    public static List<Entity> getTrackedMissiles() {
        if (isEvaluating) return Collections.emptyList();
        isEvaluating = true;
        try {
            Minecraft mc = Minecraft.getInstance();
            List<Entity> list = new ArrayList<>(TRACKED_MISSILES.size());
            if (mc.level == null) return list;

            for (ClientMissileEntry entry : TRACKED_MISSILES.values()) {
                if (entry.entity != null && entry.entity.isAlive()) {
                    list.add(entry.entity);
                }
            }
            return list;
        } finally {
            isEvaluating = false;
        }
    }

    private static final class ClientMissileEntry {
        private final Entity entity;
        private long lastUpdateTick;

        private ClientMissileEntry(Entity entity) {
            this.entity = entity;
        }
    }
}
