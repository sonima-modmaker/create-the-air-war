package hi.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.Entity;

import hi.init.CreateTheAirWarsModEntities;

import hi.entity.BgghEntity;

public class ChfhfhPriNazhatiiKlavishiProcedure {
	public static void execute(net.minecraft.world.level.LevelAccessor world, Entity entity) {
		if (entity == null)
			return;
		{
			Entity _shootFrom = entity;
			Level projectileLevel = _shootFrom.level();
			if (!projectileLevel.isClientSide()) {
				Projectile _entityToSpawn = new Object() {
					public Projectile getArrow(Level level, float damage, int knockback) {
						AbstractArrow entityToSpawn = new BgghEntity(CreateTheAirWarsModEntities.BGGH.get(), level);
						entityToSpawn.setBaseDamage(damage);
						// removed
						entityToSpawn.setSilent(true);
						entityToSpawn.pickup = AbstractArrow.Pickup.ALLOWED;
						return entityToSpawn;
					}
				}.getArrow(projectileLevel, 5, 1);
				_entityToSpawn.setPos(_shootFrom.getX(), _shootFrom.getEyeY() - 0.1, _shootFrom.getZ());
				_entityToSpawn.shoot(_shootFrom.getLookAngle().x, _shootFrom.getLookAngle().y, _shootFrom.getLookAngle().z, 1, 0);
				if (world instanceof net.minecraft.world.level.Level _level) _level.addFreshEntity(_entityToSpawn);
			}
		}
	}
}
