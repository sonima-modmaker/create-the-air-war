package hi.procedures;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import hi.init.CreateTheAirWarsModEntities;

import hi.entity.C25Entity;

public class GdffgdgdgKoghdaSushchnostKhoditPoBlokuProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (!(world instanceof Level _level))
			return;
		if (_level.isClientSide())
			return;
		{
			BlockPos _bp = BlockPos.containing(x, y, z);
			world.setBlock(_bp, Blocks.AIR.defaultBlockState(), 3);
		}
		if (world instanceof ServerLevel projectileLevel) {
			Projectile _entityToSpawn = new Object() {
				public Projectile getArrow(Level level, float damage, int knockback, byte piercing) {
					AbstractArrow entityToSpawn = new C25Entity(CreateTheAirWarsModEntities.C_25.get(), level);
					entityToSpawn.setBaseDamage(damage);
					// removed
					entityToSpawn.setSilent(true);
					//entityToSpawn.setPierceLevel(piercing);
					return entityToSpawn;
				}
			}.getArrow(projectileLevel, 500, 3, (byte) 3);
			_entityToSpawn.setPos(x, y, z);
			if (entity != null && _entityToSpawn instanceof C25Entity c25Entity) {
				c25Entity.setOzmTarget(entity.getY() + 0.1);
			}
			_entityToSpawn.setNoGravity(true);
			_entityToSpawn.shoot(0, 2, 0, (float) 0.6, 0);
					if (world instanceof net.minecraft.world.level.Level _lvl) _lvl.addFreshEntity(_entityToSpawn);
		}
		_level.playSound(null, BlockPos.containing(x, y, z), net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:greanateactv")), SoundSource.NEUTRAL, 1, 1);
	}
}
