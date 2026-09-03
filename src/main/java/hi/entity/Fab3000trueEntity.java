
package hi.entity;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.util.RandomSource;

import hi.procedures.Fab3000trueKoghdaSnariadPopadaietVBlokProcedure;

import hi.init.CreateTheAirWarsModEntities;
import hi.init.CreateTheAirWarsModBlocks;
import hi.init.CreateTheAirWarsModParticleTypes;
import hi.util.ExplosionUtils;
import hi.util.ProjectileChunkLoader;
import net.minecraft.sounds.SoundSource;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class Fab3000trueEntity extends AbstractArrow implements ItemSupplier, RocketExplosionCarrier {
	@Override
	protected net.minecraft.world.item.ItemStack getDefaultPickupItem() { return net.minecraft.world.item.ItemStack.EMPTY; }
	public static final ItemStack PROJECTILE_ITEM = new ItemStack(CreateTheAirWarsModBlocks.FAB_3000TRUEBLOCK.get());

	

	public Fab3000trueEntity(EntityType<? extends Fab3000trueEntity> type, Level world) {
		super(type, world);
	}

	public Fab3000trueEntity(EntityType<? extends Fab3000trueEntity> type, double x, double y, double z, Level world) {
		super(type, x, y, z, world, net.minecraft.world.item.ItemStack.EMPTY, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOW));
	}

	public Fab3000trueEntity(EntityType<? extends Fab3000trueEntity> type, LivingEntity entity, Level world) {
		super(type, entity.getX(), entity.getEyeY() - 0.1, entity.getZ(), world, net.minecraft.world.item.ItemStack.EMPTY, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOW));
	}

	

	@Override
	@OnlyIn(Dist.CLIENT)
	public ItemStack getItem() {
		return PROJECTILE_ITEM;
	}

	@Override
	protected ItemStack getPickupItem() {
		return PROJECTILE_ITEM;
	}

	@Override
	protected void doPostHurtEffects(LivingEntity entity) {
		super.doPostHurtEffects(entity);
		entity.setArrowCount(entity.getArrowCount() - 1);
	}

	@Override
	public void onHitBlock(BlockHitResult blockHitResult) {
		if (this.level().isClientSide) {
			this.inGround = false;
			return;
		}
		Fab3000trueKoghdaSnariadPopadaietVBlokProcedure.execute(this.level(), blockHitResult.getLocation().x, blockHitResult.getLocation().y, blockHitResult.getLocation().z, this);
		ProjectileChunkLoader.release(this);
		this.discard();
	}

	@Override
	public void tick() {
		if (!this.level().isClientSide) {
			ProjectileChunkLoader.update(this);
		}
		super.tick();
		if (this.inGround) {
			if (this.level().isClientSide) {
				this.inGround = false;
				return;
			}
			ProjectileChunkLoader.release(this);
			this.discard();
		}
	}

	@Override
	public void remove(net.minecraft.world.entity.Entity.RemovalReason reason) {
		if (!this.level().isClientSide) {
			ProjectileChunkLoader.release(this);
		}
		super.remove(reason);
	}

	public static Fab3000trueEntity shoot(Level world, LivingEntity entity, RandomSource source) {
		return shoot(world, entity, source, 1f, 5, 5);
	}

	public static Fab3000trueEntity shoot(Level world, LivingEntity entity, RandomSource source, float pullingPower) {
		return shoot(world, entity, source, pullingPower * 1f, 5, 5);
	}

	public static Fab3000trueEntity shoot(Level world, LivingEntity entity, RandomSource random, float power, double damage, int knockback) {
		Fab3000trueEntity entityarrow = new Fab3000trueEntity(CreateTheAirWarsModEntities.FAB_3000TRUE.get(), entity, world);
		entityarrow.shoot(entity.getViewVector(0).x, entity.getViewVector(0).y, entity.getViewVector(0).z, power * 2, 0);
		entityarrow.setSilent(true);
		entityarrow.setCritArrow(false);
		entityarrow.setBaseDamage(damage);
		// // removed knockback
		world.addFreshEntity(entityarrow);
		return entityarrow;
	}

	public static Fab3000trueEntity shoot(LivingEntity entity, LivingEntity target) {
		Fab3000trueEntity entityarrow = new Fab3000trueEntity(CreateTheAirWarsModEntities.FAB_3000TRUE.get(), entity, entity.level());
		double dx = target.getX() - entity.getX();
		double dy = target.getY() + target.getY();
		double dz = target.getZ() - entity.getZ();
		entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * 0.2F, dz, 1f * 2, 12.0F);
		entityarrow.setSilent(true);
		entityarrow.setBaseDamage(5);
		// removed knockback
		entityarrow.setCritArrow(false);
		entity.level().addFreshEntity(entityarrow);
		return entityarrow;
	}

	@Override
	public ExplosionUtils.ProjectileExplosionProfile getExplosionProfile() {
		return new ExplosionUtils.ProjectileExplosionProfile(
			10f, true, 6.0, "create_the_air_wars:shellexp2", SoundSource.NEUTRAL, 30f, 1f,
			CreateTheAirWarsModParticleTypes.EXLOSION.get(), 6, 1.5, 1.5, 1.5, 0.4, 12, false
		);
	}
}
