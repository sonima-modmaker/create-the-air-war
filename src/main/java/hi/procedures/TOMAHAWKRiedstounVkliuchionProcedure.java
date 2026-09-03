package hi.procedures;

import hi.block.entity.TomahawkBlockEntity;
import hi.entity.TomahawkbultEntity;
import hi.init.CreateTheAirWarsModEntities;
import hi.util.ProjectileLaunchHelper;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class TOMAHAWKRiedstounVkliuchionProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, BlockState blockstate) {
		if (!(world instanceof ServerLevel serverLevel)) {
			return;
		}
		launch(serverLevel, BlockPos.containing(x, y, z), null, null);
	}

	public static boolean launch(ServerLevel world, BlockPos pos, BlockPos targetPos, Player sourcePlayer) {
		BlockState blockstate = world.getBlockState(pos);
		if (blockstate.isAir()) {
			return false;
		}

		Direction facing = getDirection(blockstate);
		double dx = 0;
		double dz = 0;
		double dy = 0.4;
		if (facing == Direction.NORTH) {
			dz = -50;
		} else if (facing == Direction.SOUTH) {
			dz = 50;
		} else if (facing == Direction.EAST) {
			dx = 50;
		} else if (facing == Direction.WEST) {
			dx = -50;
		}

		Projectile entityToSpawn = new Object() {
			public Projectile getArrow(Level level, float damage, int knockback) {
				AbstractArrow projectile = new TomahawkbultEntity(CreateTheAirWarsModEntities.TOMAHAWKBULT.get(), level);
				projectile.setBaseDamage(damage);
				projectile.setSilent(true);
				return projectile;
			}
		}.getArrow(world, 40, 6);

		double spawnX = pos.getX() + 0.5;
		double spawnY = pos.getY() + 0.5;
		double spawnZ = pos.getZ() + 0.5;
		TomahawkBlockEntity be = world.getBlockEntity(pos) instanceof TomahawkBlockEntity tomahawkBe ? tomahawkBe : null;
		if (targetPos != null && be != null) {
			Vec3 targetCenter = targetPos.getCenter();
			be.setTarget(targetCenter.x, targetCenter.y, targetCenter.z);
			world.sendBlockUpdated(pos, blockstate, blockstate, 3);
		}

		ProjectileLaunchHelper.LaunchTransform launch = ProjectileLaunchHelper.resolveBlockLaunch(world, pos, spawnX, spawnY, spawnZ, dx, dy, dz);
		entityToSpawn.setPos(launch.position().x, launch.position().y, launch.position().z);
		if (entityToSpawn instanceof TomahawkbultEntity tomahawk) {
			tomahawk.armDelayedActivation(launch.launchedFromSubLevel(), launch.radioAltitude());
			if (be != null && be.hasTarget()) {
				tomahawk.setTarget(be.getTargetX(), be.getTargetY(), be.getTargetZ());
			} else if (targetPos != null) {
				Vec3 targetCenter = targetPos.getCenter();
				tomahawk.setTarget(targetCenter.x, targetCenter.y, targetCenter.z);
			}
		}

		Vec3 inherited = launch.inheritedVelocity();
		Vec3 launchDirection = launch.direction().lengthSqr() > 1.0E-6 ? launch.direction().normalize() : new Vec3(dx, dy, dz).normalize();
		Vec3 startVelocity = launch.radioAltitude() > 20.0 ? inherited : inherited.add(launchDirection.scale(0.35));
		entityToSpawn.setDeltaMovement(startVelocity);
		world.addFreshEntity(entityToSpawn);
		world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		return true;
	}

	private static Direction getDirection(BlockState _bs) {
		Property<?> _prop = _bs.getBlock().getStateDefinition().getProperty("facing");
		if (_prop instanceof DirectionProperty _dp)
			return _bs.getValue(_dp);
		_prop = _bs.getBlock().getStateDefinition().getProperty("axis");
		return _prop instanceof EnumProperty _ep && _ep.getPossibleValues().toArray()[0] instanceof Direction.Axis ? Direction.fromAxisAndDirection((Direction.Axis) _bs.getValue(_ep), Direction.AxisDirection.POSITIVE) : Direction.NORTH;
	}
}
