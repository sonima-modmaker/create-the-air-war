
package hi.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import hi.init.CreateTheAirWarsModEntities;
import hi.init.CreateTheAirWarsModBlocks;
import hi.init.CreateTheAirWarsModParticleTypes;
import hi.procedures.TomahawktrueKoghdaSnariadPopadaietVBlokProcedure;
import hi.util.ExplosionUtils;
import hi.util.ProjectileChunkLoader;
import hi.util.ProjectileLaunchHelper;
import hi.util.SuperbWarfareFlightModel;
import hi.procedures.DsfsdsfPriObnovlieniiTikaProcedure;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class TomahawkbultEntity extends AbstractArrow implements ItemSupplier, RocketExplosionCarrier {
	private static final EntityDataAccessor<Integer> ACTIVATION_DELAY = SynchedEntityData.defineId(TomahawkbultEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> WAITING_FOR_LOW_ALTITUDE_ACTIVATION = SynchedEntityData.defineId(TomahawkbultEntity.class, EntityDataSerializers.BOOLEAN);
	private static final int ATTACK_STAGE_CRUISE = 0;
	private static final int ATTACK_STAGE_INGRESS = 1;
	private static final int ATTACK_STAGE_ORBIT = 2;
	private static final int ATTACK_STAGE_TERMINAL = 3;
	private static final double TERMINAL_DETONATION_DISTANCE = 5.0;
	private static final double TARGET_REACHED_DISTANCE = 2.0;
	private static final double DROP_MODE_TRIGGER_ALTITUDE = 50.0;
	private static final double ENGINE_START_ALTITUDE = 50.0;
	private static final double CRUISE_ALTITUDE_CLEARANCE = 18.0;
	private static final double APPROACH_ALTITUDE_CLEARANCE = 20.0;
	private static final double DIVE_START_DISTANCE = 140.0;
	private static final double LOW_ALTITUDE_CLEARANCE = 10.0;
	private static final double ATTACK_PATTERN_TRIGGER_DISTANCE = 300.0;
	private static final double ATTACK_PATTERN_HARD_TRIGGER_DISTANCE = 200.0;
	private static final double DESCENT_TARGET_DISTANCE = 200.0;
	private static final double DESCENT_START_DISTANCE = 320.0;
	private static final double ORBIT_RADIUS = 70.0;
	private static final double ORBIT_ENTRY_TOLERANCE = 18.0;
	private static final double ORBIT_ADVANCE_PER_TICK = 0.028;
	private static final int INITIAL_STRAIGHT_TICKS = 14;
	private static final int DELAYED_ACTIVATION_TICKS = 40;
	private static final double BOMB_DROP_VERTICAL_SPEED = -0.65;
	private static final double OBSTACLE_LOOKAHEAD = 28.0;
	private static final double OBSTACLE_VERTICAL_CLEARANCE = 16.0;
	private static final double GUIDANCE_Y_SMOOTHING = 0.035;
	private static final double GUIDANCE_DIRECTION_SMOOTHING = 0.06;
	private static final double MIN_TURN_BLEND = 0.035;
	private static final double MAX_TURN_BLEND = 0.12;
	private static final double MAX_POWERED_RANGE = 5000.0;
	private static final double INITIAL_MIN_SPEED = 2.35;
	private static final double TERMINAL_SPEED = 2.45;
	private static final double CRUISE_SPEED = 2.15;
	private boolean hasTarget;
	private double targetX;
	private double targetY;
	private double targetZ;
	private int attackStage = ATTACK_STAGE_CRUISE;
	private int orbitDirection = 1;
	private double orbitProgress = 0.0;
	private double orbitStartAngle = 0.0;
	private double smoothedDesiredY = Double.NaN;
	private Vec3 smoothedDesiredDirection = Vec3.ZERO;
	private int poweredFlightTicks;
	private double poweredDistanceTravelled;
	private boolean engineCutoff;

	@Override
	protected net.minecraft.world.item.ItemStack getDefaultPickupItem() { return net.minecraft.world.item.ItemStack.EMPTY; }
	public static final ItemStack PROJECTILE_ITEM = new ItemStack(CreateTheAirWarsModBlocks.THTRUE.get());

	

	public TomahawkbultEntity(EntityType<? extends TomahawkbultEntity> type, Level world) {
		super(type, world);
	}

	public TomahawkbultEntity(EntityType<? extends TomahawkbultEntity> type, double x, double y, double z, Level world) {
		super(type, x, y, z, world, net.minecraft.world.item.ItemStack.EMPTY, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOW));
	}

	public TomahawkbultEntity(EntityType<? extends TomahawkbultEntity> type, LivingEntity entity, Level world) {
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

	private static final EntityDataAccessor<Boolean> MANEUVERING = SynchedEntityData.defineId(TomahawkbultEntity.class, EntityDataSerializers.BOOLEAN);

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ACTIVATION_DELAY, 0);
		builder.define(WAITING_FOR_LOW_ALTITUDE_ACTIVATION, false);
		builder.define(MANEUVERING, false);
	}

	public boolean isManeuvering() {
		return this.entityData.get(MANEUVERING);
	}

	public void setManeuvering(boolean maneuvering) {
		this.entityData.set(MANEUVERING, maneuvering);
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
		TomahawktrueKoghdaSnariadPopadaietVBlokProcedure.execute(this.level(), blockHitResult.getBlockPos().getX(), blockHitResult.getBlockPos().getY(),
				blockHitResult.getBlockPos().getZ(), this);
		ProjectileChunkLoader.release(this);
		this.discard();
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		if (this.level().isClientSide) {
			this.inGround = false;
			return;
		}
		TomahawktrueKoghdaSnariadPopadaietVBlokProcedure.execute(this.level(), result.getLocation().x, result.getLocation().y, result.getLocation().z, this);
		ProjectileChunkLoader.release(this);
		this.discard();
	}

	@Override
	public ExplosionUtils.ProjectileExplosionProfile getExplosionProfile() {
		return new ExplosionUtils.ProjectileExplosionProfile(
			5f, true, 1.5, "create_the_air_wars:shellexp2", SoundSource.NEUTRAL, 5f, 1f,
			CreateTheAirWarsModParticleTypes.EXLOSION.get(), 6, 1.5, 1.5, 1.5, 0.4, 4, false
		);
	}

	@Override
	public void tick() {
		if (!this.level().isClientSide) {
			ProjectileChunkLoader.update(this);
		}
		if (!this.level().isClientSide) {
			if (!this.hasTarget) {
				Vec3 motion = this.getDeltaMovement();
				double motionLen = Math.sqrt(motion.x * motion.x + motion.y * motion.y + motion.z * motion.z);
				double dirX;
				double dirZ;
				if (motionLen > 0.05) {
					dirX = motion.x / motionLen;
					dirZ = motion.z / motionLen;
				} else {
					double yawRad = Math.toRadians(this.getYRot());
					dirX = -Math.sin(yawRad);
					dirZ = Math.cos(yawRad);
				}
				double tx = this.getX() + dirX * 320;
				double tz = this.getZ() + dirZ * 320;
				double ty = this.getY() + 20;
				this.hasTarget = true;
				this.targetX = tx;
				this.targetY = ty;
				this.targetZ = tz;
			}
		}
		if (!this.level().isClientSide && this.hasTarget) {
			if (this.isWaitingForLowAltitudeActivation()) {
				this.setNoGravity(false);
				this.poweredFlightTicks = 0;
				this.smoothedDesiredY = Double.NaN;
				this.smoothedDesiredDirection = Vec3.ZERO;
				Vec3 delayedVelocity = this.getDeltaMovement();
				this.setDeltaMovement(delayedVelocity.x * 0.996, delayedVelocity.y, delayedVelocity.z * 0.996);
				if (this.getCurrentRadioAltitude() <= ENGINE_START_ALTITUDE) {
					this.setWaitingForLowAltitudeActivation(false);
					this.setNoGravity(true);
					this.poweredFlightTicks = 0;
				}
			} else if (this.getActivationDelayTicks() > 0) {
				this.setNoGravity(false);
				this.poweredFlightTicks = 0;
				this.smoothedDesiredY = Double.NaN;
				this.smoothedDesiredDirection = Vec3.ZERO;
				Vec3 delayedVelocity = this.getDeltaMovement();
				this.setDeltaMovement(delayedVelocity.x * 0.992, delayedVelocity.y, delayedVelocity.z * 0.992);
				this.setActivationDelayTicks(this.getActivationDelayTicks() - 1);
			} else {
				this.setNoGravity(true);
				this.poweredFlightTicks++;
				if (!this.engineCutoff) {
					this.poweredDistanceTravelled += this.getDeltaMovement().length();
					if (this.poweredDistanceTravelled >= MAX_POWERED_RANGE) {
						this.engineCutoff = true;
						this.setNoGravity(false);
					}
				}
			}

			if (!this.isEngineActive()) {
				if (this.engineCutoff) {
					Vec3 glide = this.getDeltaMovement();
					this.setNoGravity(false);
					this.setDeltaMovement(glide.x * 0.998, glide.y - 0.035, glide.z * 0.998);
				}
				Level _level = this.level();
				if (_level.isClientSide()) {
					DsfsdsfPriObnovlieniiTikaProcedure.execute(_level, this.getX(), this.getY(), this.getZ());
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
				return;
			}

			double tx = this.targetX;
			double ty = this.targetY;
			double tz = this.targetZ;
			Vec3 pos = this.position();
			Vec3 currentVel = this.getDeltaMovement();
			Vec3 currentDir = currentVel.lengthSqr() > 1.0e-6 ? currentVel.normalize() : this.getForward().normalize();
			if (this.poweredFlightTicks <= INITIAL_STRAIGHT_TICKS) {
				double straightSpeed = Math.max(INITIAL_MIN_SPEED, currentVel.length() * 1.04);
				Vec3 straightVelocity = currentDir.scale(straightSpeed);
				this.setDeltaMovement(straightVelocity);
				this.setYRot((float) (Math.toDegrees(Math.atan2(straightVelocity.z, straightVelocity.x)) - 90.0));
				double horizSpeed = Math.sqrt(straightVelocity.x * straightVelocity.x + straightVelocity.z * straightVelocity.z);
				this.setXRot((float) (-Math.toDegrees(Math.atan2(straightVelocity.y, Math.max(horizSpeed, 0.001)))));
			} else {
			Vec3 toTarget = new Vec3(tx - pos.x, ty - pos.y, tz - pos.z);
			double fullDistance = toTarget.length();
			if (fullDistance <= TARGET_REACHED_DISTANCE) {
				TomahawktrueKoghdaSnariadPopadaietVBlokProcedure.execute(this.level(), tx, ty, tz, this);
				ProjectileChunkLoader.release(this);
				this.discard();
				return;
			}

			double dx = tx - pos.x;
			double dz = tz - pos.z;
			double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
			if (horizontalDistance > 0.001) {
				double dirX = dx / horizontalDistance;
				double dirZ = dz / horizontalDistance;
				double ground = 0.0;
				double[] lookahead = new double[] { 40, 90, 160, 240, 320 };
				for (double step : lookahead) {
					double aheadX = pos.x + dirX * Math.min(step, horizontalDistance);
					double aheadZ = pos.z + dirZ * Math.min(step, horizontalDistance);
					int solid = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(aheadX), (int) Math.floor(aheadZ));
					int surface = this.level().getHeight(Heightmap.Types.WORLD_SURFACE, (int) Math.floor(aheadX), (int) Math.floor(aheadZ));
					ground = Math.max(ground, Math.max(solid, surface));
				}
				int solidHere = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(pos.x), (int) Math.floor(pos.z));
				int surfaceHere = this.level().getHeight(Heightmap.Types.WORLD_SURFACE, (int) Math.floor(pos.x), (int) Math.floor(pos.z));
				ground = Math.max(ground, Math.max(solidHere, surfaceHere));
				double targetGround = getGroundHeight(tx, tz);

				updateAttackPattern(horizontalDistance, pos, currentDir);

				Vec3 desiredTarget = new Vec3(tx, ty, tz);
				double cruiseAltitude = ground + CRUISE_ALTITUDE_CLEARANCE;
				double approachAltitude = ground + APPROACH_ALTITUDE_CLEARANCE;
				double desiredAltitude = cruiseAltitude;

				if (horizontalDistance <= DESCENT_START_DISTANCE) {
					double descentFactor = Mth.clamp((DESCENT_START_DISTANCE - horizontalDistance) / (DESCENT_START_DISTANCE - DESCENT_TARGET_DISTANCE), 0.0, 1.0);
					desiredAltitude = Mth.lerp(descentFactor, cruiseAltitude, approachAltitude);
				}
				if (horizontalDistance <= DESCENT_TARGET_DISTANCE) {
					desiredAltitude = approachAltitude;
				}

				if (attackStage == ATTACK_STAGE_INGRESS || attackStage == ATTACK_STAGE_ORBIT) {
					desiredAltitude = approachAltitude;
				}

				if (attackStage == ATTACK_STAGE_INGRESS) {
					Vec3 offset = getOrbitOffset();
					desiredTarget = new Vec3(tx + offset.x, desiredAltitude, tz + offset.z);
					if (pos.distanceTo(desiredTarget) <= ORBIT_ENTRY_TOLERANCE) {
						attackStage = ATTACK_STAGE_ORBIT;
						orbitStartAngle = Math.atan2(pos.z - tz, pos.x - tx);
						orbitProgress = 0.0;
					}
				} else if (attackStage == ATTACK_STAGE_ORBIT) {
					orbitProgress = Math.min(Math.PI, orbitProgress + ORBIT_ADVANCE_PER_TICK);
					double angle = orbitStartAngle + orbitProgress * orbitDirection;
					desiredTarget = new Vec3(tx + Math.cos(angle) * ORBIT_RADIUS, desiredAltitude, tz + Math.sin(angle) * ORBIT_RADIUS);
					if (orbitProgress >= Math.PI - 0.01) {
						attackStage = ATTACK_STAGE_TERMINAL;
					}
				} else if (attackStage == ATTACK_STAGE_TERMINAL) {
					double diveFactor = horizontalDistance <= DIVE_START_DISTANCE ? 1.0 - (horizontalDistance / DIVE_START_DISTANCE) : 0.0;
					diveFactor = Math.max(0.0, Math.min(1.0, diveFactor));
					desiredAltitude = (targetGround + APPROACH_ALTITUDE_CLEARANCE) * (1.0 - diveFactor) + ty * diveFactor;
					desiredTarget = new Vec3(tx, desiredAltitude, tz);
				} else {
					desiredTarget = new Vec3(tx, desiredAltitude, tz);
				}

				Vec3 obstacleCheckDirection = new Vec3(desiredTarget.x - pos.x, 0.0, desiredTarget.z - pos.z);
				double desiredY = desiredTarget.y;
				if (obstacleCheckDirection.lengthSqr() > 1.0e-6) {
					obstacleCheckDirection = obstacleCheckDirection.normalize();
					Vec3 clipStart = pos.add(0.0, 0.25, 0.0);
					Vec3 clipEnd = clipStart.add(obstacleCheckDirection.scale(Math.min(horizontalDistance, OBSTACLE_LOOKAHEAD)));
					BlockHitResult obstacleHit = this.level().clip(new ClipContext(clipStart, clipEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
					if (obstacleHit.getType() == HitResult.Type.BLOCK) {
						double obstacleTopY = obstacleHit.getBlockPos().getY() + 1.0;
						desiredY = Math.max(desiredY, obstacleTopY + OBSTACLE_VERTICAL_CLEARANCE);
					}
				}

				if (Double.isNaN(this.smoothedDesiredY)) {
					this.smoothedDesiredY = desiredY;
				} else {
					this.smoothedDesiredY = Mth.lerp(GUIDANCE_Y_SMOOTHING, this.smoothedDesiredY, desiredY);
				}
				desiredY = this.smoothedDesiredY;

				Vec3 desiredDirection = new Vec3(desiredTarget.x - pos.x, desiredY - pos.y, desiredTarget.z - pos.z);
				if (desiredDirection.lengthSqr() > 1.0e-6) {
					desiredDirection = desiredDirection.normalize();
				}
				if (this.smoothedDesiredDirection.lengthSqr() <= 1.0e-6) {
					this.smoothedDesiredDirection = desiredDirection;
				} else if (desiredDirection.lengthSqr() > 1.0e-6) {
					this.smoothedDesiredDirection = this.smoothedDesiredDirection.lerp(desiredDirection, GUIDANCE_DIRECTION_SMOOTHING).normalize();
				}
				desiredDirection = this.smoothedDesiredDirection;

				double currentSpeed = Math.max(2.0, currentVel.length());
				double desiredSpeed = horizontalDistance < 60.0 ? TERMINAL_SPEED : CRUISE_SPEED;
				double speed = currentSpeed * 0.8 + desiredSpeed * 0.2;
				double alignment = desiredDirection.lengthSqr() > 1.0e-6 ? currentDir.dot(desiredDirection) : 1.0;
				double turnNeed = Mth.clamp((1.0 - alignment) * 0.5, 0.0, 1.0);
				double turnBlend = Mth.lerp(turnNeed, MIN_TURN_BLEND, MAX_TURN_BLEND);
				if (horizontalDistance < 90.0) {
					turnBlend = Math.max(turnBlend, 0.12);
				}
				double maxTurnDegrees = Mth.clamp((this.poweredFlightTicks - INITIAL_STRAIGHT_TICKS) * 0.15D, 1.0D, 36.0D);
				if (horizontalDistance < 90.0) maxTurnDegrees = Math.max(maxTurnDegrees, 12.0D);
				Vec3 turnedDirection = SuperbWarfareFlightModel.turnToward(currentDir, desiredDirection, maxTurnDegrees);
				if (this.isManeuvering() && horizontalDistance > 40.0) {
					Vec3 rightVector = new Vec3(-desiredDirection.z, 0.0, desiredDirection.x);
					if (rightVector.lengthSqr() > 1.0e-6) {
						double weaveAngle = Math.sin(this.tickCount * 0.25) * 0.20;
						turnedDirection = turnedDirection.add(rightVector.normalize().scale(weaveAngle)).normalize();
					}
				}
				Vec3 newVelocity = turnedDirection.scale(speed);
				if (newVelocity.lengthSqr() > 1.0e-6) {
					this.setDeltaMovement(newVelocity);
					this.setYRot((float) (Math.toDegrees(Math.atan2(newVelocity.z, newVelocity.x)) - 90.0));
					double horizSpeed = Math.sqrt(newVelocity.x * newVelocity.x + newVelocity.z * newVelocity.z);
					this.setXRot((float) (-Math.toDegrees(Math.atan2(newVelocity.y, Math.max(horizSpeed, 0.001)))));
				}

				if (horizontalDistance <= TERMINAL_DETONATION_DISTANCE && Math.abs(pos.y - ty) <= 6.0) {
					TomahawktrueKoghdaSnariadPopadaietVBlokProcedure.execute(this.level(), tx, ty, tz, this);
					ProjectileChunkLoader.release(this);
					this.discard();
					return;
				}
			}
			}
		}
		Level _level = this.level();
		if (_level.isClientSide() && this.isEngineActive()) {
			DsfsdsfPriObnovlieniiTikaProcedure.execute(_level, this.getX(), this.getY(), this.getZ());
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

	public void setTarget(double x, double y, double z) {
		this.hasTarget = true;
		this.targetX = x;
		this.targetY = y;
		this.targetZ = z;
		this.attackStage = ATTACK_STAGE_CRUISE;
		this.orbitDirection = 1;
		this.orbitProgress = 0.0;
		this.orbitStartAngle = 0.0;
		this.smoothedDesiredY = Double.NaN;
		this.smoothedDesiredDirection = Vec3.ZERO;
	}

	public void armDelayedActivation(boolean launchedFromConstruction, double radioAltitude) {
		this.setActivationDelayTicks(0);
		this.setWaitingForLowAltitudeActivation(radioAltitude > DROP_MODE_TRIGGER_ALTITUDE);
	}

	public boolean isEngineActive() {
		return !this.engineCutoff && !this.isWaitingForLowAltitudeActivation() && this.getActivationDelayTicks() <= 0;
	}

	public int getActivationDelayTicks() {
		return this.entityData.get(ACTIVATION_DELAY);
	}

	private void setActivationDelayTicks(int ticks) {
		this.entityData.set(ACTIVATION_DELAY, Math.max(0, ticks));
	}

	private boolean isWaitingForLowAltitudeActivation() {
		return this.entityData.get(WAITING_FOR_LOW_ALTITUDE_ACTIVATION);
	}

	private void setWaitingForLowAltitudeActivation(boolean waiting) {
		this.entityData.set(WAITING_FOR_LOW_ALTITUDE_ACTIVATION, waiting);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putBoolean("HasTarget", this.hasTarget);
		tag.putDouble("TargetX", this.targetX);
		tag.putDouble("TargetY", this.targetY);
		tag.putDouble("TargetZ", this.targetZ);
		tag.putInt("AttackStage", this.attackStage);
		tag.putInt("OrbitDirection", this.orbitDirection);
		tag.putDouble("OrbitProgress", this.orbitProgress);
		tag.putDouble("OrbitStartAngle", this.orbitStartAngle);
		tag.putDouble("SmoothedDesiredY", this.smoothedDesiredY);
		tag.putDouble("SmoothedDesiredDirX", this.smoothedDesiredDirection.x);
		tag.putDouble("SmoothedDesiredDirY", this.smoothedDesiredDirection.y);
		tag.putDouble("SmoothedDesiredDirZ", this.smoothedDesiredDirection.z);
		tag.putInt("ActivationDelay", this.getActivationDelayTicks());
		tag.putBoolean("WaitingForLowAltitudeActivation", this.isWaitingForLowAltitudeActivation());
		tag.putInt("PoweredFlightTicks", this.poweredFlightTicks);
		tag.putDouble("PoweredDistanceTravelled", this.poweredDistanceTravelled);
		tag.putBoolean("EngineCutoff", this.engineCutoff);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.hasTarget = tag.getBoolean("HasTarget");
		this.targetX = tag.getDouble("TargetX");
		this.targetY = tag.getDouble("TargetY");
		this.targetZ = tag.getDouble("TargetZ");
		this.attackStage = tag.contains("AttackStage") ? tag.getInt("AttackStage") : ATTACK_STAGE_CRUISE;
		this.orbitDirection = tag.contains("OrbitDirection") ? tag.getInt("OrbitDirection") : 1;
		this.orbitProgress = tag.contains("OrbitProgress") ? tag.getDouble("OrbitProgress") : 0.0;
		this.orbitStartAngle = tag.contains("OrbitStartAngle") ? tag.getDouble("OrbitStartAngle") : 0.0;
		this.smoothedDesiredY = tag.contains("SmoothedDesiredY") ? tag.getDouble("SmoothedDesiredY") : Double.NaN;
		if (tag.contains("SmoothedDesiredDirX")) {
			this.smoothedDesiredDirection = new Vec3(tag.getDouble("SmoothedDesiredDirX"), tag.getDouble("SmoothedDesiredDirY"), tag.getDouble("SmoothedDesiredDirZ"));
		} else {
			this.smoothedDesiredDirection = Vec3.ZERO;
		}
		this.setActivationDelayTicks(tag.getInt("ActivationDelay"));
		this.setWaitingForLowAltitudeActivation(tag.getBoolean("WaitingForLowAltitudeActivation"));
		this.poweredFlightTicks = tag.getInt("PoweredFlightTicks");
		this.poweredDistanceTravelled = tag.getDouble("PoweredDistanceTravelled");
		this.engineCutoff = tag.getBoolean("EngineCutoff");
	}

	private double getCurrentRadioAltitude() {
		if (this.level() instanceof ServerLevel serverLevel) {
			return ProjectileLaunchHelper.getRadioAltitude(serverLevel, this.position());
		}
		return Double.MAX_VALUE;
	}

	private void updateAttackPattern(double horizontalDistance, Vec3 pos, Vec3 currentDir) {
		// A cruise missile must commit to the target, not enter the old half-orbit.
		// Also upgrades missiles loaded from an older save while already in orbit.
		if (horizontalDistance <= ATTACK_PATTERN_TRIGGER_DISTANCE ||
			attackStage == ATTACK_STAGE_INGRESS || attackStage == ATTACK_STAGE_ORBIT) {
			attackStage = ATTACK_STAGE_TERMINAL;
			orbitProgress = 0.0;
		}
	}

	private Vec3 getOrbitOffset() {
		Vec3 toTargetFlat = new Vec3(targetX - this.getX(), 0.0, targetZ - this.getZ());
		if (toTargetFlat.lengthSqr() <= 1.0e-6) {
			return new Vec3(ORBIT_RADIUS, 0.0, 0.0);
		}
		toTargetFlat = toTargetFlat.normalize();
		Vec3 left = new Vec3(-toTargetFlat.z, 0.0, toTargetFlat.x);
		return left.scale(ORBIT_RADIUS * orbitDirection);
	}

	private double getGroundHeight(double x, double z) {
		int solid = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(x), (int) Math.floor(z));
		int surface = this.level().getHeight(Heightmap.Types.WORLD_SURFACE, (int) Math.floor(x), (int) Math.floor(z));
		return Math.max(solid, surface);
	}

	public static TomahawkbultEntity shoot(Level world, LivingEntity entity, RandomSource source) {
		return shoot(world, entity, source, 2f, 15, 15);
	}

	public static TomahawkbultEntity shoot(Level world, LivingEntity entity, RandomSource source, float pullingPower) {
		return shoot(world, entity, source, pullingPower * 2f, 15, 15);
	}

	public static TomahawkbultEntity shoot(Level world, LivingEntity entity, RandomSource random, float power, double damage, int knockback) {
		TomahawkbultEntity entityarrow = new TomahawkbultEntity(CreateTheAirWarsModEntities.TOMAHAWKBULT.get(), entity, world);
		entityarrow.shoot(entity.getViewVector(1).x, entity.getViewVector(1).y, entity.getViewVector(1).z, power * 2.8f, 0);
		entityarrow.setSilent(true);
		entityarrow.setCritArrow(false);
		entityarrow.setBaseDamage(damage);
		// // removed knockback
		world.addFreshEntity(entityarrow);
		return entityarrow;
	}

	public static TomahawkbultEntity shoot(LivingEntity entity, LivingEntity target) {
		TomahawkbultEntity entityarrow = new TomahawkbultEntity(CreateTheAirWarsModEntities.TOMAHAWKBULT.get(), entity, entity.level());
		double dx = target.getX() - entity.getX();
		double dy = target.getY() + target.getEyeHeight() - 1.1;
		double dz = target.getZ() - entity.getZ();
		entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * 0.2F, dz, 2f * 2.8f, 12.0F);
		entityarrow.setSilent(true);
		entityarrow.setBaseDamage(15);
		// removed knockback
		entityarrow.setCritArrow(false);
		entity.level().addFreshEntity(entityarrow);
		return entityarrow;
	}
}
