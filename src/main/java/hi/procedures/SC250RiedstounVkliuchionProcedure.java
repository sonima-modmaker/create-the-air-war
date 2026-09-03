package hi.procedures;

import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;


import java.util.Map;

import hi.init.CreateTheAirWarsModEntities;

import hi.entity.GvrdcrcdEntity;
import hi.util.ProjectileLaunchHelper;

public class SC250RiedstounVkliuchionProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, BlockState blockstate) {
		if ((new Object() {
			public Direction getDirection(BlockState _bs) {
				Property<?> _prop = _bs.getBlock().getStateDefinition().getProperty("facing");
				if (_prop instanceof DirectionProperty _dp)
					return _bs.getValue(_dp);
				_prop = _bs.getBlock().getStateDefinition().getProperty("axis");
				return _prop instanceof EnumProperty _ep && _ep.getPossibleValues().toArray()[0] instanceof Direction.Axis ? Direction.fromAxisAndDirection((Direction.Axis) _bs.getValue(_ep), Direction.AxisDirection.POSITIVE) : Direction.NORTH;
			}
		}.getDirection(blockstate)) == Direction.NORTH) {
			if (world instanceof ServerLevel projectileLevel) {
				Projectile _entityToSpawn = new Object() {
					public Projectile getArrow(Level level, float damage, int knockback) {
						AbstractArrow entityToSpawn = new GvrdcrcdEntity(CreateTheAirWarsModEntities.GVRDCRCD.get(), level);
						entityToSpawn.setBaseDamage(damage);
						// removed
						entityToSpawn.setSilent(true);
						return entityToSpawn;
					}
				}.getArrow(projectileLevel, 150, 10);
				ProjectileLaunchHelper.LaunchTransform launch = ProjectileLaunchHelper.resolveBlockLaunch(projectileLevel, BlockPos.containing(x, y, z), x + 0.5, y, z + 0.5, 0, -1, 0);
				_entityToSpawn.setPos(launch.position().x, launch.position().y, launch.position().z);
				_entityToSpawn.shoot(launch.direction().x, launch.direction().y, launch.direction().z, 0, 0);
				ProjectileLaunchHelper.applyInheritedVelocity(_entityToSpawn, launch.inheritedVelocity());
				if (world instanceof net.minecraft.world.level.Level _level) _level.addFreshEntity(_entityToSpawn);
				////RecoilForceAttachment.addRecoil(world, BlockPos.containing(x, y, z), 0, -1, 0);
			}
			{
				BlockPos _bp = BlockPos.containing(x, y, z);
				BlockState _bs = Blocks.AIR.defaultBlockState();
				BlockState _bso = world.getBlockState(_bp);
				for (Map.Entry<Property<?>, Comparable<?>> entry : _bso.getValues().entrySet()) {
					Property _property = _bs.getBlock().getStateDefinition().getProperty(entry.getKey().getName());
					if (_property != null && _bs.getValue(_property) != null)
						try {
							_bs = _bs.setValue(_property, (Comparable) entry.getValue());
						} catch (Exception e) {
						}
				}
				world.setBlock(_bp, _bs, 3);
			}
		}
		if ((new Object() {
			public Direction getDirection(BlockState _bs) {
				Property<?> _prop = _bs.getBlock().getStateDefinition().getProperty("facing");
				if (_prop instanceof DirectionProperty _dp)
					return _bs.getValue(_dp);
				_prop = _bs.getBlock().getStateDefinition().getProperty("axis");
				return _prop instanceof EnumProperty _ep && _ep.getPossibleValues().toArray()[0] instanceof Direction.Axis ? Direction.fromAxisAndDirection((Direction.Axis) _bs.getValue(_ep), Direction.AxisDirection.POSITIVE) : Direction.NORTH;
			}
		}.getDirection(blockstate)) == Direction.SOUTH) {
			if (world instanceof ServerLevel projectileLevel) {
				Projectile _entityToSpawn = new Object() {
					public Projectile getArrow(Level level, float damage, int knockback) {
						AbstractArrow entityToSpawn = new GvrdcrcdEntity(CreateTheAirWarsModEntities.GVRDCRCD.get(), level);
						entityToSpawn.setBaseDamage(damage);
						// removed
						entityToSpawn.setSilent(true);
						return entityToSpawn;
					}
				}.getArrow(projectileLevel, 150, 10);
				ProjectileLaunchHelper.LaunchTransform launch = ProjectileLaunchHelper.resolveBlockLaunch(projectileLevel, BlockPos.containing(x, y, z), x + 0.5, y, z + 0.5, 0, -1, 0);
				_entityToSpawn.setPos(launch.position().x, launch.position().y, launch.position().z);
				_entityToSpawn.shoot(launch.direction().x, launch.direction().y, launch.direction().z, 0, 0);
				ProjectileLaunchHelper.applyInheritedVelocity(_entityToSpawn, launch.inheritedVelocity());
				if (world instanceof net.minecraft.world.level.Level _level) _level.addFreshEntity(_entityToSpawn);
				////RecoilForceAttachment.addRecoil(world, BlockPos.containing(x, y, z), 0, -1, 0);
			}
			{
				BlockPos _bp = BlockPos.containing(x, y, z);
				BlockState _bs = Blocks.AIR.defaultBlockState();
				BlockState _bso = world.getBlockState(_bp);
				for (Map.Entry<Property<?>, Comparable<?>> entry : _bso.getValues().entrySet()) {
					Property _property = _bs.getBlock().getStateDefinition().getProperty(entry.getKey().getName());
					if (_property != null && _bs.getValue(_property) != null)
						try {
							_bs = _bs.setValue(_property, (Comparable) entry.getValue());
						} catch (Exception e) {
						}
				}
				world.setBlock(_bp, _bs, 3);
			}
		}
		if ((new Object() {
			public Direction getDirection(BlockState _bs) {
				Property<?> _prop = _bs.getBlock().getStateDefinition().getProperty("facing");
				if (_prop instanceof DirectionProperty _dp)
					return _bs.getValue(_dp);
				_prop = _bs.getBlock().getStateDefinition().getProperty("axis");
				return _prop instanceof EnumProperty _ep && _ep.getPossibleValues().toArray()[0] instanceof Direction.Axis ? Direction.fromAxisAndDirection((Direction.Axis) _bs.getValue(_ep), Direction.AxisDirection.POSITIVE) : Direction.NORTH;
			}
		}.getDirection(blockstate)) == Direction.WEST) {
			if (world instanceof ServerLevel projectileLevel) {
				Projectile _entityToSpawn = new Object() {
					public Projectile getArrow(Level level, float damage, int knockback) {
						AbstractArrow entityToSpawn = new GvrdcrcdEntity(CreateTheAirWarsModEntities.GVRDCRCD.get(), level);
						entityToSpawn.setBaseDamage(damage);
						// removed
						entityToSpawn.setSilent(true);
						return entityToSpawn;
					}
				}.getArrow(projectileLevel, 150, 10);
				ProjectileLaunchHelper.LaunchTransform launch = ProjectileLaunchHelper.resolveBlockLaunch(projectileLevel, BlockPos.containing(x, y, z), x + 0.5, y, z + 0.5, 0, -1, 0);
				_entityToSpawn.setPos(launch.position().x, launch.position().y, launch.position().z);
				_entityToSpawn.shoot(launch.direction().x, launch.direction().y, launch.direction().z, 0, 0);
				ProjectileLaunchHelper.applyInheritedVelocity(_entityToSpawn, launch.inheritedVelocity());
				if (world instanceof net.minecraft.world.level.Level _level) _level.addFreshEntity(_entityToSpawn);
				////RecoilForceAttachment.addRecoil(world, BlockPos.containing(x, y, z), 0, -1, 0);
			}
			{
				BlockPos _bp = BlockPos.containing(x, y, z);
				BlockState _bs = Blocks.AIR.defaultBlockState();
				BlockState _bso = world.getBlockState(_bp);
				for (Map.Entry<Property<?>, Comparable<?>> entry : _bso.getValues().entrySet()) {
					Property _property = _bs.getBlock().getStateDefinition().getProperty(entry.getKey().getName());
					if (_property != null && _bs.getValue(_property) != null)
						try {
							_bs = _bs.setValue(_property, (Comparable) entry.getValue());
						} catch (Exception e) {
						}
				}
				world.setBlock(_bp, _bs, 3);
			}
		}
		if ((new Object() {
			public Direction getDirection(BlockState _bs) {
				Property<?> _prop = _bs.getBlock().getStateDefinition().getProperty("facing");
				if (_prop instanceof DirectionProperty _dp)
					return _bs.getValue(_dp);
				_prop = _bs.getBlock().getStateDefinition().getProperty("axis");
				return _prop instanceof EnumProperty _ep && _ep.getPossibleValues().toArray()[0] instanceof Direction.Axis ? Direction.fromAxisAndDirection((Direction.Axis) _bs.getValue(_ep), Direction.AxisDirection.POSITIVE) : Direction.NORTH;
			}
		}.getDirection(blockstate)) == Direction.EAST) {
			if (world instanceof ServerLevel projectileLevel) {
				Projectile _entityToSpawn = new Object() {
					public Projectile getArrow(Level level, float damage, int knockback) {
						AbstractArrow entityToSpawn = new GvrdcrcdEntity(CreateTheAirWarsModEntities.GVRDCRCD.get(), level);
						entityToSpawn.setBaseDamage(damage);
						// removed
						entityToSpawn.setSilent(true);
						return entityToSpawn;
					}
				}.getArrow(projectileLevel, 150, 10);
				ProjectileLaunchHelper.LaunchTransform launch = ProjectileLaunchHelper.resolveBlockLaunch(projectileLevel, BlockPos.containing(x, y, z), x + 0.5, y, z + 0.5, 0, -1, 0);
				_entityToSpawn.setPos(launch.position().x, launch.position().y, launch.position().z);
				_entityToSpawn.shoot(launch.direction().x, launch.direction().y, launch.direction().z, 0, 0);
				ProjectileLaunchHelper.applyInheritedVelocity(_entityToSpawn, launch.inheritedVelocity());
				if (world instanceof net.minecraft.world.level.Level _level) _level.addFreshEntity(_entityToSpawn);
				//RecoilForceAttachment.addRecoil(world, BlockPos.containing(x, y, z), 0, -1, 0);
			}
			{
				BlockPos _bp = BlockPos.containing(x, y, z);
				BlockState _bs = Blocks.AIR.defaultBlockState();
				BlockState _bso = world.getBlockState(_bp);
				for (Map.Entry<Property<?>, Comparable<?>> entry : _bso.getValues().entrySet()) {
					Property _property = _bs.getBlock().getStateDefinition().getProperty(entry.getKey().getName());
					if (_property != null && _bs.getValue(_property) != null)
						try {
							_bs = _bs.setValue(_property, (Comparable) entry.getValue());
						} catch (Exception e) {
						}
				}
				world.setBlock(_bp, _bs, 3);
			}
		}
	}
}
