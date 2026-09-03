package hi.entity;

import hi.init.CreateTheAirWarsModItems;
import hi.init.CreateTheAirWarsModParticleTypes;
import hi.util.ExplosionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import java.util.UUID;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class C75RocketEntity extends AbstractArrow implements ItemSupplier, RocketExplosionCarrier {
    private static final int LAUNCH_GRACE_TICKS = 3;
    private static final int BOOST_TICKS = 18;
    private static final double BOOST_CLIMB_BIAS = 0.92D;
    private static final double BOOST_END_SPEED = 5.5D;
    private static final double GLIDE_MIN_SPEED = 3.6D;
    private static final double GLIDE_DAMPING = 0.998D;
    private static final double GLIDE_STEER = 0.18D;
    private static final double TARGET_PULL = 0.25D;
    private static final float MODEL_CENTER = 8.0F;
    private static final float MODEL_SCALE = 1.0F / 16.0F;
    private static final double THRUSTER_PARTICLE_PUSH = 0.45D;
    private static final double THRUSTER_PARTICLE_INHERIT = 0.1D;
    
    // Customize thruster origins for C-75 to be at the tail nozzle (Z = 37.0D)
    private static final Vec3[] THRUSTER_ORIGINS = new Vec3[] {
        new Vec3(8.0D, 8.0D, 37.0D) // Single centered thruster
    };
    private static final float[] THRUSTER_YAW_OFFSETS = new float[] {0.0F};
    
    public static final double INITIAL_FORWARD_SPEED = 2.5D;

    @Nullable
    private BlockPos cameraPos;
    @Nullable
    private Vec3 initialDirection;
    @Nullable
    private Vec3 launchStartPos;
    @Nullable
    private UUID targetUUID;
    @Nullable
    private Entity lockedTarget;
    @Nullable
    private UUID targetSubLevelId;
    private boolean targetLost = false;
    private int targetLossTicks = 0;
    private int noTargetTicks = 0;

    public void setTargetEntity(Entity target) {
        this.lockedTarget = target;
        this.targetUUID = target.getUUID();
        this.targetSubLevelId = null;
        this.targetLost = false;
        this.targetLossTicks = 0;
        this.noTargetTicks = 0;
    }

    public void setTargetSubLevel(@Nullable UUID targetSubLevelId) {
        this.targetSubLevelId = targetSubLevelId;
        this.lockedTarget = null;
        this.targetUUID = null;
        this.targetLost = false;
        this.targetLossTicks = 0;
        this.noTargetTicks = 0;
    }

    @Nullable
    public UUID getTargetUUID() {
        return this.targetUUID;
    }

    @Nullable
    public UUID getTargetSubLevelId() {
        return this.targetSubLevelId;
    }

    public C75RocketEntity(EntityType<? extends C75RocketEntity> type, Level world) {
        super(type, world);
    }

    public C75RocketEntity(EntityType<? extends C75RocketEntity> type, double x, double y, double z, Level world) {
        super(type, x, y, z, world, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    public C75RocketEntity(EntityType<? extends C75RocketEntity> type, LivingEntity entity, Level world) {
        super(type, entity.getX(), entity.getEyeY() - 0.1D, entity.getZ(), world, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(CreateTheAirWarsModItems.C75_ROCKET.get());
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    public void setCameraPos(@Nullable BlockPos cameraPos) {
        this.cameraPos = cameraPos != null ? cameraPos.immutable() : null;
    }

    public void setInitialDirection(@Nullable Vec3 initialDirection) {
        this.initialDirection = initialDirection != null && initialDirection.lengthSqr() > 1.0E-6D ? initialDirection.normalize() : null;
    }

    public void setLaunchStartPos(@Nullable Vec3 launchStartPos) {
        this.launchStartPos = launchStartPos;
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

    @Override
    public ExplosionUtils.ProjectileExplosionProfile getExplosionProfile() {
        // Return same explosion profile as X-25ML
        return new ExplosionUtils.ProjectileExplosionProfile(
            4.0F, true, 1.0D, "create_the_air_wars:shellexp2", SoundSource.HOSTILE, 3.8F, 1.0F,
            hi.init.CreateTheAirWarsModParticleTypes.EXLOSION.get(), 5, 1.0D, 1.0D, 1.0D, 0.22D, 3, false
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
                : (current.lengthSqr() > 1.0E-6D ? current.normalize() : new Vec3(0.0D, 0.0D, -1.0D));

            if (this.tickCount <= BOOST_TICKS) {
                double progress = Mth.clamp((double) this.tickCount / BOOST_TICKS, 0.0D, 1.0D);
                Vec3 climbDir = straightDir.add(0.0D, BOOST_CLIMB_BIAS, 0.0D).normalize();
                Vec3 desiredDir = current.lengthSqr() > 1.0E-6D
                    ? current.normalize().lerp(climbDir, 0.34D).normalize()
                    : climbDir;
                double speed = Mth.lerp(progress, INITIAL_FORWARD_SPEED, BOOST_END_SPEED);
                this.setDeltaMovement(desiredDir.scale(speed));
                this.refreshOrientation();
            } else {
                Vec3 currentDir = current.lengthSqr() > 1.0E-6D ? current.normalize() : straightDir;
                double speed = Math.max(GLIDE_MIN_SPEED, current.length() * GLIDE_DAMPING);
                Vec3 desiredDir = currentDir;
                boolean hasTarget = false;

                if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    if (this.lockedTarget == null && this.targetUUID != null) {
                        this.lockedTarget = serverLevel.getEntity(this.targetUUID);
                    }

                    if (this.lockedTarget != null && this.lockedTarget.isAlive()) {
                        hasTarget = true;
                        Vec3 targetPos = this.lockedTarget.position();
                        Vec3 targetVel = this.lockedTarget.getDeltaMovement();
                        double distance = this.position().distanceTo(targetPos);
                        
                        // Intercept and detonate if getting very close to target
                        if (this.lockedTarget instanceof net.minecraft.world.entity.LivingEntity) {
                            if (distance < 4.5D) {
                                this.explodeNow(this.position());
                                return;
                            }
                        } else {
                            if (distance < 3.0D) {
                                this.lockedTarget.discard();
                                this.explodeNow(this.position());
                                return;
                            }
                        }

                        // Decoy flare (LTC) distraction check (every 5 ticks)
                        if (this.tickCount % 5 == 0 && !(this.lockedTarget instanceof HeattrapFlareEntity)) {
                            List<HeattrapFlareEntity> flares = this.level().getEntitiesOfClass(HeattrapFlareEntity.class, this.lockedTarget.getBoundingBox().inflate(24.0D), HeattrapFlareEntity::canAttractAim9x);
                            if (!flares.isEmpty()) {
                                HeattrapFlareEntity flare = flares.get(this.random.nextInt(flares.size()));
                                // 20% chance to decoy/divert to flare instead of original target
                                if (this.random.nextFloat() < 0.20F) {
                                    this.setTargetEntity(flare);
                                    targetPos = flare.position();
                                    targetVel = flare.getDeltaMovement();
                                    distance = this.position().distanceTo(targetPos);
                                }
                            }
                        }

                        Vec3 targetVector = computeInterceptVector(this.position(), targetPos, targetVel, speed);
                        if (targetVector.lengthSqr() > 1.0E-6D) {
                            Vec3 desiredTargetDir = targetVector.normalize();
                            double alignment = currentDir.dot(desiredTargetDir);
                            // Seeker FOV limit check (165 degrees) from Aim-9x
                            if (alignment < Math.cos(Math.toRadians(165.0D))) {
                                targetLossTicks++;
                                if (targetLossTicks >= 6) {
                                    this.lockedTarget = null;
                                    this.targetUUID = null;
                                    hasTarget = false;
                                }
                            } else {
                                targetLossTicks = 0;
                                desiredDir = rotateTowards(currentDir, desiredTargetDir, Math.toRadians(28.0));
                                double pull = Mth.clamp(distance / 64.0D, 0.2D, 1.0D);
                                speed = Math.max(GLIDE_MIN_SPEED, speed * (1.0D - TARGET_PULL * 0.2D) + BOOST_END_SPEED * 0.06D * pull);
                            }
                        }
                    } else if (!targetLost && targetSubLevelId != null) {
                        hi.util.Aim9xTargetingHelper.TrackedSubLevelTarget target = hi.util.Aim9xTargetingHelper.resolveTarget(serverLevel, targetSubLevelId);
                        if (target == null) {
                            targetLost = true;
                        } else {
                            hasTarget = true;
                            Vec3 targetVector = computeInterceptVector(this.position(), target.position(), target.velocityPerTick(), speed);
                            if (targetVector.lengthSqr() > 1.0E-6D) {
                                Vec3 aimDir = targetVector.normalize();
                                double alignment = currentDir.dot(aimDir);
                                if (alignment < Math.cos(Math.toRadians(165.0))) {
                                    targetLossTicks++;
                                    if (targetLossTicks >= 6) {
                                        targetLost = true;
                                        targetSubLevelId = null;
                                        hasTarget = false;
                                    }
                                } else {
                                    targetLossTicks = 0;
                                    desiredDir = rotateTowards(currentDir, aimDir, Math.toRadians(28.0));
                                    
                                    double distance = this.position().distanceTo(target.position());
                                    if (distance < 4.5D) {
                                        this.explodeNow(this.position());
                                        return;
                                    }
                                    double pull = Mth.clamp(distance / 64.0D, 0.2D, 1.0D);
                                    speed = Math.max(GLIDE_MIN_SPEED, speed * (1.0D - TARGET_PULL * 0.2D) + BOOST_END_SPEED * 0.06D * pull);
                                }
                            }
                        }
                    } else if (this.cameraPos != null && this.level().getBlockEntity(this.cameraPos) instanceof hi.block.entity.CameraBlockEntity camera) {
                        Vec3 targetPos = camera.getGuidanceTarget();
                        if (targetPos != null) {
                            hasTarget = true;
                            Vec3 toTarget = targetPos.subtract(this.position());
                            if (toTarget.lengthSqr() > 1.0E-6D) {
                                desiredDir = rotateTowards(currentDir, toTarget.normalize(), Math.toRadians(28.0));
                                double distance = toTarget.length();
                                double pull = Mth.clamp(distance / 64.0D, 0.2D, 1.0D);
                                speed = Math.max(GLIDE_MIN_SPEED, speed * (1.0D - TARGET_PULL * 0.2D) + BOOST_END_SPEED * 0.06D * pull);
                            }
                        }
                    }
                }

                if (!hasTarget) {
                    desiredDir = currentDir;
                    noTargetTicks++;
                    if (noTargetTicks >= 60) {
                        this.explodeNow(this.position());
                        return;
                    }
                } else {
                    noTargetTicks = 0;
                }

                this.setDeltaMovement(desiredDir.scale(speed));
                this.refreshOrientation();
            }
        }

        super.tick();

        if (this.inGround && !this.level().isClientSide) {
            this.discard();
        } else if (this.level().isClientSide) {
            this.inGround = false;
            this.spawnThrusterParticles();
        }
    }

    private boolean isEngineActive() {
        return this.tickCount <= BOOST_TICKS;
    }

    @Nullable
    private Vec3 resolveGuidanceTarget() {
        if (this.lockedTarget == null && this.targetUUID != null && !this.level().isClientSide && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            this.lockedTarget = serverLevel.getEntity(this.targetUUID);
        }
        if (this.lockedTarget != null && this.lockedTarget.isAlive()) {
            Vec3 targetPos = this.lockedTarget.position();
            Vec3 targetVel = this.lockedTarget.getDeltaMovement();
            double distance = this.position().distanceTo(targetPos);
            double timeToTarget = distance / 2.5D; // Estimate average travel speed
            
            // Predicitive interception: predicted target position in the future
            return targetPos.add(targetVel.scale(timeToTarget));
        }
        if (this.cameraPos != null && this.level().getBlockEntity(this.cameraPos) instanceof hi.block.entity.CameraBlockEntity camera) {
            return camera.getGuidanceTarget();
        }
        return this.launchStartPos != null && this.initialDirection != null
            ? this.launchStartPos.add(this.initialDirection.scale(128.0D))
            : null;
    }

    private void spawnThrusterParticles() {
        float yaw = this.getVisualYaw(0.0F);
        float pitch = this.getVisualPitch(0.0F);
        Vec3 entityVelocity = this.getDeltaMovement();
        for (int i = 0; i < THRUSTER_ORIGINS.length; i++) {
            Vec3 positionOffset = this.getThrusterPositionOffset(THRUSTER_ORIGINS[i], THRUSTER_YAW_OFFSETS[i], yaw, pitch);
            Vec3 direction = this.getThrusterDirection(THRUSTER_YAW_OFFSETS[i], yaw, pitch);
            Vec3 spawnPos = this.position().add(positionOffset).add(direction.scale(0.14D));
            Vec3 particleVelocity = direction.scale(THRUSTER_PARTICLE_PUSH)
                .add(entityVelocity.scale(THRUSTER_PARTICLE_INHERIT))
                .add(
                    (this.random.nextDouble() - 0.5D) * 0.005D,
                    (this.random.nextDouble() - 0.5D) * 0.005D,
                    (this.random.nextDouble() - 0.5D) * 0.005D
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

    private Vec3 getThrusterPositionOffset(Vec3 thrusterOrigin, float thrusterYawOffset, float bodyYaw, float bodyPitch) {
        org.joml.Vector3f position = new org.joml.Vector3f(0.0F, 0.0F, 2.0F);
        position.rotateY((float) Math.toRadians(thrusterYawOffset));
        position.add((float) thrusterOrigin.x, (float) thrusterOrigin.y, (float) thrusterOrigin.z);
        position.sub(MODEL_CENTER, MODEL_CENTER, MODEL_CENTER).mul(MODEL_SCALE).mul(3.5f);
        position.rotateX((float) Math.toRadians(bodyPitch));
        position.rotateY((float) Math.toRadians(bodyYaw + 180.0F));
        return new Vec3(position.x, position.y, position.z);
    }

    private Vec3 getThrusterDirection(float thrusterYawOffset, float bodyYaw, float bodyPitch) {
        org.joml.Vector3f direction = new org.joml.Vector3f(0.0F, 0.0F, 1.0F);
        direction.rotateY((float) Math.toRadians(thrusterYawOffset));
        direction.rotateX((float) Math.toRadians(bodyPitch));
        direction.rotateY((float) Math.toRadians(bodyYaw + 180.0F));
        direction.normalize();
        return new Vec3(direction.x, direction.y, direction.z);
    }

    @Override
    public void onHitBlock(BlockHitResult blockHitResult) {
        if (this.level().isClientSide || this.tickCount <= LAUNCH_GRACE_TICKS) {
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
        if (this.targetUUID != null) {
            tag.putUUID("TargetUUID", this.targetUUID);
        }
        if (this.targetSubLevelId != null) {
            tag.putUUID("TargetSubLevelId", this.targetSubLevelId);
        }
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
            this.launchStartPos = new Vec3(tag.getDouble("LaunchStartX"), tag.getDouble("LaunchStartY"), tag.getDouble("LaunchStartZ"));
        } else {
            this.launchStartPos = null;
        }
        if (tag.hasUUID("TargetUUID")) {
            this.targetUUID = tag.getUUID("TargetUUID");
        } else {
            this.targetUUID = null;
        }
        if (tag.hasUUID("TargetSubLevelId")) {
            this.targetSubLevelId = tag.getUUID("TargetSubLevelId");
        } else {
            this.targetSubLevelId = null;
        }
    }

    public static Vec3 computeInterceptVector(Vec3 missilePos, Vec3 targetPos, Vec3 targetVelocity, double missileSpeed) {
        Vec3 relativePos = targetPos.subtract(missilePos);
        double distance = relativePos.length();
        if (distance < 1.0E-4D) {
            return relativePos;
        }

        // Bounded proportional lead pursuit: smooth, direct intercept trajectory without jitter or looping
        double effectiveSpeed = Math.max(missileSpeed, 1.0D);
        double timeToTarget = Mth.clamp(distance / effectiveSpeed, 0.0D, 2.5D);
        Vec3 predictedTargetPos = targetPos.add(targetVelocity.scale(timeToTarget));
        return predictedTargetPos.subtract(missilePos);
    }

    public static Vec3 rotateTowards(Vec3 currentDirection, Vec3 desiredDirection, double maxTurnRadians) {
        double cosTheta = currentDirection.dot(desiredDirection);
        if (cosTheta >= 0.9999) {
            return desiredDirection;
        }
        double theta = Math.acos(Mth.clamp(cosTheta, -1.0, 1.0));
        if (theta <= maxTurnRadians) {
            return desiredDirection;
        }
        Vec3 rotationAxis = currentDirection.cross(desiredDirection);
        if (rotationAxis.lengthSqr() < 1.0E-6) {
            return desiredDirection;
        }
        rotationAxis = rotationAxis.normalize();
        org.joml.Quaterniond rotation = new org.joml.Quaterniond().setAngleAxis(maxTurnRadians, rotationAxis.x, rotationAxis.y, rotationAxis.z);
        org.joml.Vector3d current = new org.joml.Vector3d(currentDirection.x, currentDirection.y, currentDirection.z);
        current.rotate(rotation);
        return new Vec3(current.x, current.y, current.z).normalize();
    }
}
