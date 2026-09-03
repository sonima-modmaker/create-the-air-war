package hi.block.entity;

import hi.client.sound.Aim9xSeekerSoundManager;
import hi.entity.Aim9xbultEntity;
import hi.entity.HeattrapFlareEntity;
import hi.init.CreateTheAirWarsModBlockEntities;
import hi.procedures.Aim9xRiedstounVkliuchionProcedure;
import hi.util.Aim9xTargetingHelper;
import hi.util.LauncherCrashDetector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.UUID;

public class Aim9xBlockEntity extends BlockEntity {
    private static final int SEARCH_TICKS_REQUIRED = 60;
    private static final int LOCK_TICKS_REQUIRED = 40;
    private static final int MIN_SEARCH_TICKS_REQUIRED = 8;
    private static final int MIN_LOCK_TICKS_REQUIRED = 6;
    private static final int TARGET_SCAN_INTERVAL_TICKS = 5;
    private static final int CRASH_CHECK_INTERVAL_TICKS = 4;
    private static final int CLIENT_SOUND_UPDATE_INTERVAL_TICKS = 4;
    private static final double ENTITY_TARGET_SEARCH_RANGE = 320.0;
    private static final double ENTITY_TARGET_SEARCH_RANGE_SQR = ENTITY_TARGET_SEARCH_RANGE * ENTITY_TARGET_SEARCH_RANGE;
    private static final double ENTITY_TARGET_MIN_ALIGNMENT = 0.5735764363510460D; // 110 degree seeker cone.

    public enum SeekerState {
        IDLE,
        SEARCHING,
        LOCKING;

        public static SeekerState fromOrdinal(int value) {
            SeekerState[] values = values();
            if (value < 0 || value >= values.length) {
                return IDLE;
            }
            return values[value];
        }
    }

    private final Vector3d lastVelocity = new Vector3d();
    private boolean velocityInitialized = false;
    private SeekerState seekerState = SeekerState.IDLE;
    private UUID targetSubLevelId;
    private UUID targetRocketId;
    private UUID targetHeatTrapId;
    private int searchTicks;
    private int lockTicks;
    private int targetScanCooldown;
    private int crashCheckCooldown;
    private net.minecraft.world.phys.AABB cachedSearchBox;

