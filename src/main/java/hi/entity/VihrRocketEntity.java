package hi.entity;

import hi.init.CreateTheAirWarsModItems;
import hi.init.CreateTheAirWarsModParticleTypes;
import hi.util.ExplosionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class VihrRocketEntity extends AbstractArrow implements ItemSupplier, RocketExplosionCarrier {
    private static final EntityDataAccessor<Boolean> HAS_LAUNCH_START = SynchedEntityData.defineId(VihrRocketEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> LAUNCH_START_X = SynchedEntityData.defineId(VihrRocketEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LAUNCH_START_Y = SynchedEntityData.defineId(VihrRocketEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> LAUNCH_START_Z = SynchedEntityData.defineId(VihrRocketEntity.class, EntityDataSerializers.FLOAT);
    public static final ItemStack PROJECTILE_ITEM = new ItemStack(CreateTheAirWarsModItems.VIHR_ROCKET.get());
    private static final int LAUNCH_GRACE_TICKS = 3;
    private static final int GUIDANCE_DELAY_TICKS = 6;
    private static final int SPEED_RAMP_TICKS = 20;
    private static final double LASER_MIN_PROGRESS = 10.0D;
    private static final double LASER_FORWARD_SPEED = 2.85D;
    public static final double INITIAL_FORWARD_SPEED = LASER_FORWARD_SPEED / 5.0D;
    private static final double LASER_ERROR_GAIN = 0.42D;
    private static final double LASER_DAMPING = 0.74D;
    private static final double LASER_MAX_CORRECTION = 1.25D;
    private static final double LASER_ORBIT_RADIUS = 0.38D;
    private static final double LASER_ORBIT_SPEED = 0.34D;
    private static final double MIN_GUIDED_SPEED = 2.2D;
    private static final double MAX_GUIDED_SPEED = 3.35D;
    private static final double LAUNCH_REGION_XZ = 0.95D;
    private static final double LAUNCH_REGION_Y = 0.6D;
    private static final float FINAL_SPIN_DEGREES_PER_TICK = 32.0F;
    private static final float SPIN_UP_TICKS = 2.0F;
    private static final float WING_DEPLOY_DELAY_TICKS = 4.0F;
    private static final float WING_CLOSED_ANGLE = 62.5F;
    private static final float WING_OPEN_ANGLE = 37.5F;
    private static final float MODEL_CENTER = 8.0F;
    private static final float MODEL_SCALE = 1.0F / 16.0F;
    private static final double THRUSTER_PARTICLE_PUSH = 0.28D;
    private static final double THRUSTER_PARTICLE_INHERIT = 0.08D;
    private static final Vec3[] THRUSTER_ORIGINS = new Vec3[] {
        new Vec3(7.0D, 8.0D, 22.0D),
        new Vec3(9.0D, 8.0D, 22.0D)
    };
    private static final float[] THRUSTER_YAW_OFFSETS = new float[] {-10.0F, 10.0F};

    @Nullable
    private BlockPos cameraPos;
    @Nullable
    private Vec3 initialDirection;
    @Nullable
    private Vec3 launchStartPos;
    private int launchExitTick = -1;

    public VihrRocketEntity(EntityType<? extends VihrRocketEntity> type, Level world) {
        super(type, world);
    }

    public VihrRocketEntity(EntityType<? extends VihrRocketEntity> type, double x, double y, double z, Level world) {
        super(type, x, y, z, world, ItemStack.EMPTY, new ItemStack(net.minecraft.world.item.Items.BOW));
    }

    public VihrRocketEntity(EntityType<? extends VihrRocketEntity> type, LivingEntity entity, Level world) {
        super(type, entity.getX(), entity.getEyeY() - 0.1D, entity.getZ(), world, ItemStack.EMPTY, new ItemStack(net.minecraft.world.item.Items.BOW));
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getItem() {
        return PROJECTILE_ITEM;
    }

    @Override
    protected ItemStack getPickupItem() {
        return PROJECTILE_ITEM;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAS_LAUNCH_START, false);
        builder.define(LAUNCH_START_X, 0.0F);
        builder.define(LAUNCH_START_Y, 0.0F);
        builder.define(LAUNCH_START_Z, 0.0F);
    }

    public void setCameraPos(BlockPos cameraPos) {
        this.cameraPos = cameraPos != null ? cameraPos.immutable() : null;
    }

    public void setInitialDirection(Vec3 initialDirection) {
        this.initialDirection = initialDirection != null && initialDirection.lengthSqr() > 1.0E-6D ? initialDirection.normalize() : null;
    }

    public void setLaunchStartPos(Vec3 launchStartPos) {
        this.launchStartPos = launchStartPos;
        this.entityData.set(HAS_LAUNCH_START, launchStartPos != null);
        if (launchStartPos != null) {
            this.entityData.set(LAUNCH_START_X, (float) launchStartPos.x);
            this.entityData.set(LAUNCH_START_Y, (float) launchStartPos.y);
            this.entityData.set(LAUNCH_START_Z, (float) launchStartPos.z);
        }
    }

    public void refreshOrientation() {
        Vec3 velocity = this.getDeltaMovement();
        if (velocity.lengthSqr() < 1.0E-6D) {
            return;
        }
        double horizontal = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        float yaw = (float) Math.toDegrees(Math.atan2(velocity.x, velocity.z));
        float pitch = (float) Math.toDegrees(Math.atan2(velocity.y, Math.max(horizontal, 1.0E-4D)));
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yRotO = yaw;
        this.xRotO = pitch;
    }

    public float getVisualYaw(float partialTicks) {
        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-6D) {
            return (float) Math.toDegrees(Math.atan2(motion.x, motion.z));
        }
        return Mth.rotLerp(partialTicks, this.yRotO, this.getYRot());
    }

    public float getVisualPitch(float partialTicks) {
        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-6D) {
            double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            return (float) Math.toDegrees(Math.atan2(motion.y, Math.max(horizontal, 1.0E-4D)));
        }
        return Mth.rotLerp(partialTicks, this.xRotO, this.getXRot());
    }

    public float getVisualRollDegrees(float partialTicks) {
        float ticksSinceExit = this.getTicksSinceLaunchExit(partialTicks);
        if (ticksSinceExit < 0.0F) {
            return 0.0F;
        }
        if (ticksSinceExit <= SPIN_UP_TICKS) {
            return FINAL_SPIN_DEGREES_PER_TICK * ticksSinceExit * ticksSinceExit / (2.0F * SPIN_UP_TICKS);
        }
        return FINAL_SPIN_DEGREES_PER_TICK * (ticksSinceExit - (SPIN_UP_TICKS * 0.5F));
    }

    public float getWingAngleDegrees(float partialTicks) {
        float ticksSinceExit = this.getTicksSinceLaunchExit(partialTicks);
        if (ticksSinceExit < 0.0F) {
            return WING_CLOSED_ANGLE;
        }
        float progress = Mth.clamp(ticksSinceExit - WING_DEPLOY_DELAY_TICKS, 0.0F, 1.0F);
        return Mth.lerp(progress, WING_CLOSED_ANGLE, WING_OPEN_ANGLE);
    }

    public float getTicksSinceLaunchExit(float partialTicks) {
        if (this.launchExitTick < 0) {
            return -1.0F;
        }
        return Math.max(0.0F, (this.tickCount - this.launchExitTick) + partialTicks);
    }

    @Override
    public ExplosionUtils.ProjectileExplosionProfile getExplosionProfile() {
        return new ExplosionUtils.ProjectileExplosionProfile(
            3.0F, true, 0.8D, "create_the_air_wars:shellexp2", SoundSource.HOSTILE, 3.4F, 1.08F,
            hi.init.CreateTheAirWarsModParticleTypes.EXLOSION.get(), 4, 0.7D, 0.7D, 0.7D, 0.16D, 2, false
        );
    }

    @Override
    public void tick() {
        this.setNoGravity(true);
        this.noPhysics = this.tickCount <= LAUNCH_GRACE_TICKS;
        if (!this.level().isClientSide) {
            Vec3 current = this.getDeltaMovement();
            Vec3 straightDir = this.initialDirection != null && this.initialDirection.lengthSqr() > 1.0E-6D
                ? this.initialDirection
                : (current.lengthSqr() > 1.0E-6D ? current.normalize() : new Vec3(0.0D, 0.0D, 1.0D));
            double rampedForwardSpeed = this.getRampedForwardSpeed();

            if (this.tickCount <= GUIDANCE_DELAY_TICKS) {
                double straightSpeed = Math.max(rampedForwardSpeed, current.dot(straightDir));
                this.setDeltaMovement(straightDir.scale(straightSpeed));
                this.refreshOrientation();
            } else if (this.cameraPos != null && this.level().getBlockEntity(this.cameraPos) instanceof hi.block.entity.CameraBlockEntity camera) {
                Vec3 rayOrigin = camera.getWorldEyePosition();
                Vec3 rayDirection = camera.getWorldLookDirection();
                if (rayDirection.lengthSqr() > 1.0E-6D) {
                    rayDirection = rayDirection.normalize();
                    Vec3 missilePos = this.position();
                    double alongLaser = Math.max(LASER_MIN_PROGRESS, missilePos.subtract(rayOrigin).dot(rayDirection));
                    Vec3 closestLaserPoint = rayOrigin.add(rayDirection.scale(alongLaser));
                    Vec3 orbitReference = Math.abs(rayDirection.y) > 0.92D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
                    Vec3 orbitAxisA = rayDirection.cross(orbitReference);
                    if (orbitAxisA.lengthSqr() < 1.0E-6D) {
                        orbitAxisA = rayDirection.cross(new Vec3(0.0D, 0.0D, 1.0D));
                    }
                    orbitAxisA = orbitAxisA.normalize();
                    Vec3 orbitAxisB = rayDirection.cross(orbitAxisA).normalize();
                    double orbitPhase = this.tickCount * LASER_ORBIT_SPEED;
                    Vec3 orbitOffset = orbitAxisA.scale(Math.cos(orbitPhase) * LASER_ORBIT_RADIUS)
                        .add(orbitAxisB.scale(Math.sin(orbitPhase) * LASER_ORBIT_RADIUS));
                    Vec3 orbitPoint = closestLaserPoint.add(orbitOffset);
                    Vec3 beamError = orbitPoint.subtract(missilePos);
                    Vec3 forwardVelocity = rayDirection.scale(Math.max(rampedForwardSpeed, current.dot(rayDirection) + 0.12D));
                    Vec3 lateralVelocity = current.subtract(rayDirection.scale(current.dot(rayDirection)));
                    Vec3 correctionVelocity = beamError.scale(LASER_ERROR_GAIN).subtract(lateralVelocity.scale(LASER_DAMPING));
                    if (correctionVelocity.lengthSqr() > LASER_MAX_CORRECTION * LASER_MAX_CORRECTION) {
                        correctionVelocity = correctionVelocity.normalize().scale(LASER_MAX_CORRECTION);
                    }
                    Vec3 desiredVelocity = forwardVelocity.add(correctionVelocity);
                    if (desiredVelocity.lengthSqr() > 1.0E-6D) {
                        Vec3 desiredDir = desiredVelocity.normalize();
                        Vec3 currentDir = current.lengthSqr() > 1.0E-6D ? current.normalize() : straightDir;
                        double minGuidedSpeed = Math.min(rampedForwardSpeed, MIN_GUIDED_SPEED);
                        double currentSpeed = Mth.clamp(Math.max(rampedForwardSpeed, current.length() * 1.01D), minGuidedSpeed, MAX_GUIDED_SPEED);
                        Vec3 steered = currentDir.lerp(desiredDir, 0.34D).normalize();
                        Vec3 wobble = new Vec3(
                            Math.sin(this.tickCount * 0.41D) * 0.006D,
                            Math.cos(this.tickCount * 0.33D) * 0.004D,
                            Math.sin(this.tickCount * 0.29D + 0.6D) * 0.006D
                        );
                        this.setDeltaMovement(steered.scale(currentSpeed).add(wobble));
                        this.refreshOrientation();
                    }
                }
            } else if (current.lengthSqr() > 1.0E-6D) {
                this.setDeltaMovement(current.normalize().scale(Math.max(rampedForwardSpeed, current.length())));
                this.refreshOrientation();
            }
        }
        super.tick();
        this.updateLaunchExitState();

        if (this.inGround && !this.level().isClientSide) {
            this.discard();
        } else if (this.level().isClientSide) {
            this.inGround = false;
            this.spawnThrusterParticles();
        }
    }

    private double getRampedForwardSpeed() {
        double progress = Mth.clamp((double) this.tickCount / SPEED_RAMP_TICKS, 0.0D, 1.0D);
        return Mth.lerp(progress, INITIAL_FORWARD_SPEED, LASER_FORWARD_SPEED);
    }

    private void updateLaunchExitState() {
        Vec3 effectiveLaunchStart = this.getEffectiveLaunchStartPos();
        if (this.launchExitTick >= 0 || effectiveLaunchStart == null) {
            return;
        }
        AABB launchRegion = new AABB(effectiveLaunchStart, effectiveLaunchStart).inflate(LAUNCH_REGION_XZ, LAUNCH_REGION_Y, LAUNCH_REGION_XZ);
        if (!this.getBoundingBox().intersects(launchRegion)) {
            this.launchExitTick = this.tickCount;
        }
    }

    @Nullable
    private Vec3 getEffectiveLaunchStartPos() {
        if (this.entityData.get(HAS_LAUNCH_START)) {
            return new Vec3(
                this.entityData.get(LAUNCH_START_X),
                this.entityData.get(LAUNCH_START_Y),
                this.entityData.get(LAUNCH_START_Z)
            );
        }
        return this.launchStartPos;
    }

    private void spawnThrusterParticles() {
        if (this.launchExitTick < 0) {
            return;
        }
        if ((this.tickCount & 1) != 0) {
            return;
        }
        float yaw = this.getVisualYaw(0.0F);
        float pitch = this.getVisualPitch(0.0F);
        float roll = this.getVisualRollDegrees(0.0F);
        Vec3 entityVelocity = this.getDeltaMovement();
        for (int i = 0; i < THRUSTER_ORIGINS.length; i++) {
            Vec3 positionOffset = this.getThrusterPositionOffset(THRUSTER_ORIGINS[i], THRUSTER_YAW_OFFSETS[i], yaw, pitch, roll);
            Vec3 direction = this.getThrusterDirection(THRUSTER_YAW_OFFSETS[i], yaw, pitch, roll);
            Vec3 spawnPos = this.position().add(positionOffset).add(direction.scale(0.12D));
            Vec3 particleVelocity = direction.scale(THRUSTER_PARTICLE_PUSH)
                .add(entityVelocity.scale(THRUSTER_PARTICLE_INHERIT))
                .add(
                    (this.random.nextDouble() - 0.5D) * 0.006D,
                    (this.random.nextDouble() - 0.5D) * 0.006D,
                    (this.random.nextDouble() - 0.5D) * 0.006D
                );
            this.level().addParticle(
                CreateTheAirWarsModParticleTypes.VIHR_THRUSTER_SMOKE.get(),
                spawnPos.x,
                spawnPos.y,
                spawnPos.z,
                particleVelocity.x,
                particleVelocity.y,
                particleVelocity.z
            );
        }
    }

    private Vec3 getThrusterPositionOffset(Vec3 thrusterOrigin, float thrusterYawOffset, float bodyYaw, float bodyPitch, float bodyRoll) {
        Vector3f position = new Vector3f(0.0F, 0.0F, 2.0F);
        position.rotateY((float) Math.toRadians(thrusterYawOffset));
        position.add((float) thrusterOrigin.x, (float) thrusterOrigin.y, (float) thrusterOrigin.z);
        position.sub(MODEL_CENTER, MODEL_CENTER, MODEL_CENTER).mul(MODEL_SCALE);
        position.rotateY((float) Math.toRadians(bodyYaw + 180.0F));
        position.rotateX((float) Math.toRadians(bodyPitch));
        position.rotateZ((float) Math.toRadians(bodyRoll));
        return new Vec3(position.x, position.y, position.z);
    }

    private Vec3 getThrusterDirection(float thrusterYawOffset, float bodyYaw, float bodyPitch, float bodyRoll) {
        Vector3f direction = new Vector3f(0.0F, 0.0F, 1.0F);
        direction.rotateY((float) Math.toRadians(thrusterYawOffset));
        direction.rotateY((float) Math.toRadians(bodyYaw + 180.0F));
        direction.rotateX((float) Math.toRadians(bodyPitch));
        direction.rotateZ((float) Math.toRadians(bodyRoll));
        direction.normalize();
        return new Vec3(direction.x, direction.y, direction.z);
    }

    @Override
    public void onHitBlock(BlockHitResult blockHitResult) {
        if (this.level().isClientSide) {
            this.inGround = false;
            return;
        }
        if (this.tickCount <= LAUNCH_GRACE_TICKS) {
            this.inGround = false;
            return;
        }
        this.explodeNow(blockHitResult.getLocation());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) {
            this.inGround = false;
            return;
        }
        this.explodeNow(result.getLocation());
    }

    private void explodeNow(Vec3 pos) {
        ExplosionUtils.explodeProjectileImpact(this.level(), pos.x, pos.y, pos.z, this, this.getExplosionProfile());
        this.discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.cameraPos != null) {
            tag.putInt("CameraX", this.cameraPos.getX());
            tag.putInt("CameraY", this.cameraPos.getY());
            tag.putInt("CameraZ", this.cameraPos.getZ());
        }
        if (this.initialDirection != null) {
            tag.putDouble("InitialDirectionX", this.initialDirection.x);
            tag.putDouble("InitialDirectionY", this.initialDirection.y);
            tag.putDouble("InitialDirectionZ", this.initialDirection.z);
        }
        if (this.launchStartPos != null) {
            tag.putDouble("LaunchStartX", this.launchStartPos.x);
            tag.putDouble("LaunchStartY", this.launchStartPos.y);
            tag.putDouble("LaunchStartZ", this.launchStartPos.z);
        }
        tag.putInt("LaunchExitTick", this.launchExitTick);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("CameraX")) {
            this.cameraPos = new BlockPos(tag.getInt("CameraX"), tag.getInt("CameraY"), tag.getInt("CameraZ"));
        } else {
            this.cameraPos = null;
        }
        if (tag.contains("InitialDirectionX")) {
            this.initialDirection = new Vec3(tag.getDouble("InitialDirectionX"), tag.getDouble("InitialDirectionY"), tag.getDouble("InitialDirectionZ")).normalize();
        } else {
            this.initialDirection = null;
        }
        if (tag.contains("LaunchStartX")) {
            this.setLaunchStartPos(new Vec3(tag.getDouble("LaunchStartX"), tag.getDouble("LaunchStartY"), tag.getDouble("LaunchStartZ")));
        } else {
            this.launchStartPos = null;
            this.entityData.set(HAS_LAUNCH_START, false);
        }
        this.launchExitTick = tag.contains("LaunchExitTick") ? tag.getInt("LaunchExitTick") : -1;
    }
}
