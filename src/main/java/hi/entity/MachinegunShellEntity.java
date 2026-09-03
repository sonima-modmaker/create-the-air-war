package hi.entity;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;

import hi.init.CreateTheAirWarsModEntities;
import hi.init.CreateTheAirWarsModItems;
import hi.init.CreateTheAirWarsModSounds;
import hi.init.CreateTheAirWarsModParticleTypes;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class MachinegunShellEntity extends AbstractArrow implements ItemSupplier {
	private static final double FRIENDLY_LAUNCH_RADIUS_SQR = 20.0 * 20.0;
	private static final double INHERITED_VELOCITY_FADE = 0.92;
	private static final double INHERITED_VELOCITY_EPSILON_SQR = 0.0025;
	private boolean passbyPlayed;
	private Vec3 launchPosition;
	private Vec3 inheritedLaunchVelocity = Vec3.ZERO;


	@Override
	protected net.minecraft.world.item.ItemStack getDefaultPickupItem() { return net.minecraft.world.item.ItemStack.EMPTY; }
	public static final ItemStack PROJECTILE_ITEM = new ItemStack(CreateTheAirWarsModItems.SHELL.get());

	

	public MachinegunShellEntity(EntityType<? extends MachinegunShellEntity> type, Level world) {
		super(type, world);
	}

	public MachinegunShellEntity(EntityType<? extends MachinegunShellEntity> type, double x, double y, double z, Level world) {
		super(type, x, y, z, world, net.minecraft.world.item.ItemStack.EMPTY, net.minecraft.world.item.ItemStack.EMPTY);
	}

	public MachinegunShellEntity(EntityType<? extends MachinegunShellEntity> type, LivingEntity entity, Level world) {
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
	protected net.minecraft.sounds.SoundEvent getDefaultHitGroundSoundEvent() {
		return net.minecraft.sounds.SoundEvents.EMPTY;
	}

	@Override
	public void onHitBlock(BlockHitResult blockHitResult) {
		super.onHitBlock(blockHitResult);
		if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
			// Восстановлено пробитие (разрушение) блоков без выпадения предметов для сохранения TPS
			BlockState state = this.level().getBlockState(blockHitResult.getBlockPos());
			if (!state.isAir() && state.getDestroySpeed(this.level(), blockHitResult.getBlockPos()) >= 0 && this.random.nextFloat() < 0.25f) {
				this.level().destroyBlock(blockHitResult.getBlockPos(), false);
			}
			if (this.random.nextFloat() < 0.25f) {
				serverLevel.sendParticles(CreateTheAirWarsModParticleTypes.MACHINEGUN_SMOKE.get(), this.getX(), this.getY(), this.getZ(), 2, 0.1, 0.1, 0.1, 0.01);
			}
		}
		this.discard();
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		this.discard();
	}

	@Override
	public void tick() {
		this.setNoGravity(true);
		super.tick();
		if (this.tickCount > 100 || !this.level().isLoaded(this.blockPosition())) {
			this.discard();
			return;
		}
		if (!this.level().isClientSide) {
			Vec3 vel = this.getDeltaMovement();
			Vec3 inherited = getActiveInheritedLaunchVelocity();
			Vec3 ballistic = vel.subtract(inherited);
			this.setDeltaMovement(ballistic.x * 0.99 + inherited.x, ballistic.y * 0.99 - 0.01 + inherited.y, ballistic.z * 0.99 + inherited.z);
		}
		if (!this.level().isClientSide && !this.passbyPlayed) {
			Vec3 vel = this.getDeltaMovement();
			double velLen = vel.length();
			if (velLen > 0.05) {
				Vec3 velDir = vel.scale(1.0 / velLen);
				for (Player player : this.level().players()) {
					if (player == this.getOwner()) continue;
					Vec3 toPlayer = player.position().add(0, player.getEyeHeight() * 0.5, 0).subtract(this.position());
					double along = toPlayer.dot(velDir);
					double lateral = toPlayer.subtract(velDir.scale(along)).length();
					if (along > 0 && along <= 16 && lateral <= 8) {
						this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
								CreateTheAirWarsModSounds.BULLET_PASSBY.get(), SoundSource.NEUTRAL, 3.0f, 0.9f + this.random.nextFloat() * 0.2f);
						this.passbyPlayed = true;
						break;
					}
				}
			}
		}
		if (this.inGround)
			this.discard();
	}

	public static MachinegunShellEntity shoot(Level world, LivingEntity entity, RandomSource source, float power, double damage, int knockback) {
		MachinegunShellEntity entityarrow = new MachinegunShellEntity(CreateTheAirWarsModEntities.MACHINEGUN_SHELL.get(), entity, world);
		entityarrow.shoot(entity.getViewVector(1).x, entity.getViewVector(1).y, entity.getViewVector(1).z, power * 2, 0);
		entityarrow.setSilent(true);
		entityarrow.setCritArrow(false);
		entityarrow.setBaseDamage(damage);
		// // removed knockback
		world.addFreshEntity(entityarrow);
		return entityarrow;
	}

	public void setLaunchContext(Vec3 launchPosition, Vec3 inheritedVelocity) {
		this.launchPosition = launchPosition;
		this.inheritedLaunchVelocity = inheritedVelocity != null ? inheritedVelocity : Vec3.ZERO;
	}

	private Vec3 getActiveInheritedLaunchVelocity() {
		if (inheritedLaunchVelocity.lengthSqr() <= INHERITED_VELOCITY_EPSILON_SQR) {
			inheritedLaunchVelocity = Vec3.ZERO;
			return Vec3.ZERO;
		}
		if (launchPosition != null && this.position().distanceToSqr(launchPosition) > FRIENDLY_LAUNCH_RADIUS_SQR) {
			inheritedLaunchVelocity = inheritedLaunchVelocity.scale(INHERITED_VELOCITY_FADE);
		}
		return inheritedLaunchVelocity;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putBoolean("PassbyPlayed", this.passbyPlayed);
		if (launchPosition != null) {
			tag.putDouble("LaunchX", launchPosition.x);
			tag.putDouble("LaunchY", launchPosition.y);
			tag.putDouble("LaunchZ", launchPosition.z);
		}
		tag.putDouble("InheritedLaunchVelocityX", inheritedLaunchVelocity.x);
		tag.putDouble("InheritedLaunchVelocityY", inheritedLaunchVelocity.y);
		tag.putDouble("InheritedLaunchVelocityZ", inheritedLaunchVelocity.z);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.passbyPlayed = tag.getBoolean("PassbyPlayed");
		this.launchPosition = tag.contains("LaunchX") ? new Vec3(tag.getDouble("LaunchX"), tag.getDouble("LaunchY"), tag.getDouble("LaunchZ")) : null;
		this.inheritedLaunchVelocity = new Vec3(
			tag.getDouble("InheritedLaunchVelocityX"),
			tag.getDouble("InheritedLaunchVelocityY"),
			tag.getDouble("InheritedLaunchVelocityZ")
		);
	}
}