    public Aim9xBlockEntity(BlockPos pos, BlockState state) {
        super(CreateTheAirWarsModBlockEntities.AIM9X.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, Aim9xBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (be.crashCheckCooldown-- <= 0) {
            be.crashCheckCooldown = CRASH_CHECK_INTERVAL_TICKS;
            try {
                if (LauncherCrashDetector.detectCrash(be, level, be.lastVelocity, be.velocityInitialized)) {
                    hi.util.ExplosionUtils.safeExplode(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5f, true);
                    hi.util.ExplosionUtils.applyShake(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2.0);
                    hi.util.ExplosionUtils.playSoundSafe(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        "create_the_air_wars:shellexp2", net.minecraft.sounds.SoundSource.NEUTRAL, 5f, 1f);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    return;
                }
                be.velocityInitialized = LauncherCrashDetector.updateVelocity(be, be.lastVelocity);
            } catch (Throwable ignored) {
            }
        }

        boolean powered = level.getBestNeighborSignal(pos) > 0;
        if (!powered) {
            be.reset(SeekerState.IDLE);
            return;
        }

        HeattrapFlareEntity heatTrapTarget = be.resolveTrackedHeatTrapTarget(serverLevel);
        EntityTarget rocketTarget = heatTrapTarget == null ? be.resolveTrackedRocketTarget(serverLevel) : null;
        Aim9xTargetingHelper.TrackedSubLevelTarget target = heatTrapTarget == null && rocketTarget == null && be.targetSubLevelId != null
            ? Aim9xTargetingHelper.resolveTarget(serverLevel, be.targetSubLevelId)
            : null;
        if (be.targetScanCooldown-- <= 0) {
            heatTrapTarget = be.findBestHeatTrapTarget(serverLevel, pos, state);
            rocketTarget = heatTrapTarget == null ? be.findBestRocketTarget(serverLevel, pos, state) : null;
            target = nextTargetId(heatTrapTarget, rocketTarget) == null ? Aim9xTargetingHelper.findBestTarget(serverLevel, pos, state) : null;
            be.targetScanCooldown = TARGET_SCAN_INTERVAL_TICKS;
        }
        UUID nextHeatTrapId = heatTrapTarget != null ? heatTrapTarget.getUUID() : null;
        UUID nextRocketId = rocketTarget != null ? rocketTarget.id() : null;
        UUID nextSubLevelId = nextHeatTrapId == null && nextRocketId == null && target != null ? target.id() : null;
        double targetDistanceSqr = target != null ? target.distanceSqr()
            : rocketTarget != null ? rocketTarget.position().distanceToSqr(Vec3.atCenterOf(pos))
            : heatTrapTarget != null ? heatTrapTarget.position().distanceToSqr(Vec3.atCenterOf(pos))
            : Double.POSITIVE_INFINITY;
        int requiredSearchTicks = be.computeRequiredSearchTicks(targetDistanceSqr);
        int requiredLockTicks = be.computeRequiredLockTicks(targetDistanceSqr);

        if (nextHeatTrapId == null && nextRocketId == null && nextSubLevelId == null) {
            be.clearTargets();
            be.searchTicks = 0;
            be.lockTicks = 0;
            be.setSeekerState(SeekerState.SEARCHING);
            return;
        }

        if (!be.matchesTarget(nextHeatTrapId, nextRocketId, nextSubLevelId)) {
            be.targetHeatTrapId = nextHeatTrapId;
            be.targetRocketId = nextRocketId;
            be.targetSubLevelId = nextSubLevelId;
            be.searchTicks = 0;
            be.lockTicks = 0;
            be.setSeekerState(SeekerState.SEARCHING);
        }

        if (be.searchTicks < requiredSearchTicks) {
            be.searchTicks++;
            be.lockTicks = 0;
            be.setSeekerState(SeekerState.SEARCHING);
            return;
        }

        be.setSeekerState(SeekerState.LOCKING);
        be.lockTicks++;
        if (be.lockTicks < requiredLockTicks) {
            return;
        }

        if (level.getBestNeighborSignal(pos) <= 0) {
            be.reset(SeekerState.IDLE);
            return;
        }

        Aim9xRiedstounVkliuchionProcedure.execute(serverLevel, pos.getX(), pos.getY(), pos.getZ(), state, be.targetSubLevelId, be.targetRocketId, be.targetHeatTrapId);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, Aim9xBlockEntity be) {
        if (level == null || !level.isClientSide()) {
            return;
        }
        if ((level.getGameTime() + pos.asLong()) % CLIENT_SOUND_UPDATE_INTERVAL_TICKS != 0L) {
            return;
        }
        Aim9xSeekerSoundManager.update(be);
    }

    public SeekerState getSeekerState() {
        return seekerState;
    }

    private void reset(SeekerState nextState) {
        clearTargets();
        searchTicks = 0;
        lockTicks = 0;
        setSeekerState(nextState);
    }

    private void clearTargets() {
        targetSubLevelId = null;
        targetRocketId = null;
        targetHeatTrapId = null;
    }

    private boolean matchesTarget(UUID heatTrapId, UUID rocketId, UUID subLevelId) {
        return equalsNullable(targetHeatTrapId, heatTrapId)
            && equalsNullable(targetRocketId, rocketId)
            && equalsNullable(targetSubLevelId, subLevelId);
    }

    private boolean equalsNullable(UUID a, UUID b) {
        return a == null ? b == null : a.equals(b);
    }

    private void setSeekerState(SeekerState nextState) {
        if (seekerState == nextState) {
            return;
        }
        seekerState = nextState;
        setChanged();
        syncToClient();
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("SeekerState", seekerState.ordinal());
        tag.putInt("SearchTicks", searchTicks);
        tag.putInt("LockTicks", lockTicks);
        if (targetSubLevelId != null) {
            tag.putUUID("TargetSubLevelId", targetSubLevelId);
        }
        if (targetRocketId != null) {
            tag.putUUID("TargetRocketId", targetRocketId);
        }
        if (targetHeatTrapId != null) {
            tag.putUUID("TargetHeatTrapId", targetHeatTrapId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        seekerState = SeekerState.fromOrdinal(tag.getInt("SeekerState"));
        searchTicks = tag.getInt("SearchTicks");
        lockTicks = tag.getInt("LockTicks");
        targetSubLevelId = tag.hasUUID("TargetSubLevelId") ? tag.getUUID("TargetSubLevelId") : null;
        targetRocketId = tag.hasUUID("TargetRocketId") ? tag.getUUID("TargetRocketId") : null;
        targetHeatTrapId = tag.hasUUID("TargetHeatTrapId") ? tag.getUUID("TargetHeatTrapId") : null;
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt, net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            loadAdditional(tag, provider);
        }
    }

    private HeattrapFlareEntity findBestHeatTrapTarget(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        Vec3 origin = Vec3.atCenterOf(pos);
        Vec3 forward = getFacingVector(state);
        HeattrapFlareEntity bestTarget = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (HeattrapFlareEntity flare : serverLevel.getEntitiesOfClass(HeattrapFlareEntity.class, getSearchBox(pos), HeattrapFlareEntity::canAttractAim9x)) {
            double score = computeTargetScore(origin, forward, flare.position());
            if (score > bestScore) {
                bestScore = score;
                bestTarget = flare;
            }
        }
        return bestTarget;
    }

    private EntityTarget findBestRocketTarget(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        Vec3 origin = Vec3.atCenterOf(pos);
        Vec3 forward = getFacingVector(state);
        EntityTarget bestTarget = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (net.minecraft.world.entity.Entity entity : serverLevel.getEntities((net.minecraft.world.entity.Entity) null, getSearchBox(pos), Aim9xbultEntity::isValidSeekerRocketTarget)) {
            double score = computeTargetScore(origin, forward, entity.position()) + getRocketTargetPriorityBonus(entity);
            if (score > bestScore) {
                bestScore = score;
                bestTarget = new EntityTarget(entity.getUUID(), entity.position());
            }
        }
        return bestTarget;
    }

    private net.minecraft.world.phys.AABB getSearchBox(BlockPos pos) {
        if (cachedSearchBox == null) {
            cachedSearchBox = new net.minecraft.world.phys.AABB(pos).inflate(ENTITY_TARGET_SEARCH_RANGE);
        }
        return cachedSearchBox;
    }

    private HeattrapFlareEntity resolveTrackedHeatTrapTarget(ServerLevel serverLevel) {
        if (targetHeatTrapId == null) {
            return null;
        }
        net.minecraft.world.entity.Entity entity = serverLevel.getEntity(targetHeatTrapId);
        if (entity instanceof HeattrapFlareEntity flare && flare.canAttractAim9x()) {
            return flare;
        }
        targetHeatTrapId = null;
        return null;
    }

    private EntityTarget resolveTrackedRocketTarget(ServerLevel serverLevel) {
        if (targetRocketId == null) {
            return null;
        }
        net.minecraft.world.entity.Entity entity = serverLevel.getEntity(targetRocketId);
        if (Aim9xbultEntity.isValidSeekerRocketTarget(entity)) {
            return new EntityTarget(entity.getUUID(), entity.position());
        }
        targetRocketId = null;
        return null;
    }

    private static UUID nextTargetId(HeattrapFlareEntity heatTrapTarget, EntityTarget rocketTarget) {
        if (heatTrapTarget != null) {
            return heatTrapTarget.getUUID();
        }
        return rocketTarget != null ? rocketTarget.id() : null;
    }

    private double computeTargetScore(Vec3 origin, Vec3 forward, Vec3 targetPos) {
        Vec3 toTarget = targetPos.subtract(origin);
        double distanceSqr = toTarget.lengthSqr();
        if (distanceSqr < 1.0 || distanceSqr > ENTITY_TARGET_SEARCH_RANGE_SQR) {
            return Double.NEGATIVE_INFINITY;
        }
        Vec3 direction = toTarget.normalize();
        double alignment = forward.dot(direction);
        if (alignment < ENTITY_TARGET_MIN_ALIGNMENT) {
            return Double.NEGATIVE_INFINITY;
        }
        return alignment * 4.0 + (1.0 - Math.sqrt(distanceSqr) / ENTITY_TARGET_SEARCH_RANGE);
    }

    private int computeRequiredSearchTicks(double distanceSqr) {
        return scaleRequiredTicks(distanceSqr, MIN_SEARCH_TICKS_REQUIRED, SEARCH_TICKS_REQUIRED);
    }

    private int computeRequiredLockTicks(double distanceSqr) {
        return scaleRequiredTicks(distanceSqr, MIN_LOCK_TICKS_REQUIRED, LOCK_TICKS_REQUIRED);
    }

    private int scaleRequiredTicks(double distanceSqr, int minTicks, int maxTicks) {
        if (!Double.isFinite(distanceSqr)) {
            return maxTicks;
        }
        double clampedDistance = Mth.clamp(Math.sqrt(Math.max(0.0D, distanceSqr)), 0.0D, ENTITY_TARGET_SEARCH_RANGE);
        double normalized = clampedDistance / ENTITY_TARGET_SEARCH_RANGE;
        return Mth.clamp(Mth.floor(Mth.lerp(normalized, minTicks, maxTicks)), minTicks, maxTicks);
    }

    private double getRocketTargetPriorityBonus(net.minecraft.world.entity.Entity entity) {
        if (entity instanceof hi.entity.VihrRocketEntity || entity instanceof hi.entity.X25mlEntity) {
            return 1.25D;
        }
        return 0.0D;
    }

    private Vec3 getFacingVector(BlockState state) {
        Direction facing = getDirection(state);
        return new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ()).normalize();
    }

    private Direction getDirection(BlockState state) {
        Property<?> prop = state.getBlock().getStateDefinition().getProperty("facing");
        if (prop instanceof DirectionProperty directionProperty) {
            return state.getValue(directionProperty);
        }
        prop = state.getBlock().getStateDefinition().getProperty("axis");
        return prop instanceof EnumProperty<?> enumProperty && enumProperty.getPossibleValues().toArray()[0] instanceof Direction.Axis
            ? Direction.fromAxisAndDirection((Direction.Axis) state.getValue((Property<Direction.Axis>) enumProperty), Direction.AxisDirection.POSITIVE)
            : Direction.NORTH;
    }

    private record EntityTarget(UUID id, Vec3 position) {
    }
}
