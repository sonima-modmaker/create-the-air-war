package hi.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public final class ProjectileLaunchHelper {
    private static final double SUBLEVEL_VELOCITY_TO_TICK_SCALE = 0.85 / 20.0;

    private ProjectileLaunchHelper() {
    }

    public record LaunchTransform(Vec3 position, Vec3 direction, Vec3 inheritedVelocity, boolean launchedFromSubLevel, double radioAltitude) {
    }

    public static LaunchTransform resolveBlockLaunch(ServerLevel level, BlockPos sourcePos, double spawnX, double spawnY, double spawnZ, double dirX, double dirY, double dirZ) {
        Vec3 position = new Vec3(spawnX, spawnY, spawnZ);
        Vec3 direction = new Vec3(dirX, dirY, dirZ);
        Vec3 inheritedVelocity = Vec3.ZERO;
        boolean launchedFromSubLevel = false;
        ServerLevel collisionLevel = resolveCollisionLevel(level);
        double radioAltitude = 0.0;

        try {
            dev.ryanhcode.sable.sublevel.SubLevel subLevel = dev.ryanhcode.sable.Sable.HELPER.getContaining(level, sourcePos);
            if (subLevel == null) {
                radioAltitude = getRadioAltitude(collisionLevel, position);
                return new LaunchTransform(position, direction, inheritedVelocity, false, radioAltitude);
            }
            launchedFromSubLevel = true;

            org.joml.Vector3d localPos = new org.joml.Vector3d(spawnX, spawnY, spawnZ);
            org.joml.Vector3d worldPos = new org.joml.Vector3d();
            dev.ryanhcode.sable.Sable.HELPER.projectOutOfSubLevel(level, localPos, worldPos);
            position = new Vec3(worldPos.x, worldPos.y, worldPos.z);

            Object pose = subLevel.getClass().getMethod("logicalPose").invoke(subLevel);
            if (pose != null) {
                Object orientation = pose.getClass().getMethod("orientation").invoke(pose);
                if (orientation instanceof org.joml.Quaterniondc quaternion) {
                    org.joml.Vector3d directionVector = new org.joml.Vector3d(dirX, dirY, dirZ);
                    quaternion.transform(directionVector);
                    direction = new Vec3(directionVector.x, directionVector.y, directionVector.z);
                }
            }

            if (subLevel instanceof dev.ryanhcode.sable.sublevel.ServerSubLevel serverSubLevel && serverSubLevel.latestLinearVelocity != null) {
                inheritedVelocity = new Vec3(
                    serverSubLevel.latestLinearVelocity.x() * SUBLEVEL_VELOCITY_TO_TICK_SCALE,
                    serverSubLevel.latestLinearVelocity.y() * SUBLEVEL_VELOCITY_TO_TICK_SCALE,
                    serverSubLevel.latestLinearVelocity.z() * SUBLEVEL_VELOCITY_TO_TICK_SCALE
                );
            }
        } catch (Throwable ignored) {
        }

        radioAltitude = getRadioAltitude(collisionLevel, position);
        return new LaunchTransform(position, direction, inheritedVelocity, launchedFromSubLevel, radioAltitude);
    }

    public static void applyInheritedVelocity(Projectile projectile, Vec3 inheritedVelocity) {
        if (projectile == null || inheritedVelocity == null || inheritedVelocity.lengthSqr() < 1.0E-8) {
            return;
        }
        projectile.setDeltaMovement(projectile.getDeltaMovement().add(inheritedVelocity));
    }

    public static double getRadioAltitude(ServerLevel level, Vec3 worldPosition) {
        ServerLevel collisionLevel = resolveCollisionLevel(level);
        int x = (int) Math.floor(worldPosition.x);
        int z = (int) Math.floor(worldPosition.z);
        int startY = (int) Math.floor(worldPosition.y - 0.001);
        int minY = collisionLevel.getMinBuildHeight();

        for (int scanY = startY; scanY >= minY; scanY--) {
            BlockPos samplePos = new BlockPos(x, scanY, z);
            BlockState sampleState = collisionLevel.getBlockState(samplePos);
            if (sampleState.isAir()) {
                continue;
            }
            if (!sampleState.getCollisionShape(collisionLevel, samplePos).isEmpty()) {
                return worldPosition.y - (scanY + 1.0);
            }
        }

        return worldPosition.y - minY;
    }

    private static ServerLevel resolveCollisionLevel(ServerLevel level) {
        if (level.getServer() == null) {
            return level;
        }

        ServerLevel sameDimension = level.getServer().getLevel(level.dimension());
        if (sameDimension != null && sameDimension != level) {
            return sameDimension;
        }

        ServerLevel overworld = level.getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (overworld != null && overworld != level) {
            return overworld;
        }

        return level;
    }
}
