package hi.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ProjectileChunkLoader {
    private static final int TICKET_LEVEL = 2;
    private static final double AHEAD_SPEED_SQR = 0.25;
    private static final double AHEAD_LOOKAHEAD_TICKS = 24.0;
    private static final int MAX_LOOKAHEAD_CHUNKS = 24;
    private static final Map<UUID, TicketState> PROJECTILE_TICKETS = new HashMap<>();
    private static final Map<TicketKey, SharedTicket> SHARED_TICKETS = new HashMap<>();

    private ProjectileChunkLoader() {
    }

    public static void update(AbstractArrow projectile) {
        if (!(projectile.level() instanceof ServerLevel serverLevel) || !projectile.isAlive()) {
            return;
        }

        UUID projectileId = projectile.getUUID();
        TicketState state = PROJECTILE_TICKETS.computeIfAbsent(projectileId, ignored -> new TicketState());
        if (state.level != null && state.level != serverLevel) {
            clearState(state);
        }
        state.level = serverLevel;

        Set<ChunkPos> requiredChunks = new HashSet<>();
        ChunkPos currentChunk = projectile.chunkPosition();
        addWithBorder(requiredChunks, currentChunk, 1);

        Vec3 motion = projectile.getDeltaMovement();
        if (motion.lengthSqr() > AHEAD_SPEED_SQR) {
            double lookaheadTicks = Math.min(AHEAD_LOOKAHEAD_TICKS, (MAX_LOOKAHEAD_CHUNKS * 16.0D) / Math.max(0.001D, motion.length()));
            // Sample every eight blocks, not just a few points.  Sparse samples left
            // unloaded gaps that could swallow fast missiles between two tickets.
            int samples = Math.max(2, Math.min(MAX_LOOKAHEAD_CHUNKS * 2, (int) Math.ceil(motion.length() * lookaheadTicks / 8.0D)));
            for (int sample = 1; sample <= samples; sample++) {
                double ticksAhead = lookaheadTicks * sample / samples;
                BlockPos aheadPos = BlockPos.containing(
                    projectile.getX() + motion.x * ticksAhead,
                    projectile.getY() + motion.y * ticksAhead,
                    projectile.getZ() + motion.z * ticksAhead
                );
                requiredChunks.add(new ChunkPos(aheadPos));
            }
            Vec3 end = projectile.position().add(motion.scale(lookaheadTicks));
            addWithBorder(requiredChunks, new ChunkPos(BlockPos.containing(end)), 1);
        }

        syncState(state, requiredChunks);
    }

    /** Acquires tickets before an entity is inserted into a remote, currently dormant chunk. */
    public static void prime(AbstractArrow projectile) {
        update(projectile);
    }

    private static void addWithBorder(Set<ChunkPos> chunks, ChunkPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) chunks.add(new ChunkPos(center.x + x, center.z + z));
        }
    }

    public static void release(AbstractArrow projectile) {
        TicketState state = PROJECTILE_TICKETS.remove(projectile.getUUID());
        if (state == null) {
            return;
        }
        clearState(state);
    }

    private static void syncState(TicketState state, Set<ChunkPos> requiredChunks) {
        Set<ChunkPos> removed = new HashSet<>(state.chunks);
        removed.removeAll(requiredChunks);
        for (ChunkPos chunk : removed) {
            releaseSharedTicket(state.level, chunk);
            state.chunks.remove(chunk);
        }

        for (ChunkPos chunk : requiredChunks) {
            if (state.chunks.add(chunk)) {
                acquireSharedTicket(state.level, chunk);
            } else {
                refreshSharedTicket(state.level, chunk);
            }
        }
    }

    private static void clearState(TicketState state) {
        if (state.level != null) {
            for (ChunkPos chunk : state.chunks) {
                releaseSharedTicket(state.level, chunk);
            }
        }
        state.chunks.clear();
        state.level = null;
    }

    private static void acquireSharedTicket(ServerLevel level, ChunkPos chunk) {
        TicketKey key = new TicketKey(level.dimension(), chunk);
        SharedTicket shared = SHARED_TICKETS.get(key);
        if (shared != null) {
            shared.references++;
            return;
        }

        BlockPos ticketPos = chunk.getMiddleBlockPosition(0);
        level.getChunkSource().addRegionTicket(TicketType.PORTAL, chunk, TICKET_LEVEL, ticketPos);
        SHARED_TICKETS.put(key, new SharedTicket(ticketPos));
    }

    private static void releaseSharedTicket(ServerLevel level, ChunkPos chunk) {
        TicketKey key = new TicketKey(level.dimension(), chunk);
        SharedTicket shared = SHARED_TICKETS.get(key);
        if (shared == null || --shared.references > 0) {
            return;
        }

        level.getChunkSource().removeRegionTicket(TicketType.PORTAL, chunk, TICKET_LEVEL, shared.ticketPos);
        SHARED_TICKETS.remove(key);
    }

    private static void refreshSharedTicket(ServerLevel level, ChunkPos chunk) {
        SharedTicket shared = SHARED_TICKETS.get(new TicketKey(level.dimension(), chunk));
        if (shared != null) {
            // PORTAL tickets expire unless their timestamp is refreshed. Re-adding
            // the same ticket keeps long-flight missiles ticking without players.
            level.getChunkSource().addRegionTicket(TicketType.PORTAL, chunk, TICKET_LEVEL, shared.ticketPos);
        }
    }

    private static final class TicketState {
        private ServerLevel level;
        private final Set<ChunkPos> chunks = new HashSet<>();
    }

    private static final class SharedTicket {
        private final BlockPos ticketPos;
        private int references = 1;

        private SharedTicket(BlockPos ticketPos) {
            this.ticketPos = ticketPos;
        }
    }

    private record TicketKey(ResourceKey<Level> dimension, ChunkPos chunk) {
    }
}
