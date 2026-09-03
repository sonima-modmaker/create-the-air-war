package hi.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class LauncherCrashDetector {
    public static final double MIN_SPEED_FOR_CRASH = 16.0;
    public static final double DECELERATION_THRESHOLD = 7.0;
    public static final double DECELERATION_RATIO = 0.45;

    private LauncherCrashDetector() {
    }

    public static boolean detectCrash(BlockEntity be, Level level, Vector3d lastVelocity, boolean initialized) {
        if (!initialized) {
            return false;
        }
        try {
            Object subLevel = dev.ryanhcode.sable.Sable.HELPER.getContaining(be);
            if (!(subLevel instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel ssl)) {
                return false;
            }

            Vector3dc currentVel = ssl.latestLinearVelocity;
            if (currentVel == null) {
                return false;
            }

            double oldSpeed = lastVelocity.length();
            double newSpeed = currentVel.length();
            double deceleration = oldSpeed - newSpeed;

            if (oldSpeed < MIN_SPEED_FOR_CRASH || deceleration < DECELERATION_THRESHOLD) {
                return false;
            }

            double ratio = deceleration / Math.max(oldSpeed, 0.001);
            if (ratio < DECELERATION_RATIO) {
                return false;
            }

            return hasImpactSurface(be, level, currentVel);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean updateVelocity(BlockEntity be, Vector3d lastVelocity) {
        try {
            Object subLevel = dev.ryanhcode.sable.Sable.HELPER.getContaining(be);
            if (!(subLevel instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel ssl)) {
                return false;
            }

            Vector3dc currentVel = ssl.latestLinearVelocity;
            if (currentVel == null) {
                return false;
            }

            lastVelocity.set(currentVel);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean hasImpactSurface(BlockEntity be, Level level, Vector3dc currentVel) {
        Vec3 velocity = new Vec3(currentVel.x(), currentVel.y(), currentVel.z());
        if (velocity.lengthSqr() < 1.0e-4) {
            return false;
        }

        Vec3 origin = dev.ryanhcode.sable.Sable.HELPER.projectOutOfSubLevel(level, Vec3.atCenterOf(be.getBlockPos()));
        Vec3 direction = velocity.normalize();
        double[] probes = {0.8, 1.4, 2.2};

        for (double distance : probes) {
            Vec3 probe = origin.add(direction.scale(distance));
            BlockPos probePos = BlockPos.containing(probe);
            BlockState state = level.getBlockState(probePos);
            if (!state.isAir() && !state.getCollisionShape(level, probePos).isEmpty()) {
                return true;
            }
        }

        return false;
    }
}
