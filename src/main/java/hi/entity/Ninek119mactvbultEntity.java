
package hi.entity;

import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;

import hi.procedures.DsfsdsfPriObnovlieniiTikaProcedure;
import hi.procedures.CsdsKoghdaSnariadPopadaietVBlokProcedure;

import hi.init.CreateTheAirWarsModEntities;
import hi.init.CreateTheAirWarsModBlocks;
import hi.init.CreateTheAirWarsModParticleTypes;
import hi.util.ExplosionUtils;
import hi.util.ProjectileChunkLoader;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class Ninek119mactvbultEntity extends AbstractArrow implements ItemSupplier, RocketExplosionCarrier {
	@Override
	protected net.minecraft.world.item.ItemStack getDefaultPickupItem() { return net.minecraft.world.item.ItemStack.EMPTY; }
	public static final ItemStack PROJECTILE_ITEM = new ItemStack(CreateTheAirWarsModBlocks.NINEK_119MACTV.get());

	

	public Ninek119mactvbultEntity(EntityType<? extends Ninek119mactvbultEntity> type, Level world) {
		super(type, world);
	}

	public Ninek119mactvbultEntity(EntityType<? extends Ninek119mactvbultEntity> type, double x, double y, double z, Level world) {
		super(type, x, y, z, world, net.minecraft.world.item.ItemStack.EMPTY, net.minecraft.world.item.ItemStack.EMPTY);
	}

	public Ninek119mactvbultEntity(EntityType<? extends Ninek119mactvbultEntity> type, LivingEntity entity, Level world) {
		super(type, entity.getX(), entity.getEyeY() - 0.1, entity.getZ(), world, net.minecraft.world.item.ItemStack.EMPTY, net.minecraft.world.item.ItemStack.EMPTY);
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
		CsdsKoghdaSnariadPopadaietVBlokProcedure.execute(this.level(), blockHitResult.getLocation().x, blockHitResult.getLocation().y, blockHitResult.getLocation().z, this);
		ProjectileChunkLoader.release(this);
		this.discard();
	}

	@Override
	public void tick() {
		if (!this.level().isClientSide) {
			ProjectileChunkLoader.update(this);
		}
		super.tick();
		DsfsdsfPriObnovlieniiTikaProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ());
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

	public static Ninek119mactvbultEntity shoot(Level world, LivingEntity entity, RandomSource source) {
		return shoot(world, entity, source, 1f, 5, 5);
	}

	public static Ninek119mactvbultEntity shoot(Level world, LivingEntity entity, RandomSource source, float pullingPower) {
		return shoot(world, entity, source, pullingPower * 1f, 5, 5);
	}

	public static Ninek119mactvbultEntity shoot(Level world, LivingEntity entity, RandomSource random, float power, double damage, int knockback) {
		Ninek119mactvbultEntity entityarrow = new Ninek119mactvbultEntity(CreateTheAirWarsModEntities.NINEK_119MACTVBULT.get(), entity, world);
		entityarrow.shoot(entity.getViewVector(1).x, entity.getViewVector(1).y, entity.getViewVector(1).z, power * 2, 0);
		entityarrow.setSilent(true);
		entityarrow.setCritArrow(false);
		entityarrow.setBaseDamage(damage);
		// // removed knockback
		world.addFreshEntity(entityarrow);
		world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.parse("entity.arrow.shoot")), SoundSource.PLAYERS, 1, 1f / (random.nextFloat() * 0.5f + 1) + (power / 2));
		return entityarrow;
	}

	public static Ninek119mactvbultEntity shoot(LivingEntity entity, LivingEntity target) {
		Ninek119mactvbultEntity entityarrow = new Ninek119mactvbultEntity(CreateTheAirWarsModEntities.NINEK_119MACTVBULT.get(), entity, entity.level());
		double dx = target.getX() - entity.getX();
		double dy = target.getY() + target.getEyeHeight() - 1.1;
		double dz = target.getZ() - entity.getZ();
		entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * 0.2F, dz, 1f * 2, 12.0F);
		entityarrow.setSilent(true);
		entityarrow.setBaseDamage(5);
		// removed knockback
		entityarrow.setCritArrow(false);
		entity.level().addFreshEntity(entityarrow);
		entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.parse("entity.arrow.shoot")), SoundSource.PLAYERS, 1, 1f / (RandomSource.create().nextFloat() * 0.5f + 1));
		return entityarrow;
	}

	@Override
	public ExplosionUtils.ProjectileExplosionProfile getExplosionProfile() {
		return new ExplosionUtils.ProjectileExplosionProfile(
			3f, true, 1.0, "create_the_air_wars:vzrivc-8", SoundSource.NEUTRAL, 2f, 1f,
			CreateTheAirWarsModParticleTypes.EXLOSION.get(), 6, 1.5, 1.5, 1.5, 0.4, 3, false
		);
	}
}
