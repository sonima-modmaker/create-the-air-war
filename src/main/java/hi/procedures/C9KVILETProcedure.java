package hi.procedures;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import hi.init.CreateTheAirWarsModEntities;

import hi.entity.C3ktrueEntity;
import hi.util.ProjectileLaunchHelper;

public class C9KVILETProcedure {
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
						AbstractArrow entityToSpawn = new C3ktrueEntity(CreateTheAirWarsModEntities.C_3KTRUE.get(), level);
						entityToSpawn.setBaseDamage(damage);
						// removed
						entityToSpawn.setSilent(true);
						entityToSpawn.setCritArrow(true);
						entityToSpawn.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
						return entityToSpawn;
					}
				}.getArrow(projectileLevel, 50, 0);
				ProjectileLaunchHelper.LaunchTransform launch = ProjectileLaunchHelper.resolveBlockLaunch(projectileLevel, BlockPos.containing(x, y, z), x + 0.5, y + 0.5, z + 0.5, 0, 0, -50);
				_entityToSpawn.setPos(launch.position().x, launch.position().y, launch.position().z);
				_entityToSpawn.shoot(launch.direction().x, launch.direction().y, launch.direction().z, 4, 0);
				ProjectileLaunchHelper.applyInheritedVelocity(_entityToSpawn, launch.inheritedVelocity());
				if (world instanceof net.minecraft.world.level.Level _level) _level.addFreshEntity(_entityToSpawn);
			}
			if (world instanceof net.minecraft.world.level.Level _level) {
				if (!_level.isClientSide()) {
					_level.playSound(null, BlockPos.containing(x, y, z), net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:vilet")), SoundSource.BLOCKS, 1, 1);
				} else {
					_level.playLocalSound(x, y, z, net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:vilet")), SoundSource.BLOCKS, 1, 1, false);
				}
			}
			world.setBlock(BlockPos.containing(x, y, z), Blocks.AIR.defaultBlockState(), 3);
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
						AbstractArrow entityToSpawn = new C3ktrueEntity(CreateTheAirWarsModEntities.C_3KTRUE.get(), level);
						entityToSpawn.setBaseDamage(damage);
						// removed
						entityToSpawn.setSilent(true);
						entityToSpawn.setCritArrow(true);
						entityToSpawn.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
						return entityToSpawn;
					}
				}.getArrow(projectileLevel, 50, 0);
				ProjectileLaunchHelper.LaunchTransform launch = ProjectileLaunchHelper.resolveBlockLaunch(projectileLevel, BlockPos.containing(x, y, z), x + 0.5, y + 0.5, z + 0.5, 0, 0, 50);
				_entityToSpawn.setPos(launch.position().x, launch.position().y, launch.position().z);
				_entityToSpawn.shoot(launch.direction().x, launch.direction().y, launch.direction().z, 4, 0);
				ProjectileLaunchHelper.applyInheritedVelocity(_entityToSpawn, launch.inheritedVelocity());
				if (world instanceof net.minecraft.world.level.Level _level) _level.addFreshEntity(_entityToSpawn);
			}
			if (world instanceof net.minecraft.world.level.Level _level) {
				if (!_level.isClientSide()) {
					_level.playSound(null, BlockPos.containing(x, y, z), net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:vilet")), SoundSource.BLOCKS, 1, 1);
				} else {
					_level.playLocalSound(x, y, z, net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:vilet")), SoundSource.BLOCKS, 1, 1, false);
				}
			}
			world.setBlock(BlockPos.containing(x, y, z), Blocks.AIR.defaultBlockState(), 3);
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
						AbstractArrow entityToSpawn = new C3ktrueEntity(CreateTheAirWarsModEntities.C_3KTRUE.get(), level);
						entityToSpawn.setBaseDamage(damage);
						// removed
						entityToSpawn.setSilent(true);
						entityToSpawn.setCritArrow(true);
						entityToSpawn.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
						return entityToSpawn;
					}
				}.getArrow(projectileLevel, 50, 0);
				ProjectileLaunchHelper.LaunchTransform launch = ProjectileLaunchHelper.resolveBlockLaunch(projectileLevel, BlockPos.containing(x, y, z), x + 0.5, y + 0.5, z + 0.5, -50, 0, 0);
				_entityToSpawn.setPos(launch.position().x, launch.position().y, launch.position().z);
				_entityToSpawn.shoot(launch.direction().x, launch.direction().y, launch.direction().z, 4, 0);
				ProjectileLaunchHelper.applyInheritedVelocity(_entityToSpawn, launch.inheritedVelocity());
				if (world instanceof net.minecraft.world.level.Level _level) _level.addFreshEntity(_entityToSpawn);
			}
			if (world instanceof net.minecraft.world.level.Level _level) {
				if (!_level.isClientSide()) {
					_level.playSound(null, BlockPos.containing(x, y, z), net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:vilet")), SoundSource.BLOCKS, 1, 1);
				} else {
					_level.playLocalSound(x, y, z, net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:vilet")), SoundSource.BLOCKS, 1, 1, false);
				}
			}
			world.setBlock(BlockPos.containing(x, y, z), Blocks.AIR.defaultBlockState(), 3);
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
						AbstractArrow entityToSpawn = new C3ktrueEntity(CreateTheAirWarsModEntities.C_3KTRUE.get(), level);
						entityToSpawn.setBaseDamage(damage);
						// removed
						entityToSpawn.setSilent(true);
						entityToSpawn.setCritArrow(true);
						entityToSpawn.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
						return entityToSpawn;
					}
				}.getArrow(projectileLevel, 50, 0);
				ProjectileLaunchHelper.LaunchTransform launch = ProjectileLaunchHelper.resolveBlockLaunch(projectileLevel, BlockPos.containing(x, y, z), x + 0.5, y + 0.5, z + 0.5, 50, 0, 0);
				_entityToSpawn.setPos(launch.position().x, launch.position().y, launch.position().z);
				_entityToSpawn.shoot(launch.direction().x, launch.direction().y, launch.direction().z, 4, 0);
				ProjectileLaunchHelper.applyInheritedVelocity(_entityToSpawn, launch.inheritedVelocity());
				if (world instanceof net.minecraft.world.level.Level _level) _level.addFreshEntity(_entityToSpawn);
			}
			if (world instanceof net.minecraft.world.level.Level _level) {
				if (!_level.isClientSide()) {
					_level.playSound(null, BlockPos.containing(x, y, z), net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:vilet")), SoundSource.BLOCKS, 1, 1);
				} else {
					_level.playLocalSound(x, y, z, net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:vilet")), SoundSource.BLOCKS, 1, 1, false);
				}
			}
			world.setBlock(BlockPos.containing(x, y, z), Blocks.AIR.defaultBlockState(), 3);
		}
	}
}
