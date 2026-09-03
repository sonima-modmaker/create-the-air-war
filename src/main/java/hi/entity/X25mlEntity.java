package hi.entity;

import hi.init.CreateTheAirWarsModItems;
import hi.init.CreateTheAirWarsModParticleTypes;
import hi.util.ExplosionUtils;
import hi.util.SuperbWarfareFlightModel;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class X25mlEntity extends AbstractArrow implements ItemSupplier, RocketExplosionCarrier {
    private static final EntityDataAccessor<Boolean> RAID_FLIGHT = SynchedEntityData.defineId(X25mlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int LAUNCH_GRACE_TICKS = 3;
    private static final int BOOST_TICKS = 18;
    private static final double BOOST_CLIMB_BIAS = 0.92D;
    private static final double BOOST_END_SPEED = 2.7D;
    private static final double GLIDE_MIN_SPEED = 1.7D;
    private static final double GLIDE_DAMPING = 0.9965D;
    private static final double GLIDE_STEER = 0.12D;
    private static final double TARGET_PULL = 0.18D;
    private static final float MODEL_CENTER = 8.0F;
    private static final float MODEL_SCALE = 1.0F / 16.0F;
    private static final double THRUSTER_PARTICLE_PUSH = 0.85D;
    private static final double THRUSTER_PARTICLE_INHERIT = 0.22D;
    private static final Vec3[] THRUSTER_ORIGINS = new Vec3[] {
        new Vec3(10.0D, 8.0D, 22.0D),
        new Vec3(6.0D, 8.0D, 22.0D)
    };
    private static final float[] THRUSTER_YAW_OFFSETS = new float[] {32.5F, -32.5F};
    public static final double INITIAL_FORWARD_SPEED = 0.8D;
    public static final ItemStack PROJECTILE_ITEM = new ItemStack(CreateTheAirWarsModItems.X25ML.get());

    @Nullable
    private BlockPos cameraPos;
    @Nullable
    private Vec3 initialDirection;
    @Nullable
    private Vec3 launchStartPos;
    @Nullable
    private Vec3 raidTarget;

    private static final double RAID_CRUISE_CLEARANCE = 20.0D;
    private static final double RAID_POPUP_START_DISTANCE = 150.0D;
    private static final double RAID_POPUP_APEX_DISTANCE = 65.0D;
    private static final double RAID_POPUP_HEIGHT = 60.0D;
    private static final double RAID_SPEED = 2.75D;

    public X25mlEntity(EntityType<? extends X25mlEntity> type, Level world) {
        super(type, world);
    }

    public X25mlEntity(EntityType<? extends X25mlEntity> type, double x, double y, double z, Level world) {
        super(type, x, y, z, world, ItemStack.EMPTY, new ItemStack(net.minecraft.world.item.Items.BOW));
    }

    public X25mlEntity(EntityType<? extends X25mlEntity> type, LivingEntity entity, Level world) {
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
        return ItemStack.EMPTY;
    }

    private static final EntityDataAccessor<Boolean> MANEUVERING = SynchedEntityData.defineId(X25mlEntity.class, EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(RAID_FLIGHT, false);
        builder.define(MANEUVERING, false);
    }

    public boolean isManeuvering() {
        return this.entityData.get(MANEUVERING);
    }

    public void setManeuvering(boolean maneuvering) {
        this.entityData.set(MANEUVERING, maneuvering);
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

    public void setRaidTarget(@Nullable Vec3 target) {
        this.raidTarget = target;
        this.entityData.set(RAID_FLIGHT, target != null);
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

            if (this.raidTarget != null) {
                this.updateRaidFlight(current, straightDir);
            } else if (this.tickCount <= BOOST_TICKS) {
                double progress = Mth.clamp((double) this.tickCount / BOOST_TICKS, 0.0D, 1.0D);
                Vec3 climbDir = straightDir.add(0.0D, BOOST_CLIMB_BIAS, 0.0D).normalize();
                double maxTurn = Mth.lerp(progress, 5.0D, 15.0D);
                Vec3 desiredDir = SuperbWarfareFlightModel.turnToward(current, climbDir, maxTurn);
                double speed = Mth.lerp(progress, INITIAL_FORWARD_SPEED, BOOST_END_SPEED);
                this.setDeltaMovement(desiredDir.scale(speed));
                this.refreshOrientation();
            } else {
                Vec3 target = this.resolveGuidanceTarget();
                Vec3 currentDir = current.lengthSqr() > 1.0E-6D ? current.normalize() : straightDir;
                double speed = Math.max(GLIDE_MIN_SPEED, current.length() * GLIDE_DAMPING);
                Vec3 desiredDir = currentDir;

                if (target != null) {
                    Vec3 toTarget = target.subtract(this.position());
                    if (toTarget.lengthSqr() > 1.0E-6D) {
                        Vec3 targetDir = toTarget.normalize();
                        double maxTurn = Mth.clamp((this.tickCount - BOOST_TICKS) * 0.5D, 2.0D, 15.0D);
                        desiredDir = SuperbWarfareFlightModel.turnToward(currentDir, targetDir, maxTurn);
                        double distance = toTarget.length();
                        double pull = Mth.clamp(distance / 64.0D, 0.2D, 1.0D);
                        speed = Math.max(GLIDE_MIN_SPEED, speed * (1.0D - TARGET_PULL * 0.2D) + BOOST_END_SPEED * 0.06D * pull);
                    }
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
            if (this.isEngineActive()) {
                this.spawnThrusterParticles();
            }
        }
    }

    private void updateRaidFlight(Vec3 current, Vec3 fallbackDirection) {
        Vec3 toTarget = this.raidTarget.subtract(this.position());
        double horizontalDistance = Math.hypot(toTarget.x, toTarget.z);
        Vec3 aimPoint;

        if (horizontalDistance > RAID_POPUP_START_DISTANCE && this.level() instanceof ServerLevel serverLevel) {
            Vec3 horizontal = new Vec3(toTarget.x, 0.0D, toTarget.z).normalize();
            double groundMax = 0.0D;
            double[] lookaheadSteps = new double[] { 16.0D, 32.0D, 64.0D, 110.0D, 170.0D, 240.0D, 320.0D };
            for (double step : lookaheadSteps) {
                if (step > horizontalDistance) break;
                Vec3 samplePos = this.position().add(horizontal.scale(step));
                int solid = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(samplePos.x), Mth.floor(samplePos.z));
                int surface = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, Mth.floor(samplePos.x), Mth.floor(samplePos.z));
                groundMax = Math.max(groundMax, Math.max(solid, surface));
            }
            int solidHere = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(this.getX()), Mth.floor(this.getZ()));
            int surfaceHere = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, Mth.floor(this.getX()), Mth.floor(this.getZ()));
            groundMax = Math.max(groundMax, Math.max(solidHere, surfaceHere));

            double cruiseY = groundMax + RAID_CRUISE_CLEARANCE;
            Vec3 lookAheadPoint = this.position().add(horizontal.scale(48.0D));
            aimPoint = new Vec3(lookAheadPoint.x, cruiseY, lookAheadPoint.z);
        } else {
            double lift;
            if (horizontalDistance >= RAID_POPUP_APEX_DISTANCE) {
                double progress = Mth.clamp(
                    (RAID_POPUP_START_DISTANCE - horizontalDistance)
                        / (RAID_POPUP_START_DISTANCE - RAID_POPUP_APEX_DISTANCE),
                    0.0D, 1.0D);
                double smooth = progress * progress * (3.0D - 2.0D * progress);
                lift = Mth.lerp(smooth, RAID_CRUISE_CLEARANCE, RAID_POPUP_HEIGHT);
            } else {
                double progress = Mth.clamp(horizontalDistance / RAID_POPUP_APEX_DISTANCE, 0.0D, 1.0D);
                double smooth = progress * progress * (3.0D - 2.0D * progress);
                lift = RAID_POPUP_HEIGHT * smooth;
            }
            aimPoint = this.raidTarget.add(0.0D, lift, 0.0D);
        }

        Vec3 currentDir = current.lengthSqr() > 1.0E-6D ? current.normalize() : fallbackDirection;
        Vec3 desiredDir = aimPoint.subtract(this.position()).normalize();
        double maxTurn = horizontalDistance > RAID_POPUP_START_DISTANCE ? 5.0D : 9.0D;
        Vec3 flightDir = SuperbWarfareFlightModel.turnToward(currentDir, desiredDir, maxTurn);

        if (this.isManeuvering() && horizontalDistance > RAID_POPUP_APEX_DISTANCE) {
            Vec3 horizontalDir = new Vec3(toTarget.x, 0.0D, toTarget.z).normalize();
            double weaveAngle = Math.sin(this.tickCount * 0.28D) * 0.22D;
            Vec3 rightVector = new Vec3(-horizontalDir.z, 0.0D, horizontalDir.x);
            flightDir = flightDir.add(rightVector.scale(weaveAngle)).normalize();
        }

        double speed = Math.max(RAID_SPEED, current.length() * 0.998D);
        this.setDeltaMovement(flightDir.scale(speed));
        this.refreshOrientation();
    }

    private boolean isEngineActive() {
        return !this.entityData.get(RAID_FLIGHT) && this.tickCount <= BOOST_TICKS;
    }

    @Nullable
    private Vec3 resolveGuidanceTarget() {
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
                    (this.random.nextDouble() - 0.5D) * 0.03D,
                    (this.random.nextDouble() - 0.5D) * 0.03D,
                    (this.random.nextDouble() - 0.5D) * 0.03D
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
        position.sub(MODEL_CENTER, MODEL_CENTER, MODEL_CENTER).mul(MODEL_SCALE);
        position.rotateY((float) Math.toRadians(bodyYaw + 180.0F));
        position.rotateX((float) Math.toRadians(bodyPitch));
        return new Vec3(position.x, position.y, position.z);
    }

    private Vec3 getThrusterDirection(float thrusterYawOffset, float bodyYaw, float bodyPitch) {
        org.joml.Vector3f direction = new org.joml.Vector3f(0.0F, 0.0F, 1.0F);
        direction.rotateY((float) Math.toRadians(thrusterYawOffset));
        direction.rotateY((float) Math.toRadians(bodyYaw + 180.0F));
        direction.rotateX((float) Math.toRadians(bodyPitch));
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
        if (this.raidTarget != null) {
            tag.putDouble("RaidTargetX", this.raidTarget.x);
            tag.putDouble("RaidTargetY", this.raidTarget.y);
            tag.putDouble("RaidTargetZ", this.raidTarget.z);
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
        if (tag.contains("RaidTargetX")) {
            this.setRaidTarget(new Vec3(tag.getDouble("RaidTargetX"), tag.getDouble("RaidTargetY"), tag.getDouble("RaidTargetZ")));
        } else {
            this.setRaidTarget(null);
        }
    }
}
