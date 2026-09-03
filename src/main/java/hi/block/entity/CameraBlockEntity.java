package hi.block.entity;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import hi.block.CameraBlock;
import hi.init.CreateTheAirWarsModBlockEntities;
import hi.util.SableCoordinateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;
import java.util.UUID;

public class CameraBlockEntity extends BlockEntity {
    private static final double LOCK_RANGE = 256.0D;
    private static final float LERP_FACTOR = 0.36F;

    private UUID cameraId = UUID.randomUUID();
    private float yaw;
    private float pitch;
    private float targetYaw;
    private float targetPitch;
    private boolean rotationInitialized;
    private boolean locked;
    private Vec3 lockedTarget;
    private UUID lockedTargetSubLevelId;
    private Vec3 lockedTargetSubLevelLocal;
    private transient boolean renderRotationInitialized;
    private transient float renderYaw;
    private transient float renderPitch;

    public CameraBlockEntity(BlockPos pos, BlockState state) {
        super(CreateTheAirWarsModBlockEntities.CAMERA.get(), pos, state);
    }

    public UUID getCameraId() {
        return this.cameraId;
    }

    public float getYaw() {
        this.ensureRotationInitialized();
        return this.yaw;
    }

    public float getPitch() {
        this.ensureRotationInitialized();
        return this.pitch;
    }

    public float getRenderYaw(float partialTick) {
        this.ensureRotationInitialized();
        if (this.level == null || !this.level.isClientSide) {
            return this.yaw;
        }
        this.ensureRenderRotationInitialized();
        float smoothing = Mth.clamp(0.24F + partialTick * 0.16F, 0.24F, 0.42F);
        this.renderYaw = Mth.rotLerp(smoothing, this.renderYaw, this.yaw);
        return this.renderYaw;
    }

    public float getRenderPitch(float partialTick) {
        this.ensureRotationInitialized();
        if (this.level == null || !this.level.isClientSide) {
            return this.pitch;
        }
        this.ensureRenderRotationInitialized();
        float smoothing = Mth.clamp(0.24F + partialTick * 0.16F, 0.24F, 0.42F);
        this.renderPitch = Mth.lerp(smoothing, this.renderPitch, this.pitch);
        return this.renderPitch;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public Vec3 getLockedTarget() {
        return this.resolveLockedTargetWorld();
    }

    public Vec3 getGuidanceTarget() {
        Vec3 lockedTarget = this.resolveLockedTargetWorld();
        if (this.locked && lockedTarget != null) {
            return lockedTarget;
        }
        LockTarget target = this.findLockTarget();
        if (target != null) {
            return target.worldTarget();
        }
        return this.getWorldEyePosition().add(this.getWorldLookDirection().scale(LOCK_RANGE));
    }

    public Vec3 getWorldEyePosition() {
        return SableCoordinateHelper.projectOut(this.level, this.getCameraEyePosition());
    }

    public Vec3 getWorldLookDirection() {
        this.ensureRotationInitialized();
        return SableCoordinateHelper.projectDirectionOut(this.level, this.getCameraEyePosition(), Vec3.directionFromRotation(this.pitch, this.yaw));
    }

    public void adjustTargetRotation(float yawDelta, float pitchDelta) {
        this.ensureRotationInitialized();
        this.targetYaw = Mth.wrapDegrees(this.targetYaw + yawDelta);
        this.targetPitch = Mth.clamp(this.targetPitch + pitchDelta, -89.9F, 89.9F);
        if (this.locked) {
            this.setLockTarget(this.findLockTarget());
        }
        this.setChangedAndSync();
    }

    public void toggleLock() {
        this.ensureRotationInitialized();
        if (this.locked) {
            this.locked = false;
            this.clearLockTarget();
            this.setChangedAndSync();
            return;
        }

        this.locked = true;
        this.setLockTarget(this.findLockTarget());
        this.setChangedAndSync();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CameraBlockEntity camera) {
        camera.ensureRotationInitialized();
        Vec3 resolvedLockedTarget = camera.resolveLockedTargetWorld();
        if (camera.locked && resolvedLockedTarget != null) {
            camera.pointTargetAt(resolvedLockedTarget);
        }

        float nextYaw = Mth.rotLerp(LERP_FACTOR, camera.yaw, camera.targetYaw);
        float nextPitch = Mth.lerp(LERP_FACTOR, camera.pitch, camera.targetPitch);
        if (Math.abs(Mth.wrapDegrees(nextYaw - camera.yaw)) > 0.01F || Math.abs(nextPitch - camera.pitch) > 0.01F) {
            camera.yaw = Mth.wrapDegrees(nextYaw);
            camera.pitch = Mth.clamp(nextPitch, -89.9F, 89.9F);
            camera.setChangedAndSync();
        }
    }

    private void pointTargetAt(Vec3 target) {
        Vec3 localOrigin = this.getCameraEyePosition();
        Vec3 worldOrigin = SableCoordinateHelper.projectOut(this.level, localOrigin);
        Vec3 worldLook = target.subtract(worldOrigin).normalize();
        Vec3 look = SableCoordinateHelper.projectDirectionIn(this.level, localOrigin, worldLook);
        this.targetYaw = (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(-look.x, look.z)));
        this.targetPitch = (float) Mth.clamp(Mth.wrapDegrees(-Math.toDegrees(Math.asin(look.y))), -89.9D, 89.9D);
    }

