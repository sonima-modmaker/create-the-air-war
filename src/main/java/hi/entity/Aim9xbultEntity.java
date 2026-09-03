package hi.entity;

import hi.init.CreateTheAirWarsModBlocks;
import hi.init.CreateTheAirWarsModEntities;
import hi.init.CreateTheAirWarsModParticleTypes;
import hi.procedures.AIM9ROCKETTRUEKoghdaSnariadPopadaietVBlokProcedure;
import hi.procedures.DsfsdsfPriObnovlieniiTikaProcedure;
import hi.util.ExplosionUtils;
import hi.util.Aim9xTargetingHelper;
import hi.util.ProjectileChunkLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
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
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.UUID;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class Aim9xbultEntity extends AbstractArrow implements ItemSupplier, RocketExplosionCarrier {
    private static final int ACTIVATION_DELAY_TICKS = 0;
    private static final double ENGINE_MIN_SPEED = 2.2;
    private static final double ENGINE_ACCELERATION = 1.045;
    private static final double MAX_SPEED = 4.5;
    private static final double GUIDANCE_TURN_RADIANS = Math.toRadians(28.0);
    private static final double MAX_SEEK_ANGLE_COS = Math.cos(Math.toRadians(165.0));
    private static final int TARGET_LOSS_TICKS = 6;
    private static final double HEAT_TRAP_SEARCH_RANGE = 160.0;
    private static final double HEAT_TRAP_SEARCH_RANGE_SQR = HEAT_TRAP_SEARCH_RANGE * HEAT_TRAP_SEARCH_RANGE;
    private static final double HEAT_TRAP_SEARCH_COS = -1.0;
    private static final double ROCKET_TARGET_SEARCH_RANGE = 320.0;
    private static final double ROCKET_TARGET_SEARCH_RANGE_SQR = ROCKET_TARGET_SEARCH_RANGE * ROCKET_TARGET_SEARCH_RANGE;
    private static final double ROCKET_TARGET_SEARCH_COS = -1.0;
    private static final double PROXIMITY_FUSE_RANGE = 3.0;
    private static final double PROXIMITY_FUSE_RANGE_SQR = PROXIMITY_FUSE_RANGE * PROXIMITY_FUSE_RANGE;
    private static final int TARGET_SCAN_INTERVAL_TICKS = 5;
    private static final int PROXIMITY_SCAN_INTERVAL_TICKS = 2;
    private static final int TRAIL_PARTICLE_INTERVAL_TICKS = 2;
    private static final int STRAIGHT_LAUNCH_TICKS = 8;
    private static final double FRIENDLY_LAUNCH_RADIUS = 20.0;
    private static final double FRIENDLY_LAUNCH_RADIUS_SQR = FRIENDLY_LAUNCH_RADIUS * FRIENDLY_LAUNCH_RADIUS;
    private static final double INHERITED_VELOCITY_FADE = 0.94;
    private static final double INHERITED_VELOCITY_EPSILON_SQR = 0.0025;

    private int activationDelayTicks = ACTIVATION_DELAY_TICKS;
    private int targetScanCooldown;
    private int proximityScanCooldown;
    private UUID targetSubLevelId;
    private UUID targetHeatTrapId;
    private UUID targetRocketId;
    private UUID launchSubLevelId;
    private Vec3 launchPosition;
    private Vec3 inheritedLaunchVelocity = Vec3.ZERO;
    private boolean targetLost;
    private int targetLossTicks;

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    public static final ItemStack PROJECTILE_ITEM = new ItemStack(CreateTheAirWarsModBlocks.AIM9XACTIVE.get());

    public Aim9xbultEntity(EntityType<? extends Aim9xbultEntity> type, Level world) {
        super(type, world);
    }

    public Aim9xbultEntity(EntityType<? extends Aim9xbultEntity> type, double x, double y, double z, Level world) {
        super(type, x, y, z, world, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    public Aim9xbultEntity(EntityType<? extends Aim9xbultEntity> type, LivingEntity entity, Level world) {
        super(type, entity.getX(), entity.getEyeY() - 0.1, entity.getZ(), world, ItemStack.EMPTY, ItemStack.EMPTY);
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
        if (isInsideFriendlyLaunchZone(blockHitResult.getLocation())) {
            this.inGround = false;
            Vec3 motion = this.getDeltaMovement();
            Vec3 nudge = motion.lengthSqr() > 1.0E-6 ? motion.normalize().scale(0.85) : this.getForward().normalize().scale(0.85);
            Vec3 safePos = blockHitResult.getLocation().add(nudge);
            this.setPos(safePos.x, safePos.y, safePos.z);
            return;
        }
        AIM9ROCKETTRUEKoghdaSnariadPopadaietVBlokProcedure.execute(this.level(), blockHitResult.getLocation().x, blockHitResult.getLocation().y, blockHitResult.getLocation().z, this);
        ProjectileChunkLoader.release(this);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) {
            this.inGround = false;
            return;
        }
        if (isFriendlyLaunchEntity(result.getEntity())) {
            this.inGround = false;
            return;
        }
        if (isValidSeekerRocketTarget(result.getEntity())) {
            result.getEntity().discard();
        }
        AIM9ROCKETTRUEKoghdaSnariadPopadaietVBlokProcedure.execute(this.level(), result.getLocation().x, result.getLocation().y, result.getLocation().z, this);
        ProjectileChunkLoader.release(this);
        this.discard();
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            ProjectileChunkLoader.update(this);
        }

        if (!this.level().isClientSide) {
            if (activationDelayTicks > 0) {
                this.setNoGravity(false);
                activationDelayTicks--;
                Vec3 delayedVelocity = this.getDeltaMovement();
                this.setDeltaMovement(delayedVelocity.x * 0.996, delayedVelocity.y, delayedVelocity.z * 0.996);
            } else {
                this.setNoGravity(true);
                Vec3 currentVelocity = this.getDeltaMovement();
                Vec3 activeInheritedVelocity = getActiveInheritedLaunchVelocity();
                Vec3 rocketVelocity = currentVelocity.subtract(activeInheritedVelocity);
                Vec3 currentDirection = rocketVelocity.lengthSqr() > 1.0E-6 ? rocketVelocity.normalize()
                    : (currentVelocity.lengthSqr() > 1.0E-6 ? currentVelocity.normalize() : this.getForward().normalize());
                double currentRocketSpeed = rocketVelocity.lengthSqr() > 1.0E-6 ? rocketVelocity.length() : currentVelocity.length();
                double nextSpeed = Mth.clamp(Math.max(ENGINE_MIN_SPEED, currentRocketSpeed * ENGINE_ACCELERATION), ENGINE_MIN_SPEED, MAX_SPEED);
                if (this.tickCount <= STRAIGHT_LAUNCH_TICKS) {
                    this.setDeltaMovement(currentDirection.scale(nextSpeed).add(activeInheritedVelocity));
                    this.updateTrackedRotation(currentDirection);
                    super.tick();
                    return;
                }
                if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    if (targetScanCooldown-- <= 0) {
                        maybeAcquireHeatTrapTarget(serverLevel, currentDirection);
                        if (resolveHeatTrapTarget(serverLevel) == null) {
                            maybeAcquireRocketTarget(serverLevel, currentDirection);
                        }
                        targetScanCooldown = TARGET_SCAN_INTERVAL_TICKS;
                    }
                }
                Vec3 guidedDirection = currentDirection;
                if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    HeattrapFlareEntity heatTrapTarget = resolveHeatTrapTarget(serverLevel);
                    if (heatTrapTarget != null) {
                        Vec3 targetVector = computeInterceptVector(this.position(), heatTrapTarget.position(), heatTrapTarget.getDeltaMovement(), nextSpeed);
                        if (targetVector.lengthSqr() > 1.0E-6) {
                            guidedDirection = rotateTowards(currentDirection, targetVector.normalize(), GUIDANCE_TURN_RADIANS);
                            targetLost = false;
                            targetLossTicks = 0;
                        }
                    } else {
                        Entity rocketTarget = resolveRocketTarget(serverLevel);
                        if (rocketTarget != null) {
                            Vec3 targetVector = computeInterceptVector(this.position(), rocketTarget.position(), rocketTarget.getDeltaMovement(), nextSpeed);
                            if (targetVector.lengthSqr() > 1.0E-6) {
                                guidedDirection = rotateTowards(currentDirection, targetVector.normalize(), GUIDANCE_TURN_RADIANS);
                                targetLost = false;
                                targetLossTicks = 0;
                            }
                        } else if (!targetLost && targetSubLevelId != null) {
                            Aim9xTargetingHelper.TrackedSubLevelTarget target = Aim9xTargetingHelper.resolveTarget(serverLevel, targetSubLevelId);
                            if (target == null) {
                                targetLost = true;
                            } else {
                                Vec3 targetVector = computeInterceptVector(this.position(), target.position(), target.velocityPerTick(), nextSpeed);
                                if (targetVector.lengthSqr() > 1.0E-6) {
                                    Vec3 desiredDirection = targetVector.normalize();
                                    double alignment = currentDirection.dot(desiredDirection);
                                    if (alignment < MAX_SEEK_ANGLE_COS) {
                                        targetLossTicks++;
                                        if (targetLossTicks >= TARGET_LOSS_TICKS) {
                                            targetLost = true;
                                            targetSubLevelId = null;
                                        }
                                    } else {
                                        targetLossTicks = 0;
                                        guidedDirection = rotateTowards(currentDirection, desiredDirection, GUIDANCE_TURN_RADIANS);
                                    }
                                }
                            }
                        }
                    }
                }
                this.setDeltaMovement(guidedDirection.scale(nextSpeed).add(activeInheritedVelocity));
                this.updateTrackedRotation(guidedDirection);
            }
        }

        super.tick();

        if (!this.level().isClientSide && this.isEngineActive() && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            boolean detonateNearbyRocket = false;
            if (proximityScanCooldown-- <= 0) {
                proximityScanCooldown = PROXIMITY_SCAN_INTERVAL_TICKS;
                detonateNearbyRocket = tryDetonateOnNearbyRocket(serverLevel);
            }
            if (tryDetonateOnTrackedTarget(serverLevel) || detonateNearbyRocket) {
                return;
            }
        }

        if (this.isEngineActive()) {
            if ((this.tickCount % TRAIL_PARTICLE_INTERVAL_TICKS) == 0) {
                DsfsdsfPriObnovlieniiTikaProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ());
                if (!this.level().isClientSide && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.sendParticles(CreateTheAirWarsModParticleTypes.SDF.get(), this.getX(), this.getY(), this.getZ(), 1, 0.08, 0.08, 0.08, 0.0);
                }
            }
        }

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

    public boolean isEngineActive() {
        return activationDelayTicks <= 0;
    }

    public void setTargetSubLevel(UUID targetSubLevelId) {
        this.targetSubLevelId = targetSubLevelId;
        this.targetHeatTrapId = null;
        this.targetRocketId = null;
        this.targetLost = false;
        this.targetLossTicks = 0;
    }

    public void setTargetRocket(UUID targetRocketId) {
        this.targetRocketId = targetRocketId;
        this.targetHeatTrapId = null;
        this.targetSubLevelId = null;
        this.targetLost = false;
        this.targetLossTicks = 0;
    }

    public void setTargetHeatTrap(UUID targetHeatTrapId) {
        this.targetHeatTrapId = targetHeatTrapId;
        this.targetRocketId = null;
        this.targetSubLevelId = null;
        this.targetLost = false;
        this.targetLossTicks = 0;
    }

    public void setLaunchContext(Vec3 launchPosition, Vec3 inheritedVelocity, UUID launchSubLevelId) {
        this.launchPosition = launchPosition;
        this.inheritedLaunchVelocity = inheritedVelocity != null ? inheritedVelocity : Vec3.ZERO;
        this.launchSubLevelId = launchSubLevelId;
    }

    private void maybeAcquireHeatTrapTarget(net.minecraft.server.level.ServerLevel serverLevel, Vec3 currentDirection) {
        HeattrapFlareEntity bestTarget = findBestHeatTrapTarget(serverLevel, currentDirection);
        if (bestTarget == null) {
            if (targetHeatTrapId != null && resolveHeatTrapTarget(serverLevel) == null) {
                targetHeatTrapId = null;
            }
            return;
        }
        if (bestTarget.getUUID().equals(targetHeatTrapId)) {
            return;
        }
        targetHeatTrapId = bestTarget.getUUID();
        targetRocketId = null;
        targetSubLevelId = null;
        targetLost = false;
        targetLossTicks = 0;
    }

    private HeattrapFlareEntity findBestHeatTrapTarget(net.minecraft.server.level.ServerLevel serverLevel, Vec3 currentDirection) {
        HeattrapFlareEntity bestTarget = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (HeattrapFlareEntity flare : serverLevel.getEntitiesOfClass(HeattrapFlareEntity.class, this.getBoundingBox().inflate(HEAT_TRAP_SEARCH_RANGE), flare -> flare.canAttractAim9x() && !isFriendlyLaunchEntity(flare))) {
            Vec3 toTarget = flare.position().subtract(this.position());
            double distanceSqr = toTarget.lengthSqr();
            if (distanceSqr < 1.0 || distanceSqr > HEAT_TRAP_SEARCH_RANGE_SQR) {
                continue;
            }
            Vec3 directionToTarget = toTarget.normalize();
            double alignment = currentDirection.dot(directionToTarget);
            if (alignment < HEAT_TRAP_SEARCH_COS) {
                continue;
            }
            double score = alignment * 3.5 + (1.0 - Math.sqrt(distanceSqr) / HEAT_TRAP_SEARCH_RANGE);
            if (score > bestScore) {
                bestScore = score;
                bestTarget = flare;
            }
        }
        return bestTarget;
    }

    private HeattrapFlareEntity resolveHeatTrapTarget(net.minecraft.server.level.ServerLevel serverLevel) {
        if (targetHeatTrapId == null) {
            return null;
        }
        Entity entity = serverLevel.getEntity(targetHeatTrapId);
        if (entity instanceof HeattrapFlareEntity flare && flare.canAttractAim9x()) {
            return flare;
        }
        targetHeatTrapId = null;
        return null;
    }

    private void maybeAcquireRocketTarget(net.minecraft.server.level.ServerLevel serverLevel, Vec3 currentDirection) {
        Entity bestTarget = findBestRocketTarget(serverLevel, currentDirection);
        if (bestTarget == null) {
            if (targetRocketId != null && resolveRocketTarget(serverLevel) == null) {
                targetRocketId = null;
            }
            return;
        }
        targetRocketId = bestTarget.getUUID();
        targetHeatTrapId = null;
        targetLost = false;
        targetLossTicks = 0;
        targetSubLevelId = null;
    }

    private Entity findBestRocketTarget(net.minecraft.server.level.ServerLevel serverLevel, Vec3 currentDirection) {
        Entity bestTarget = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Entity entity : serverLevel.getEntities(this, this.getBoundingBox().inflate(ROCKET_TARGET_SEARCH_RANGE), this::isValidSeekerRocketTargetForThisMissile)) {
            Vec3 toTarget = entity.position().subtract(this.position());
            double distanceSqr = toTarget.lengthSqr();
            if (distanceSqr < 1.0 || distanceSqr > ROCKET_TARGET_SEARCH_RANGE_SQR) {
                continue;
            }
            Vec3 directionToTarget = toTarget.normalize();
            double alignment = currentDirection.dot(directionToTarget);
            if (alignment < ROCKET_TARGET_SEARCH_COS) {
                continue;
            }
            double score = alignment * 3.5
                + (1.0 - Math.sqrt(distanceSqr) / ROCKET_TARGET_SEARCH_RANGE)
                + getRocketTargetPriorityBonus(entity);
            if (score > bestScore) {
                bestScore = score;
                bestTarget = entity;
            }
        }
        return bestTarget;
    }

    private Entity resolveRocketTarget(net.minecraft.server.level.ServerLevel serverLevel) {
        if (targetRocketId == null) {
            return null;
        }
        Entity entity = serverLevel.getEntity(targetRocketId);
        if (isValidSeekerRocketTargetForThisMissile(entity) && this.distanceToSqr(entity) <= ROCKET_TARGET_SEARCH_RANGE_SQR) {
            return entity;
        }
        targetRocketId = null;
        return null;
    }

    private boolean tryDetonateOnTrackedTarget(net.minecraft.server.level.ServerLevel serverLevel) {
        HeattrapFlareEntity heatTrapTarget = resolveHeatTrapTarget(serverLevel);
        if (heatTrapTarget != null && this.distanceToSqr(heatTrapTarget) <= PROXIMITY_FUSE_RANGE_SQR) {
            heatTrapTarget.discard();
            detonateAt(heatTrapTarget.position());
            return true;
        }

        Entity rocketTarget = resolveRocketTarget(serverLevel);
        if (rocketTarget != null && this.distanceToSqr(rocketTarget) <= PROXIMITY_FUSE_RANGE_SQR) {
            rocketTarget.discard();
            detonateAt(rocketTarget.position());
            return true;
        }

        if (targetSubLevelId != null) {
            Aim9xTargetingHelper.TrackedSubLevelTarget target = Aim9xTargetingHelper.resolveTarget(serverLevel, targetSubLevelId);
            if (target != null && this.position().distanceToSqr(target.position()) <= PROXIMITY_FUSE_RANGE_SQR * 2.25) {
                detonateAt(target.position());
                return true;
            }
        }
        return false;
    }

    private boolean tryDetonateOnNearbyRocket(net.minecraft.server.level.ServerLevel serverLevel) {
        for (Entity entity : serverLevel.getEntities(this, this.getBoundingBox().inflate(PROXIMITY_FUSE_RANGE), this::isValidSeekerRocketTargetForThisMissile)) {
            if (this.distanceToSqr(entity) <= PROXIMITY_FUSE_RANGE_SQR) {
                entity.discard();
                detonateAt(entity.position());
                return true;
            }
        }
        return false;
    }

    private void detonateAt(Vec3 location) {
        if (this.level().isClientSide) {
            return;
        }
        AIM9ROCKETTRUEKoghdaSnariadPopadaietVBlokProcedure.execute(this.level(), location.x, location.y, location.z, this);
        ProjectileChunkLoader.release(this);
        this.discard();
    }

    public static boolean isValidSeekerRocketTarget(Entity entity) {
        if (entity == null || !entity.isAlive()) {
            return false;
        }
        if (entity instanceof Aim9xbultEntity || entity instanceof M24bultEntity || entity instanceof HeattrapFlareEntity || entity instanceof MachinegunShellEntity) {
            return false;
        }
        if (entity instanceof VihrRocketEntity || entity instanceof X25mlEntity) {
            return true;
        }
        if (entity instanceof RocketExplosionCarrier) {
            return true;
        }
        return entity instanceof AbstractArrow
            && entity.getClass().getPackageName().equals(Aim9xbultEntity.class.getPackageName());
    }

    private static double getRocketTargetPriorityBonus(Entity entity) {
        if (entity instanceof VihrRocketEntity || entity instanceof X25mlEntity) {
            return 1.25D;
        }
        return 0.0D;
    }

    private boolean isValidSeekerRocketTargetForThisMissile(Entity entity) {
        return isValidSeekerRocketTarget(entity) && !isFriendlyLaunchEntity(entity);
    }

    private boolean isFriendlyLaunchEntity(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (isInsideFriendlyLaunchZone(entity.position())) {
            return true;
        }
        if (launchSubLevelId == null || !(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return false;
        }
        try {
            dev.ryanhcode.sable.sublevel.SubLevel subLevel = dev.ryanhcode.sable.Sable.HELPER.getContaining(serverLevel, BlockPos.containing(entity.position()));
            return subLevel != null && launchSubLevelId.equals(subLevel.getUniqueId());
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean isInsideFriendlyLaunchZone(Vec3 position) {
        return launchPosition != null && position != null && launchPosition.distanceToSqr(position) <= FRIENDLY_LAUNCH_RADIUS_SQR;
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
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ActivationDelayTicks", activationDelayTicks);
        tag.putBoolean("TargetLost", targetLost);
        tag.putInt("TargetLossTicks", targetLossTicks);
        if (targetHeatTrapId != null) {
            tag.putUUID("TargetHeatTrapId", targetHeatTrapId);
        }
        if (targetRocketId != null) {
            tag.putUUID("TargetRocketId", targetRocketId);
        }
        if (targetSubLevelId != null) {
            tag.putUUID("TargetSubLevelId", targetSubLevelId);
        }
        if (launchSubLevelId != null) {
            tag.putUUID("LaunchSubLevelId", launchSubLevelId);
        }
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
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        activationDelayTicks = tag.getInt("ActivationDelayTicks");
        targetLost = tag.getBoolean("TargetLost");
        targetLossTicks = tag.getInt("TargetLossTicks");
        targetHeatTrapId = tag.hasUUID("TargetHeatTrapId") ? tag.getUUID("TargetHeatTrapId") : null;
        targetRocketId = tag.hasUUID("TargetRocketId") ? tag.getUUID("TargetRocketId") : null;
        targetSubLevelId = tag.hasUUID("TargetSubLevelId") ? tag.getUUID("TargetSubLevelId") : null;
        launchSubLevelId = tag.hasUUID("LaunchSubLevelId") ? tag.getUUID("LaunchSubLevelId") : null;
        launchPosition = tag.contains("LaunchX") ? new Vec3(tag.getDouble("LaunchX"), tag.getDouble("LaunchY"), tag.getDouble("LaunchZ")) : null;
        inheritedLaunchVelocity = new Vec3(
            tag.getDouble("InheritedLaunchVelocityX"),
            tag.getDouble("InheritedLaunchVelocityY"),
            tag.getDouble("InheritedLaunchVelocityZ")
        );
    }

    private Vec3 computeInterceptVector(Vec3 missilePos, Vec3 targetPos, Vec3 targetVelocity, double missileSpeed) {
        Vec3 relativePos = targetPos.subtract(missilePos);
        double distance = relativePos.length();
        if (distance < 1.0E-4 || missileSpeed < 1.0E-4) {
            return relativePos;
        }

        double a = targetVelocity.lengthSqr() - missileSpeed * missileSpeed;
        double b = 2.0 * relativePos.dot(targetVelocity);
        double c = relativePos.lengthSqr();
        double timeTicks;

        if (Math.abs(a) < 1.0E-6) {
            timeTicks = Math.max(0.0, -c / Math.min(b, -1.0E-6));
        } else {
            double discriminant = b * b - 4.0 * a * c;
            if (discriminant < 0.0) {
                timeTicks = distance / missileSpeed;
            } else {
                double sqrt = Math.sqrt(discriminant);
                double t1 = (-b - sqrt) / (2.0 * a);
                double t2 = (-b + sqrt) / (2.0 * a);
                timeTicks = choosePositiveInterceptTime(t1, t2, distance / missileSpeed);
            }
        }

        timeTicks = Mth.clamp(timeTicks, 0.0, 60.0);
        Vec3 predictedTarget = targetPos.add(targetVelocity.scale(timeTicks));
        return predictedTarget.subtract(missilePos);
    }

    private double choosePositiveInterceptTime(double t1, double t2, double fallback) {
        double best = Double.POSITIVE_INFINITY;
        if (t1 > 0.0) {
            best = t1;
        }
        if (t2 > 0.0) {
            best = Math.min(best, t2);
        }
        return Double.isFinite(best) ? best : fallback;
    }

    private Vec3 rotateTowards(Vec3 currentDirection, Vec3 desiredDirection, double maxTurnRadians) {
        double dot = Mth.clamp(currentDirection.dot(desiredDirection), -1.0, 1.0);
        double angle = Math.acos(dot);
        if (angle < 1.0E-4 || angle <= maxTurnRadians) {
            return desiredDirection;
        }

        Vec3 axis = currentDirection.cross(desiredDirection);
        if (axis.lengthSqr() < 1.0E-6) {
            return desiredDirection;
        }

        Vec3 normalizedAxis = axis.normalize();
        Quaterniond rotation = new Quaterniond().fromAxisAngleRad(new Vector3d(normalizedAxis.x, normalizedAxis.y, normalizedAxis.z), maxTurnRadians);
        Vector3d rotated = rotation.transform(new Vector3d(currentDirection.x, currentDirection.y, currentDirection.z));
        return new Vec3(rotated.x, rotated.y, rotated.z).normalize();
    }

    private void updateTrackedRotation(Vec3 direction) {
        if (direction.lengthSqr() < 1.0E-6) {
            return;
        }
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yaw = (float) Math.toDegrees(Math.atan2(direction.x, direction.z));
        float pitch = (float) Math.toDegrees(Math.atan2(direction.y, Math.max(horizontal, 1.0E-4)));
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.xRotO = pitch;
        this.yRotO = yaw;
    }

    public static Aim9xbultEntity shoot(Level world, LivingEntity entity, RandomSource random, float power, double damage, int knockback) {
        Aim9xbultEntity entityarrow = new Aim9xbultEntity(CreateTheAirWarsModEntities.AIM9XBULT.get(), entity, world);
        entityarrow.shoot(entity.getViewVector(1).x, entity.getViewVector(1).y, entity.getViewVector(1).z, power * 2, 0);
        entityarrow.setSilent(true);
        entityarrow.setCritArrow(false);
        entityarrow.setBaseDamage(damage);
        world.addFreshEntity(entityarrow);
        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.arrow.shoot")), SoundSource.PLAYERS, 1,
            1f / (random.nextFloat() * 0.5f + 1) + (power / 2));
        return entityarrow;
    }

    public static Aim9xbultEntity shoot(Level world, LivingEntity entity, RandomSource source) {
        return shoot(world, entity, source, 5f, 5, 5);
    }

    public static Aim9xbultEntity shoot(Level world, LivingEntity entity, RandomSource source, float pullingPower) {
        return shoot(world, entity, source, pullingPower * 5f, 5, 5);
    }

    public static Aim9xbultEntity shoot(LivingEntity entity, LivingEntity target) {
        Aim9xbultEntity entityarrow = new Aim9xbultEntity(CreateTheAirWarsModEntities.AIM9XBULT.get(), entity, entity.level());
        double dx = target.getX() - entity.getX();
        double dy = target.getY() + target.getEyeHeight() - 1.1;
        double dz = target.getZ() - entity.getZ();
        entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * 0.2F, dz, 5f * 2, 12.0F);
        entityarrow.setSilent(true);
        entityarrow.setBaseDamage(5);
        entityarrow.setCritArrow(false);
        entity.level().addFreshEntity(entityarrow);
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.arrow.shoot")), SoundSource.PLAYERS, 1,
            1f / (RandomSource.create().nextFloat() * 0.5f + 1));
        return entityarrow;
    }

    @Override
    public ExplosionUtils.ProjectileExplosionProfile getExplosionProfile() {
        return new ExplosionUtils.ProjectileExplosionProfile(
            10f, true, 2.5, "create_the_air_wars:shellex3", SoundSource.NEUTRAL, 5f, 1f,
            CreateTheAirWarsModParticleTypes.EXLOSION.get(), 8, 2.0, 2.0, 2.0, 0.6, 6, false
        );
    }
}
