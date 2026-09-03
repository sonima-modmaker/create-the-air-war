package hi.procedures;

import hi.entity.Aim9xbultEntity;
import hi.init.CreateTheAirWarsModEntities;
import hi.util.ProjectileLaunchHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;

public class Aim9xRiedstounVkliuchionProcedure {
    public static void execute(LevelAccessor world, double x, double y, double z, BlockState blockstate) {
        execute(world, x, y, z, blockstate, null, null, null);
    }

    public static void execute(LevelAccessor world, double x, double y, double z, BlockState blockstate, UUID targetSubLevelId) {
        execute(world, x, y, z, blockstate, targetSubLevelId, null, null);
    }

    public static void execute(LevelAccessor world, double x, double y, double z, BlockState blockstate, UUID targetSubLevelId, UUID targetRocketId, UUID targetHeatTrapId) {
        Direction facing = getDirection(blockstate);
        double dx = 0;
        double dz = 0;
        if (facing == Direction.NORTH) {
            dz = -50;
        } else if (facing == Direction.SOUTH) {
            dz = 50;
        } else if (facing == Direction.EAST) {
            dx = 50;
        } else if (facing == Direction.WEST) {
            dx = -50;
        }

        if (world instanceof ServerLevel projectileLevel) {
            Projectile projectile = new Object() {
                public Projectile getArrow(Level level, float damage, int knockback) {
                    Aim9xbultEntity entityToSpawn = new Aim9xbultEntity(CreateTheAirWarsModEntities.AIM9XBULT.get(), level);
                    entityToSpawn.setBaseDamage(damage);
                    entityToSpawn.setSilent(true);
                    if (targetSubLevelId != null) {
                        entityToSpawn.setTargetSubLevel(targetSubLevelId);
                    }
                    if (targetRocketId != null) {
                        entityToSpawn.setTargetRocket(targetRocketId);
                    }
                    if (targetHeatTrapId != null) {
                        entityToSpawn.setTargetHeatTrap(targetHeatTrapId);
                    }
                    return entityToSpawn;
                }
            }.getArrow(projectileLevel, 200, 1);

            ProjectileLaunchHelper.LaunchTransform launch = ProjectileLaunchHelper.resolveBlockLaunch(projectileLevel, BlockPos.containing(x, y, z), x + 0.5, y + 0.5, z + 0.5, dx, 0, dz);
            projectile.setPos(launch.position().x, launch.position().y, launch.position().z);
            Vec3 launchDirection = launch.direction().lengthSqr() > 1.0E-6 ? launch.direction().normalize() : new Vec3(dx, 0, dz).normalize();
            Vec3 initialVelocity = launch.inheritedVelocity().add(launchDirection.scale(1.15));
            if (projectile instanceof Aim9xbultEntity aim9) {
                aim9.setLaunchContext(launch.position(), launch.inheritedVelocity(), resolveLaunchSubLevelId(projectileLevel, BlockPos.containing(x, y, z)));
            }
            projectile.setNoGravity(true);
            projectile.setDeltaMovement(initialVelocity);
            if (world instanceof net.minecraft.world.level.Level level) {
                level.addFreshEntity(projectile);
            }
        }
    }

    private static UUID resolveLaunchSubLevelId(ServerLevel level, BlockPos sourcePos) {
        try {
            dev.ryanhcode.sable.sublevel.SubLevel subLevel = dev.ryanhcode.sable.Sable.HELPER.getContaining(level, sourcePos);
            return subLevel != null ? subLevel.getUniqueId() : null;
        } catch (Throwable ignored) {
            return null;
        }
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
