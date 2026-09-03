package hi.entity;

import hi.init.CreateTheAirWarsModEntities;
import hi.init.CreateTheAirWarsModItems;
import hi.init.CreateTheAirWarsModParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
public class HeattrapFlareEntity extends AbstractArrow implements ItemSupplier {
    private static final EntityDataAccessor<Boolean> LANDED = SynchedEntityData.defineId(HeattrapFlareEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> LANDED_TICKS = SynchedEntityData.defineId(HeattrapFlareEntity.class, EntityDataSerializers.INT);
    private static final int BURN_TIME_TICKS = 200;
    private static final int FADE_TIME_TICKS = 20;
    private static final int TRAIL_INTERVAL_TICKS = 4;

    public static final ItemStack PROJECTILE_ITEM = new ItemStack(CreateTheAirWarsModItems.HEATTRAP_CHARGE.get());

    public HeattrapFlareEntity(EntityType<? extends HeattrapFlareEntity> type, Level world) {
        super(type, world);
    }

    public HeattrapFlareEntity(EntityType<? extends HeattrapFlareEntity> type, double x, double y, double z, Level world) {
        super(type, x, y, z, world, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    public HeattrapFlareEntity(EntityType<? extends HeattrapFlareEntity> type, LivingEntity entity, Level world) {
        super(type, entity.getX(), entity.getEyeY() - 0.1, entity.getZ(), world, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ItemStack getItem() {
        return PROJECTILE_ITEM;
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LANDED, false);
        builder.define(LANDED_TICKS, 0);
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.EMPTY;
    }

    @Override
    public void onHitBlock(BlockHitResult blockHitResult) {
        settle(blockHitResult.getLocation());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        settle(result.getLocation());
    }

    @Override
    public void tick() {
        if (!isLanded()) {
            this.setNoGravity(false);
            super.tick();
            if (this.inGround) {
                settle(this.position());
            } else {
                Vec3 velocity = this.getDeltaMovement();
                this.setDeltaMovement(velocity.x * 0.994, velocity.y + 0.012, velocity.z * 0.994);
            }
            spawnTrail();
            return;
        }

        this.baseTick();
        this.setNoGravity(true);
        this.inGround = false;
        this.setDeltaMovement(Vec3.ZERO);
        int landedTicks = getLandedTicks() + 1;
        setLandedTicks(landedTicks);
        spawnTrail();
        if (landedTicks >= BURN_TIME_TICKS) {
            this.discard();
        }
    }

    private void spawnTrail() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if ((this.tickCount % TRAIL_INTERVAL_TICKS) != 0) {
            return;
        }
        int count = 1;
        double spread = isLanded() ? 0.025 : 0.04;
        serverLevel.sendParticles(CreateTheAirWarsModParticleTypes.HEAT_TRAP_SMOKE.get(), this.getX(), this.getY() + 0.05, this.getZ(), count, spread, spread, spread, 0.005);
    }

    private void settle(Vec3 location) {
        this.setPos(location.x, location.y + 0.02, location.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.inGround = true;
        setLanded(true);
        setLandedTicks(0);
        if (!this.level().isClientSide) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.CANDLE_EXTINGUISH, SoundSource.NEUTRAL, 0.35F, 1.4F);
        }
    }

    public boolean canAttractAim9x() {
        return this.isAlive() && getLandedTicks() < BURN_TIME_TICKS;
    }

    public boolean isLanded() {
        return this.entityData.get(LANDED);
    }

    private void setLanded(boolean landed) {
        this.entityData.set(LANDED, landed);
    }

    public int getLandedTicks() {
        return this.entityData.get(LANDED_TICKS);
    }

    private void setLandedTicks(int ticks) {
        this.entityData.set(LANDED_TICKS, ticks);
    }

    public float getRenderAlpha(float partialTicks) {
        if (!isLanded()) {
            return 1.0F;
        }
        float fadeStart = BURN_TIME_TICKS - FADE_TIME_TICKS;
        float ticks = getLandedTicks() + partialTicks;
        if (ticks <= fadeStart) {
            return 1.0F;
        }
        return Math.max(0.0F, 1.0F - ((ticks - fadeStart) / FADE_TIME_TICKS));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Landed", isLanded());
        tag.putInt("LandedTicks", getLandedTicks());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setLanded(tag.getBoolean("Landed"));
        setLandedTicks(tag.getInt("LandedTicks"));
    }

    public static HeattrapFlareEntity shoot(Level world, LivingEntity entity, RandomSource source, float power, double damage, int knockback) {
        HeattrapFlareEntity projectile = new HeattrapFlareEntity(CreateTheAirWarsModEntities.HEATTRAP_FLARE.get(), entity, world);
        projectile.shoot(entity.getViewVector(1).x, entity.getViewVector(1).y, entity.getViewVector(1).z, power * 1.8f, 0);
        projectile.setSilent(true);
        world.addFreshEntity(projectile);
        return projectile;
    }
}
