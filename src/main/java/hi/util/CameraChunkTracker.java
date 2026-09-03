package hi.util;

import hi.block.entity.MonitorBlockEntity;
import hi.config.CameraMonitorServerConfig;
import hi.mixin.camera.ChunkMapAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import hi.block.entity.CameraBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CameraChunkTracker {
    private static final int TICKET_LEVEL = 2; // Level 2 loads chunk + ticks entities
    private static final Map<UUID, Set<ChunkPos>> PLAYER_CAMERA_CHUNKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<ChunkPos>> PLAYER_QUEUED_CAMERA_CHUNKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Set<ChunkPos>> PLAYER_SENT_CAMERA_CHUNKS = new ConcurrentHashMap<>();
    private static final Map<TicketKey, Set<UUID>> CHUNK_TRACKING_PLAYERS = new ConcurrentHashMap<>();
    private static final Map<TicketKey, BlockPos> ACTIVE_SERVER_TICKETS = new ConcurrentHashMap<>();

    private CameraChunkTracker() {
    }

    public static void serverTick(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ServerLevel collisionLevel = hi.util.SableCoordinateHelper.resolveCollisionLevel(level);
        if (collisionLevel == null) {
            collisionLevel = level;
        }
        UUID uuid = player.getUUID();
        
        if (!CameraMonitorServerConfig.shouldLoadChunksByCamera()) {
            Set<ChunkPos> oldTracking = PLAYER_CAMERA_CHUNKS.getOrDefault(uuid, Collections.emptySet());
            for (ChunkPos pos : oldTracking) {
                removePlayerTracking(player, collisionLevel, pos);
            }
            PLAYER_CAMERA_CHUNKS.remove(uuid);
            PLAYER_QUEUED_CAMERA_CHUNKS.remove(uuid);
            PLAYER_SENT_CAMERA_CHUNKS.remove(uuid);
            return;
        }

        BlockPos playerPos = player.blockPosition();
        int radius = 24;
        int configuredCameraRadius = Math.max(1, CameraMonitorServerConfig.getMonitorFeedViewDistance());

        Set<ChunkPos> newTracking = new HashSet<>();

        // Actually, to make it 100% reliable, we can scan loaded chunks near player
        ChunkPos playerChunk = new ChunkPos(playerPos);
        int chunkRadius = 2; // 2 chunks radius = 5x5 chunks around the player
        for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
            for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
                net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunkSource().getChunk(playerChunk.x + cx, playerChunk.z + cz, false);
                if (chunk != null) {
                    for (BlockEntity be : chunk.getBlockEntities().values()) {
                        if (be instanceof MonitorBlockEntity monitor && monitor.hasLinkedCamera()) {
                            BlockPos camPos = monitor.getLinkedCameraPos();
                            if (playerPos.closerThan(monitor.getBlockPos(), 24.0)) {
                                ResolvedCamera resolved = resolveCamera(level, camPos);
                                if (resolved != null) {
                                    CameraBlockEntity camera = resolved.camera;
                                    net.minecraft.world.level.Level cameraLevel = resolved.level;
                                    
                                    // Project position and look direction to the Overworld (world space)
                                    Vec3 localOrigin = camera.getBlockPos().getCenter();
                                    Vec3 worldPos = hi.util.SableCoordinateHelper.projectOut(cameraLevel, localOrigin);
                                    Vec3 worldLook = hi.util.SableCoordinateHelper.projectDirectionOut(
                                        cameraLevel, 
                                        localOrigin, 
                                        Vec3.directionFromRotation(camera.getPitch(), camera.getYaw())
                                    );
                                    
                                    // Load a full circle of chunks around the camera, like a player does, to ensure instant rotation rendering
                                    if (CameraMonitorServerConfig.shouldLoadChunksByCamera()) {
                                        ChunkPos camChunk = new ChunkPos(BlockPos.containing(worldPos));
                                        int camRadius = configuredCameraRadius;
                                        for (int dx = -camRadius; dx <= camRadius; dx++) {
                                            for (int dz = -camRadius; dz <= camRadius; dz++) {
                                                if (dx * dx + dz * dz <= camRadius * camRadius) {
                                                    newTracking.add(new ChunkPos(camChunk.x + dx, camChunk.z + dz));
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (CameraMonitorServerConfig.shouldLoadChunksByCamera()) {
                                        // Fallback: project the local camPos to world space and track a 5x5 area
                                        Vec3 worldPos = hi.util.SableCoordinateHelper.projectOut(level, camPos.getCenter());
                                        ChunkPos camChunk = new ChunkPos(BlockPos.containing(worldPos));
                                        for (int dx = -configuredCameraRadius; dx <= configuredCameraRadius; dx++) {
                                            for (int dz = -configuredCameraRadius; dz <= configuredCameraRadius; dz++) {
                                                if (dx * dx + dz * dz <= configuredCameraRadius * configuredCameraRadius) {
                                                    newTracking.add(new ChunkPos(camChunk.x + dx, camChunk.z + dz));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Set<ChunkPos> oldTracking = PLAYER_CAMERA_CHUNKS.getOrDefault(uuid, Collections.emptySet());
        Set<ChunkPos> queuedChunks = PLAYER_QUEUED_CAMERA_CHUNKS.computeIfAbsent(uuid, ignored -> ConcurrentHashMap.newKeySet());
        Set<ChunkPos> sentChunks = PLAYER_SENT_CAMERA_CHUNKS.computeIfAbsent(uuid, ignored -> ConcurrentHashMap.newKeySet());

        // Process chunks to add
        for (ChunkPos pos : newTracking) {
            // Refresh even when the player was already tracking it: PORTAL
            // tickets have a timeout and otherwise remote camera chunks vanish.
            addServerTicket(collisionLevel, pos);
            if (!queuedChunks.contains(pos)) {
                // A ticket is asynchronous.  The old code attempted this only once,
                // before getChunkToSend() was ready, so entities arrived without terrain.
                // Retry until the full chunk can actually be queued, then never resend it.
                if (collisionLevel.getChunkSource().chunkMap.getChunkToSend(pos.toLong()) != null) {
                    ((ChunkMapAccessor) collisionLevel.getChunkSource().chunkMap).ctaw$markChunkPendingToSend(player, pos);
                    queuedChunks.add(pos);
                }
            }
            if (!oldTracking.contains(pos)) {
                TicketKey key = new TicketKey(collisionLevel.dimension(), pos);
                CHUNK_TRACKING_PLAYERS.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(uuid);
            }
        }

        // Process chunks to remove
        for (ChunkPos pos : oldTracking) {
            if (!newTracking.contains(pos)) {
                removePlayerTracking(player, collisionLevel, pos);
            }
        }

        if (newTracking.isEmpty()) {
            PLAYER_CAMERA_CHUNKS.remove(uuid);
            PLAYER_QUEUED_CAMERA_CHUNKS.remove(uuid);
            PLAYER_SENT_CAMERA_CHUNKS.remove(uuid);
        } else {
            PLAYER_CAMERA_CHUNKS.put(uuid, newTracking);
            queuedChunks.retainAll(newTracking);
            sentChunks.retainAll(newTracking);
        }
    }

    private static void removePlayerTracking(ServerPlayer player, ServerLevel level, ChunkPos pos) {
        UUID uuid = player.getUUID();
        Set<ChunkPos> queued = PLAYER_QUEUED_CAMERA_CHUNKS.get(uuid);
        if (queued != null) queued.remove(pos);
        Set<ChunkPos> sent = PLAYER_SENT_CAMERA_CHUNKS.get(uuid);
        if (sent != null && sent.remove(pos) && !player.getChunkTrackingView().contains(pos)) {
            ChunkMapAccessor.ctaw$dropChunk(player, pos);
        }
        // Releasing the server ticket is enough. Forcing a client drop packet here
        // fights Sodium/Embeddium/Distant Horizons and can make chunks flicker or
        // reload every tick while a monitor feed is active.
        TicketKey key = new TicketKey(level.dimension(), pos);
        Set<UUID> players = CHUNK_TRACKING_PLAYERS.get(key);
        if (players != null) {
            players.remove(uuid);
            if (players.isEmpty()) {
                CHUNK_TRACKING_PLAYERS.remove(key);
                // Release server ticket
                removeServerTicket(level, pos);
            }
        }
    }

    public static boolean isTracking(ServerPlayer player, int chunkX, int chunkZ) {
        // Entity pairing and ChunkMap tracking may begin only after terrain was queued.
        Set<ChunkPos> chunks = PLAYER_SENT_CAMERA_CHUNKS.get(player.getUUID());
        return chunks != null && chunks.contains(new ChunkPos(chunkX, chunkZ));
    }

    public static void chunkSent(ServerPlayer player, ChunkPos pos) {
        Set<ChunkPos> requested = PLAYER_CAMERA_CHUNKS.get(player.getUUID());
        if (requested != null && requested.contains(pos)) {
            PLAYER_SENT_CAMERA_CHUNKS.computeIfAbsent(player.getUUID(), ignored -> ConcurrentHashMap.newKeySet()).add(pos);
        }
    }

    public static void playerLoggedOut(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Set<ChunkPos> tracking = PLAYER_CAMERA_CHUNKS.remove(uuid);
        PLAYER_QUEUED_CAMERA_CHUNKS.remove(uuid);
        PLAYER_SENT_CAMERA_CHUNKS.remove(uuid);
        if (tracking != null) {
            ServerLevel collisionLevel = hi.util.SableCoordinateHelper.resolveCollisionLevel(player.serverLevel());
            if (collisionLevel == null) {
                collisionLevel = player.serverLevel();
            }
            for (ChunkPos pos : tracking) {
                TicketKey key = new TicketKey(collisionLevel.dimension(), pos);
                Set<UUID> players = CHUNK_TRACKING_PLAYERS.get(key);
                if (players != null) {
                    players.remove(uuid);
                    if (players.isEmpty()) {
                        CHUNK_TRACKING_PLAYERS.remove(key);
                        removeServerTicket(collisionLevel, pos);
                    }
                }
            }
        }
    }

    private static void addServerTicket(ServerLevel level, ChunkPos pos) {
        TicketKey key = new TicketKey(level.dimension(), pos);
        BlockPos ticketPos = ACTIVE_SERVER_TICKETS.computeIfAbsent(key, k -> {
            BlockPos createdPos = pos.getWorldPosition();
            return createdPos;
        });
        level.getChunkSource().addRegionTicket(TicketType.PORTAL, pos, TICKET_LEVEL, ticketPos);
    }

    private static void removeServerTicket(ServerLevel level, ChunkPos pos) {
        BlockPos ticketPos = ACTIVE_SERVER_TICKETS.remove(new TicketKey(level.dimension(), pos));
        if (ticketPos != null) {
            level.getChunkSource().removeRegionTicket(TicketType.PORTAL, pos, TICKET_LEVEL, ticketPos);
        }
    }

    private record TicketKey(ResourceKey<Level> dimension, ChunkPos chunk) {
    }

    private static class ResolvedCamera {
        public final CameraBlockEntity camera;
        public final net.minecraft.world.level.Level level;
        
        public ResolvedCamera(CameraBlockEntity camera, net.minecraft.world.level.Level level) {
            this.camera = camera;
            this.level = level;
        }
    }
    
    private static ResolvedCamera resolveCamera(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CameraBlockEntity camera) {
            return new ResolvedCamera(camera, level);
        }
        try {
            Class<?> containerClass = Class.forName("dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer");
            java.lang.reflect.Method getContainerMethod = containerClass.getMethod("getContainer", ServerLevel.class);
            Object container = getContainerMethod.invoke(null, level);
            if (container != null) {
                java.lang.reflect.Method getAllSubLevelsMethod = containerClass.getMethod("getAllSubLevels");
                java.util.Collection<?> subLevels = (java.util.Collection<?>) getAllSubLevelsMethod.invoke(container);
                for (Object subLevel : subLevels) {
                    if (subLevel != null) {
                        java.lang.reflect.Method isRemovedMethod = subLevel.getClass().getMethod("isRemoved");
                        if (!(Boolean) isRemovedMethod.invoke(subLevel)) {
                            java.lang.reflect.Method getLevelMethod = subLevel.getClass().getMethod("getLevel");
                            ServerLevel subLevelWorld = (ServerLevel) getLevelMethod.invoke(subLevel);
                            if (subLevelWorld != null) {
                                BlockEntity subBe = subLevelWorld.getBlockEntity(pos);
                                if (subBe instanceof CameraBlockEntity camera) {
                                    return new ResolvedCamera(camera, subLevelWorld);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}