    private LockTarget findLockTarget() {
        Vec3 localFrom = this.getCameraEyePosition();
        Vec3 from = SableCoordinateHelper.projectOut(this.level, localFrom);
        Vec3 direction = SableCoordinateHelper.projectDirectionOut(this.level, localFrom, Vec3.directionFromRotation(this.targetPitch, this.targetYaw));
        Vec3 to = from.add(direction.scale(LOCK_RANGE));
        ServerLevel searchLevel = SableCoordinateHelper.resolveCollisionLevel(this.level);

        LockTarget bestTarget = this.findEntityTarget(searchLevel, from, to);
        bestTarget = this.pickNearest(from, bestTarget, this.findWorldBlockTarget(searchLevel, from, to));
        bestTarget = this.pickNearest(from, bestTarget, this.findSubLevelBlockTarget(searchLevel, from, to));
        return bestTarget != null ? bestTarget : new LockTarget(to, null, null);
    }

    private LockTarget findEntityTarget(ServerLevel searchLevel, Vec3 from, Vec3 to) {
        if (searchLevel == null) {
            return null;
        }
        AABB searchBox = new AABB(from, to).inflate(1.0D);
        List<Entity> entities = searchLevel.getEntities((Entity) null, searchBox, entity -> entity.isAlive() && entity.isPickable());
        double bestDistance = Double.MAX_VALUE;
        LockTarget bestTarget = null;
        for (Entity entity : entities) {
            AABB box = entity.getBoundingBox().inflate(entity.getPickRadius() + 0.35D);
            java.util.Optional<Vec3> hit = box.clip(from, to);
            if (hit.isPresent()) {
                double distance = from.distanceToSqr(hit.get());
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestTarget = new LockTarget(entity.getBoundingBox().getCenter(), null, null);
                }
            }
        }
        return bestTarget;
    }

    private LockTarget findWorldBlockTarget(ServerLevel searchLevel, Vec3 from, Vec3 to) {
        if (searchLevel == null) {
            return null;
        }
        BlockHitResult hit = searchLevel.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        if (hit.getType() == HitResult.Type.MISS) {
            return null;
        }
        return new LockTarget(hit.getLocation(), null, null);
    }

    private LockTarget findSubLevelBlockTarget(ServerLevel searchLevel, Vec3 from, Vec3 to) {
        if (searchLevel == null) {
            return null;
        }

        ServerSubLevelContainer container = ServerSubLevelContainer.getContainer(searchLevel);
        if (container == null) {
            return null;
        }

        LockTarget bestTarget = null;
        double bestDistance = Double.MAX_VALUE;
        for (ServerSubLevel subLevel : container.getAllSubLevels()) {
            if (subLevel == null || subLevel.isRemoved()) {
                continue;
            }

            Vec3 localFrom = SableCoordinateHelper.worldToLocal(subLevel, from);
            Vec3 localTo = SableCoordinateHelper.worldToLocal(subLevel, to);
            ServerLevel clipLevel = subLevel.getLevel();
            BlockHitResult hit = clipLevel.clip(new ClipContext(localFrom, localTo, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
            if (hit.getType() == HitResult.Type.MISS) {
                java.util.Optional<Vec3> boundsHit = subLevel.boundingBox().toMojang().clip(from, to);
                if (boundsHit.isPresent()) {
                    Vec3 worldHit = boundsHit.get();
                    Vec3 localHit = SableCoordinateHelper.worldToLocal(subLevel, worldHit);
                    double distance = from.distanceToSqr(worldHit);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestTarget = new LockTarget(worldHit, subLevel.getUniqueId(), localHit);
                    }
                }
                continue;
            }

            SubLevel hitSubLevel = dev.ryanhcode.sable.Sable.HELPER.getContaining(clipLevel, hit.getBlockPos());
            if (hitSubLevel == null || !subLevel.getUniqueId().equals(hitSubLevel.getUniqueId())) {
                continue;
            }

            Vec3 localHit = hit.getLocation();
            Vec3 worldHit = SableCoordinateHelper.localToWorld(subLevel, localHit);
            double distance = from.distanceToSqr(worldHit);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestTarget = new LockTarget(worldHit, subLevel.getUniqueId(), localHit);
            }
        }

        return bestTarget;
    }

    private Vec3 getCameraEyePosition() {
        Direction facing = this.getBlockState().getValue(CameraBlock.FACING);
        return this.worldPosition.getCenter().add(Vec3.atLowerCornerOf(facing.getNormal()).scale(0.35D));
    }

    private void ensureRotationInitialized() {
        if (this.rotationInitialized) {
            return;
        }
        Direction facing = this.getBlockState().getValue(CameraBlock.FACING);
        this.yaw = facing == Direction.UP ? this.getBlockState().getValue(CameraBlock.HORIZONTAL_FACING).toYRot() : (facing.getAxis().isHorizontal() ? facing.toYRot() : 0.0F);
        this.pitch = switch (facing) {
            case UP -> 0.0F;
            case DOWN -> 90.0F;
            default -> 0.0F;
        };
        this.targetYaw = this.yaw;
        this.targetPitch = this.pitch;
        this.rotationInitialized = true;
    }

    private void ensureRenderRotationInitialized() {
        if (this.renderRotationInitialized) {
            return;
        }
        this.renderYaw = this.yaw;
        this.renderPitch = this.pitch;
        this.renderRotationInitialized = true;
    }

    private Vec3 resolveLockedTargetWorld() {
        if (this.lockedTargetSubLevelId != null && this.lockedTargetSubLevelLocal != null) {
            ServerLevel searchLevel = SableCoordinateHelper.resolveCollisionLevel(this.level);
            SubLevel subLevel = SableCoordinateHelper.resolveSubLevel(searchLevel, this.lockedTargetSubLevelId);
            if (subLevel != null) {
                return SableCoordinateHelper.localToWorld(subLevel, this.lockedTargetSubLevelLocal);
            }
        }
        return this.lockedTarget;
    }

    private void setLockTarget(LockTarget target) {
        if (target == null) {
            this.clearLockTarget();
            return;
        }
        this.lockedTarget = target.worldTarget();
        this.lockedTargetSubLevelId = target.subLevelId();
        this.lockedTargetSubLevelLocal = target.subLevelLocalTarget();
    }

    private void clearLockTarget() {
        this.lockedTarget = null;
        this.lockedTargetSubLevelId = null;
        this.lockedTargetSubLevelLocal = null;
    }

    private LockTarget pickNearest(Vec3 origin, LockTarget currentBest, LockTarget candidate) {
        if (candidate == null) {
            return currentBest;
        }
        if (currentBest == null) {
            return candidate;
        }
        double currentDistance = origin.distanceToSqr(currentBest.worldTarget());
        double candidateDistance = origin.distanceToSqr(candidate.worldTarget());
        return candidateDistance < currentDistance ? candidate : currentBest;
    }

    private void setChangedAndSync() {
        this.setChanged();
        if (this.level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putUUID("CameraId", this.cameraId);
        tag.putBoolean("RotationInitialized", this.rotationInitialized);
        tag.putFloat("Yaw", this.yaw);
        tag.putFloat("Pitch", this.pitch);
        tag.putFloat("TargetYaw", this.targetYaw);
        tag.putFloat("TargetPitch", this.targetPitch);
        tag.putBoolean("Locked", this.locked);
        if (this.lockedTarget != null) {
            tag.putDouble("LockedTargetX", this.lockedTarget.x);
            tag.putDouble("LockedTargetY", this.lockedTarget.y);
            tag.putDouble("LockedTargetZ", this.lockedTarget.z);
        }
        if (this.lockedTargetSubLevelId != null) {
            tag.putUUID("LockedTargetSubLevelId", this.lockedTargetSubLevelId);
        }
        if (this.lockedTargetSubLevelLocal != null) {
            tag.putDouble("LockedTargetSubLevelLocalX", this.lockedTargetSubLevelLocal.x);
            tag.putDouble("LockedTargetSubLevelLocalY", this.lockedTargetSubLevelLocal.y);
            tag.putDouble("LockedTargetSubLevelLocalZ", this.lockedTargetSubLevelLocal.z);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.hasUUID("CameraId")) {
            this.cameraId = tag.getUUID("CameraId");
        }
        this.rotationInitialized = tag.getBoolean("RotationInitialized");
        this.yaw = tag.getFloat("Yaw");
        this.pitch = tag.getFloat("Pitch");
        this.targetYaw = tag.getFloat("TargetYaw");
        this.targetPitch = tag.getFloat("TargetPitch");
        this.locked = tag.getBoolean("Locked");
        this.renderRotationInitialized = false;
        if (tag.contains("LockedTargetX")) {
            this.lockedTarget = new Vec3(tag.getDouble("LockedTargetX"), tag.getDouble("LockedTargetY"), tag.getDouble("LockedTargetZ"));
        } else {
            this.lockedTarget = null;
        }
        this.lockedTargetSubLevelId = tag.hasUUID("LockedTargetSubLevelId") ? tag.getUUID("LockedTargetSubLevelId") : null;
        if (tag.contains("LockedTargetSubLevelLocalX")) {
            this.lockedTargetSubLevelLocal = new Vec3(
                tag.getDouble("LockedTargetSubLevelLocalX"),
                tag.getDouble("LockedTargetSubLevelLocalY"),
                tag.getDouble("LockedTargetSubLevelLocalZ")
            );
        } else {
            this.lockedTargetSubLevelLocal = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private record LockTarget(Vec3 worldTarget, UUID subLevelId, Vec3 subLevelLocalTarget) {
    }
}
