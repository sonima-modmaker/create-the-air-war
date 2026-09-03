package hi.block.entity;

import hi.block.VihrLauncherBlock;
import hi.entity.VihrRocketEntity;
import hi.init.CreateTheAirWarsModBlockEntities;
import hi.init.CreateTheAirWarsModEntities;
import hi.util.SableCoordinateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class VihrLauncherBlockEntity extends BlockEntity implements MonitorLaunchableBlockEntity {
    public static final int MAX_ROCKETS = 6;
    private static final int ALL_ROCKETS_LOADED_MASK = (1 << MAX_ROCKETS) - 1;
    private static final int PANEL_ANIMATION_TICKS = 4;
    private static final int LAUNCH_SPAWN_DELAY_TICKS = 2;
    private static final float PANEL_OPEN_ANGLE_DEGREES = 115.0F;
    private static final Vec3[] ROCKET_SLOT_CENTERS = new Vec3[] {
        new Vec3(12.5D / 16.0D, 8.5D / 16.0D, 9.5D / 16.0D), // Panel 1 (Top Right) -> slot 0
        new Vec3(3.5D / 16.0D, 8.5D / 16.0D, 9.5D / 16.0D),  // Panel 2 (Top Left) -> slot 1
        new Vec3(14.5D / 16.0D, 3.5D / 16.0D, 9.5D / 16.0D), // Panel 3 (Bottom Right outer) -> slot 2
        new Vec3(1.5D / 16.0D, 3.5D / 16.0D, 9.5D / 16.0D),  // Panel 4 (Bottom Left outer) -> slot 3
        new Vec3(6.0D / 16.0D, 3.5D / 16.0D, 9.5D / 16.0D),  // Panel 5 (Bottom Left inner) -> slot 4
        new Vec3(10.0D / 16.0D, 3.5D / 16.0D, 9.5D / 16.0D)  // Panel 6 (Bottom Right inner) -> slot 5
    };

    private int rocketCount = MAX_ROCKETS;
    private int loadedRocketMask = ALL_ROCKETS_LOADED_MASK;
    private int openPanelMask = 0;
    private final int[] panelAnimationDirections = new int[MAX_ROCKETS];
    private final int[] panelAnimationTicks = new int[MAX_ROCKETS];
    private int pendingLaunchSlot = -1;
    private int pendingLaunchTicks = -1;
    private BlockPos pendingCameraPos;

    private float yaw;
    private float pitch;
    private float targetYaw;
    private float targetPitch;
    private boolean rotationInitialized;
    private float placementYaw = -999.0F;
    private transient boolean renderRotationInitialized;
    private transient float renderYaw;
    private transient float renderPitch;

    public VihrLauncherBlockEntity(BlockPos pos, BlockState state) {
        super(CreateTheAirWarsModBlockEntities.VIHR.get(), pos, state);
    }

    public int getRocketCount() {
        return this.rocketCount;
    }

    public boolean hasAmmo() {
        return this.rocketCount > 0;
    }

    @Override
    public boolean hasLaunchPayload() {
        return this.hasAmmo();
    }

    @Override
    public int getLaunchPayloadCount() {
        return this.rocketCount;
    }

    @Override
    public int getMaxLaunchPayloadCount() {
        return MAX_ROCKETS;
    }

    public boolean hasRocketInSlot(int slotIndex) {
        return slotIndex >= 0 && slotIndex < MAX_ROCKETS && (this.loadedRocketMask & (1 << slotIndex)) != 0;
    }

    public float getPanelAngleDegrees(int slotIndex, float partialTick) {
        if (slotIndex < 0 || slotIndex >= MAX_ROCKETS) {
            return 0.0F;
        }
        int direction = this.panelAnimationDirections[slotIndex];
        if (direction > 0) {
            float progress = Mth.clamp((this.panelAnimationTicks[slotIndex] + partialTick) / PANEL_ANIMATION_TICKS, 0.0F, 1.0F);
            return PANEL_OPEN_ANGLE_DEGREES * progress;
        }
        if (direction < 0) {
            float progress = Mth.clamp((this.panelAnimationTicks[slotIndex] + partialTick) / PANEL_ANIMATION_TICKS, 0.0F, 1.0F);
            return PANEL_OPEN_ANGLE_DEGREES * (1.0F - progress);
        }
        return this.isPanelOpen(slotIndex) ? PANEL_OPEN_ANGLE_DEGREES : 0.0F;
    }

    public boolean isPanelOpen(int slotIndex) {
        return slotIndex >= 0 && slotIndex < MAX_ROCKETS && (this.openPanelMask & (1 << slotIndex)) != 0;
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

    private void ensureRotationInitialized() {
        if (this.rotationInitialized) {
            return;
        }
        Direction facing = this.getBlockState().getValue(VihrLauncherBlock.FACING);
        if (this.placementYaw == -999.0F) {
            this.placementYaw = this.getBlockState().getValue(VihrLauncherBlock.HORIZONTAL_FACING).toYRot();
        }
        this.yaw = this.placementYaw;
        this.pitch = 0.0F;
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

    public void initializeRotationFromPlacer(net.minecraft.world.entity.LivingEntity placer) {
        float yawSnapped = this.getBlockState().getValue(VihrLauncherBlock.HORIZONTAL_FACING).toYRot();
        this.placementYaw = yawSnapped;
        this.yaw = yawSnapped;
        this.pitch = 0.0F;
        this.targetYaw = this.yaw;
        this.targetPitch = this.pitch;
        this.rotationInitialized = true;
        this.setChangedAndSync();
    }

    public void setTarget(Vec3 target) {
        this.ensureRotationInitialized();
    }

    public void clearTarget() {
        this.ensureRotationInitialized();
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        this.ensureRotationInitialized();
        this.tickPanelAnimations(level.isClientSide);
        if (!level.isClientSide) {
            this.tickPendingLaunch((ServerLevel) level);
        }
    }

    public boolean addRocket() {
        if (this.rocketCount >= MAX_ROCKETS) {
            return false;
        }
        int slotIndex = this.getNextEmptyRocketSlot();
        if (slotIndex < 0) {
            this.setChangedAndSync();
            return false;
        }
        this.loadedRocketMask |= 1 << slotIndex;
        this.rocketCount = Integer.bitCount(this.loadedRocketMask);
        if (this.isPanelOpen(slotIndex) || this.panelAnimationDirections[slotIndex] > 0) {
            this.startPanelAnimation(slotIndex, false);
        }
        this.setChangedAndSync();
        return true;
    }

    public boolean launch(CameraBlockEntity camera) {
        if (!(this.level instanceof ServerLevel) || camera == null || this.rocketCount <= 0 || this.pendingLaunchSlot >= 0) {
            return false;
        }

        int launchSlot = this.getNextLoadedRocketSlot();
        if (launchSlot < 0) {
            return false;
        }
        this.startPanelAnimation(launchSlot, true);
        this.pendingLaunchSlot = launchSlot;
        this.pendingLaunchTicks = 0;
        this.pendingCameraPos = camera.getBlockPos().immutable();
        this.setChangedAndSync();
        return true;
    }

    private Vec3 getLocalLaunchPosition(int slotIndex) {
        Vec3 slotCenter = ROCKET_SLOT_CENTERS[Mth.clamp(slotIndex, 0, ROCKET_SLOT_CENTERS.length - 1)];
        Matrix4f transform = new Matrix4f()
            .translate(0.5F, 0.5F, 0.5F);
        
        Direction facing = this.getBlockState().getValue(VihrLauncherBlock.FACING);
        Direction horizontalFacing = this.getBlockState().getValue(VihrLauncherBlock.HORIZONTAL_FACING);
        
        float mountYawRotation = 0.0F;
        if (facing == Direction.UP || facing == Direction.DOWN) {
            mountYawRotation = 180.0F - horizontalFacing.toYRot();
        } else {
            switch (facing) {
                case SOUTH -> mountYawRotation = 180.0F;
                case WEST -> mountYawRotation = 270.0F;
                case EAST -> mountYawRotation = 90.0F;
                default -> mountYawRotation = 0.0F;
            }
        }
        transform.rotate((float) Math.toRadians(mountYawRotation), 0.0F, 1.0F, 0.0F);
        
        applyMountRotation(transform, facing);
        float relativeYaw = this.getYaw() - horizontalFacing.toYRot();
        transform.rotate((float) Math.toRadians(-relativeYaw), 0.0F, 1.0F, 0.0F);
        transform.rotate((float) Math.toRadians(180.0D), 1.0F, 0.0F, 0.0F);
        
        transform.translate(-0.5F, -0.5F, -0.5F);

        Vector3f transformed = transform.transformPosition(new Vector3f((float) slotCenter.x, (float) slotCenter.y, (float) slotCenter.z));
        return Vec3.atLowerCornerOf(this.worldPosition).add(transformed.x, transformed.y, transformed.z);
    }

    private Vec3 getLocalLaunchDirection() {
        this.ensureRotationInitialized();
        return Vec3.directionFromRotation(this.pitch, this.yaw).scale(1.0D);
    }

    private void tickPanelAnimations(boolean clientSide) {
        boolean completedAnimation = false;
        for (int slotIndex = 0; slotIndex < MAX_ROCKETS; slotIndex++) {
            int direction = this.panelAnimationDirections[slotIndex];
            if (direction == 0) {
                continue;
            }
            this.panelAnimationTicks[slotIndex]++;
            if (this.panelAnimationTicks[slotIndex] >= PANEL_ANIMATION_TICKS) {
                if (direction > 0) {
                    this.openPanelMask |= 1 << slotIndex;
                } else {
                    this.openPanelMask &= ~(1 << slotIndex);
                }
                this.panelAnimationDirections[slotIndex] = 0;
                this.panelAnimationTicks[slotIndex] = 0;
                completedAnimation = true;
            }
        }
        if (completedAnimation && !clientSide) {
            this.setChangedAndSync();
        }
    }

    private void tickPendingLaunch(ServerLevel serverLevel) {
        if (this.pendingLaunchSlot < 0 || this.pendingLaunchTicks < 0) {
            return;
        }
        this.pendingLaunchTicks++;
        if (this.pendingLaunchTicks < PANEL_ANIMATION_TICKS + LAUNCH_SPAWN_DELAY_TICKS) {
            return;
        }

        int launchSlot = this.pendingLaunchSlot;
        BlockPos cameraPos = this.pendingCameraPos;
        this.pendingLaunchSlot = -1;
        this.pendingLaunchTicks = -1;
        this.pendingCameraPos = null;

        if (!this.hasRocketInSlot(launchSlot)) {
            this.setChangedAndSync();
            return;
        }

        Vec3 localSpawn = this.getLocalLaunchPosition(launchSlot);
        Vec3 launcherForward = this.getLocalLaunchDirection();
        Vec3 normalizedLaunchDirection = launcherForward.lengthSqr() > 1.0E-6D ? launcherForward.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 projectedSpawn = SableCoordinateHelper.projectOut(serverLevel, localSpawn);
        Vec3 projectedDirection = SableCoordinateHelper.projectDirectionOut(serverLevel, localSpawn, normalizedLaunchDirection);
        Vec3 launchPosition = projectedSpawn.lengthSqr() > 1.0E-8D ? projectedSpawn : localSpawn;
        Vec3 launchDir = projectedDirection.lengthSqr() > 1.0E-6D ? projectedDirection.normalize() : normalizedLaunchDirection;

        VihrRocketEntity rocket = new VihrRocketEntity(CreateTheAirWarsModEntities.VIHR_ROCKET.get(), serverLevel);
        rocket.setPos(launchPosition);
        if (cameraPos != null) {
            rocket.setCameraPos(cameraPos);
        }
        rocket.setInitialDirection(launchDir);
        rocket.setLaunchStartPos(launchPosition);
        rocket.shoot(launchDir.x, launchDir.y, launchDir.z, (float) VihrRocketEntity.INITIAL_FORWARD_SPEED, 0.0F);
        rocket.refreshOrientation();
        serverLevel.addFreshEntity(rocket);

        this.loadedRocketMask &= ~(1 << launchSlot);
        this.rocketCount = Integer.bitCount(this.loadedRocketMask);
        this.setChangedAndSync();
    }

    private void startPanelAnimation(int slotIndex, boolean opening) {
        if (slotIndex < 0 || slotIndex >= MAX_ROCKETS) {
            return;
        }
        this.panelAnimationDirections[slotIndex] = opening ? 1 : -1;
        this.panelAnimationTicks[slotIndex] = 0;
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
        tag.putInt("RocketCount", this.rocketCount);
        tag.putInt("LoadedRocketMask", this.loadedRocketMask);
        tag.putBoolean("RotationInitialized", this.rotationInitialized);
        tag.putFloat("Yaw", this.yaw);
        tag.putFloat("Pitch", this.pitch);
        tag.putFloat("TargetYaw", this.targetYaw);
        tag.putFloat("TargetPitch", this.targetPitch);
        tag.putFloat("PlacementYaw", this.placementYaw);
        tag.putInt("OpenPanelMask", this.openPanelMask);
        tag.putIntArray("PanelAnimationDirections", this.panelAnimationDirections);
        tag.putIntArray("PanelAnimationTicks", this.panelAnimationTicks);
        tag.putInt("PendingLaunchSlot", this.pendingLaunchSlot);
        tag.putInt("PendingLaunchTicks", this.pendingLaunchTicks);
        if (this.pendingCameraPos != null) {
            tag.putInt("PendingCameraX", this.pendingCameraPos.getX());
            tag.putInt("PendingCameraY", this.pendingCameraPos.getY());
            tag.putInt("PendingCameraZ", this.pendingCameraPos.getZ());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.rocketCount = Mth.clamp(tag.getInt("RocketCount"), 0, MAX_ROCKETS);
        if (tag.contains("LoadedRocketMask")) {
            this.loadedRocketMask = tag.getInt("LoadedRocketMask") & ALL_ROCKETS_LOADED_MASK;
            this.rocketCount = Integer.bitCount(this.loadedRocketMask);
        } else {
            this.loadedRocketMask = this.rocketCount <= 0 ? 0 : (1 << this.rocketCount) - 1;
        }
        this.rotationInitialized = tag.getBoolean("RotationInitialized");
        this.yaw = tag.getFloat("Yaw");
        this.pitch = tag.getFloat("Pitch");
        this.targetYaw = tag.getFloat("TargetYaw");
        this.targetPitch = tag.getFloat("TargetPitch");
        if (tag.contains("PlacementYaw")) {
            this.placementYaw = tag.getFloat("PlacementYaw");
        } else {
            this.placementYaw = -999.0F;
        }
        this.openPanelMask = tag.getInt("OpenPanelMask");
        this.copyIntArray(tag.getIntArray("PanelAnimationDirections"), this.panelAnimationDirections);
        this.copyIntArray(tag.getIntArray("PanelAnimationTicks"), this.panelAnimationTicks);
        this.pendingLaunchSlot = tag.contains("PendingLaunchSlot") ? tag.getInt("PendingLaunchSlot") : -1;
        this.pendingLaunchTicks = tag.contains("PendingLaunchTicks") ? tag.getInt("PendingLaunchTicks") : -1;
        if (tag.contains("PendingCameraX")) {
            this.pendingCameraPos = new BlockPos(tag.getInt("PendingCameraX"), tag.getInt("PendingCameraY"), tag.getInt("PendingCameraZ"));
        } else {
            this.pendingCameraPos = null;
        }
        this.renderRotationInitialized = false;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private int getNextLoadedRocketSlot() {
        for (int slotIndex = 0; slotIndex < MAX_ROCKETS; slotIndex++) {
            if (this.hasRocketInSlot(slotIndex)) {
                return slotIndex;
            }
        }
        return -1;
    }

    private int getNextEmptyRocketSlot() {
        for (int slotIndex = 0; slotIndex < MAX_ROCKETS; slotIndex++) {
            if (!this.hasRocketInSlot(slotIndex)) {
                return slotIndex;
            }
        }
        return -1;
    }

    private void copyIntArray(int[] source, int[] target) {
        for (int i = 0; i < target.length; i++) {
            target[i] = i < source.length ? source[i] : 0;
        }
    }

    private static void applyMountRotation(Matrix4f transform, Direction facing) {
        switch (facing) {
            case DOWN -> transform.rotate((float) Math.toRadians(180.0D), 1.0F, 0.0F, 0.0F);
            case NORTH -> transform.rotate((float) Math.toRadians(-90.0D), 1.0F, 0.0F, 0.0F);
            case SOUTH -> transform.rotate((float) Math.toRadians(90.0D), 1.0F, 0.0F, 0.0F);
            case EAST -> transform.rotate((float) Math.toRadians(-90.0D), 0.0F, 0.0F, 1.0F);
            case WEST -> transform.rotate((float) Math.toRadians(90.0D), 0.0F, 0.0F, 1.0F);
            default -> {
            }
        }
    }
}
