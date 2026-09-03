package hi.util;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.UUID;

public final class Aim9xTargetingHelper {
    private static final double MAX_TARGET_DISTANCE = 200.0;
    private static final double MAX_TARGET_DISTANCE_SQR = MAX_TARGET_DISTANCE * MAX_TARGET_DISTANCE;
    private static final double SEARCH_HALF_ANGLE_DEGREES = 70.0;
    private static final double SEARCH_COS_THRESHOLD = Math.cos(Math.toRadians(SEARCH_HALF_ANGLE_DEGREES));
    private static final double SUBLEVEL_VELOCITY_TO_TICK_SCALE = 1.0 / 20.0;

    private Aim9xTargetingHelper() {
    }

    public record TrackedSubLevelTarget(UUID id, Vec3 position, Vec3 velocityPerTick, double distanceSqr, double alignment) {
    }

    public static TrackedSubLevelTarget findBestTarget(ServerLevel level, BlockPos sourcePos, BlockState state) {
        ServerSubLevelContainer container = ServerSubLevelContainer.getContainer(level);
        if (container == null) {
            return null;
        }

        Direction facing = getDirection(state);
        ProjectileLaunchHelper.LaunchTransform launch = ProjectileLaunchHelper.resolveBlockLaunch(level, sourcePos,
            sourcePos.getX() + 0.5, sourcePos.getY() + 0.5, sourcePos.getZ() + 0.5,
            facing.getStepX(), 0.0, facing.getStepZ());

        Vec3 origin = launch.position();
        Vec3 forward = launch.direction().lengthSqr() > 1.0E-6 ? launch.direction().normalize() : new Vec3(facing.getStepX(), 0.0, facing.getStepZ()).normalize();
        SubLevel ownSubLevel = dev.ryanhcode.sable.Sable.HELPER.getContaining(level, sourcePos);
        UUID ownId = ownSubLevel != null ? ownSubLevel.getUniqueId() : null;

        TrackedSubLevelTarget bestTarget = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (ServerSubLevel subLevel : container.getAllSubLevels()) {
            if (subLevel == null || subLevel.isRemoved()) {
                continue;
            }
            if (ownId != null && ownId.equals(subLevel.getUniqueId())) {
                continue;
            }

            Vec3 targetCenter = toVec3(subLevel.boundingBox().center());
            Vec3 toTarget = targetCenter.subtract(origin);
            double distanceSqr = toTarget.lengthSqr();
            if (distanceSqr < 4.0 || distanceSqr > MAX_TARGET_DISTANCE_SQR) {
                continue;
            }

            Vec3 directionToTarget = toTarget.normalize();
            double alignment = forward.dot(directionToTarget);
            if (alignment < SEARCH_COS_THRESHOLD) {
                continue;
            }

            double score = alignment * 3.0 + (1.0 - Math.sqrt(distanceSqr) / MAX_TARGET_DISTANCE);
            if (score <= bestScore) {
                continue;
            }

            bestScore = score;
            bestTarget = new TrackedSubLevelTarget(
                subLevel.getUniqueId(),
                targetCenter,
                getVelocityPerTick(subLevel),
                distanceSqr,
                alignment
            );
        }

        return bestTarget;
    }

    public static TrackedSubLevelTarget resolveTarget(ServerLevel level, UUID subLevelId) {
        if (subLevelId == null) {
            return null;
        }
        ServerSubLevelContainer container = ServerSubLevelContainer.getContainer(level);
        if (container == null) {
            return null;
        }
        SubLevel subLevel = container.getSubLevel(subLevelId);
        if (!(subLevel instanceof ServerSubLevel serverSubLevel) || serverSubLevel.isRemoved()) {
            return null;
        }

        return new TrackedSubLevelTarget(
            serverSubLevel.getUniqueId(),
            toVec3(serverSubLevel.boundingBox().center()),
            getVelocityPerTick(serverSubLevel),
            0.0,
            1.0
        );
    }

    private static Vec3 getVelocityPerTick(ServerSubLevel subLevel) {
        if (subLevel.latestLinearVelocity == null) {
            return Vec3.ZERO;
        }
        return new Vec3(
            subLevel.latestLinearVelocity.x() * SUBLEVEL_VELOCITY_TO_TICK_SCALE,
            subLevel.latestLinearVelocity.y() * SUBLEVEL_VELOCITY_TO_TICK_SCALE,
            subLevel.latestLinearVelocity.z() * SUBLEVEL_VELOCITY_TO_TICK_SCALE
        );
    }

    private static Vec3 toVec3(Vector3d vector) {
        return new Vec3(vector.x, vector.y, vector.z);
    }

    private static Direction getDirection(BlockState state) {
        Property<?> prop = state.getBlock().getStateDefinition().getProperty("facing");
        if (prop instanceof DirectionProperty directionProperty) {
            return state.getValue(directionProperty);
        }
        prop = state.getBlock().getStateDefinition().getProperty("axis");
        return prop instanceof EnumProperty enumProperty && enumProperty.getPossibleValues().toArray()[0] instanceof Direction.Axis
            ? Direction.fromAxisAndDirection((Direction.Axis) state.getValue(enumProperty), Direction.AxisDirection.POSITIVE)
            : Direction.NORTH;
    }
}
