package hi.block.entity;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.force.ForceTotal;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import hi.init.CreateTheAirWarsModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

public class GyroStabilizerBlockEntity extends KineticBlockEntity implements BlockEntitySubLevelActor, IHaveGoggleInformation {
    private static final double STRENGTH_MULTIPLIER = 6.0;
    private static final double ALIGNMENT_GAIN = 4.2;
    private static final double DAMPING_GAIN = 5.4;
    private static final double MAX_TILT_ANGLE = Math.toRadians(30.0);
    private static final double MAX_DELTA_OMEGA = 0.18;
    private static final double TILT_DEADBAND = Math.toRadians(1.2);
    private static final float MIN_STABILIZING_RPM = 4.0f;

    private int redstoneLevel = 0;

    public final LerpedFloat visualSpeed = LerpedFloat.linear();
    public float flywheelAngle;

    public GyroStabilizerBlockEntity(BlockPos pos, BlockState state) {
        super(CreateTheAirWarsModBlockEntities.GYRO_STABILIZER.get(), pos, state);
        this.visualSpeed.startWithValue(0.0);
    }

    public GyroStabilizerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.visualSpeed.startWithValue(0.0);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(1.0);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level == null || !this.level.isClientSide) {
            return;
        }

        float targetSpeed = isPoweredForStabilization() ? this.getSpeed() : 0.0f;
        this.visualSpeed.updateChaseTarget(targetSpeed);
        this.visualSpeed.tickChaser();
        this.flywheelAngle = (this.flywheelAngle + this.visualSpeed.getValue() * 1.2f) % 360.0f;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (this.level == null) return;
        if (!this.level.isClientSide) {
            updateRedstoneLevel();
        } else {
            float targetSpeed = isPoweredForStabilization() ? this.getSpeed() : 0.0f;
            this.visualSpeed.chase(targetSpeed, 1 / 64.0, Chaser.EXP);
        }
    }

    public void updateRedstoneLevel() {
        if (level != null && !level.isClientSide) {
            int newLevel = level.getBestNeighborSignal(worldPosition);
            if (this.redstoneLevel != newLevel) {
                this.redstoneLevel = newLevel;
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    public void sable$physicsTick(ServerSubLevel subLevel, RigidBodyHandle handle, double timeStep) {
        double rpm = Math.abs(getSpeed());
        if (!isStabilizing() || rpm < MIN_STABILIZING_RPM || !Double.isFinite(timeStep) || timeStep <= 0.0) {
            return;
        }

        Level level = this.getLevel();
        if (level == null) {
            return;
        }

        double rpmFactor = Math.min(1.0, rpm / 256.0);
        double redstoneFactor = this.redstoneLevel / 15.0;
        double strengthFactor = redstoneFactor * rpmFactor * rpmFactor * STRENGTH_MULTIPLIER;

        double stepScale = timeStep * 20.0;
        Quaterniond orientation = subLevel.logicalPose().orientation();

        Vector3d angularVelocityWorld = handle.getAngularVelocity(new Vector3d());
        if (!isFinite(angularVelocityWorld)) {
            return;
        }

        Vector3d gravity = Sable.HELPER.projectOutOfSubLevel(level,
            new Vector3d(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5));
        gravity = DimensionPhysicsData.getGravity(level, gravity, new Vector3d());
        Vector3d worldUp = getWorldUp(gravity);
        if (!isFinite(worldUp)) {
            return;
        }

        Vector3d angularVelocityLocal = orientation.transformInverse(angularVelocityWorld, new Vector3d());
        if (!isFinite(angularVelocityLocal)) {
            return;
        }
        angularVelocityLocal.y = 0.0;

        Vector3d targetUpLocal = orientation.transformInverse(worldUp, new Vector3d());
        if (!isFinite(targetUpLocal) || targetUpLocal.lengthSquared() < 1.0e-6) {
            return;
        }
        targetUpLocal.normalize();

        double safeUpY = Math.abs(targetUpLocal.y) < 1.0e-3 ? Math.copySign(1.0e-3, targetUpLocal.y == 0.0 ? 1.0 : targetUpLocal.y) : targetUpLocal.y;
        double pitchError = Math.atan2(targetUpLocal.z, safeUpY);
        double rollError = -Math.atan2(targetUpLocal.x, safeUpY);
        pitchError = Mth.clamp(pitchError, -MAX_TILT_ANGLE, MAX_TILT_ANGLE);
        rollError = Mth.clamp(rollError, -MAX_TILT_ANGLE, MAX_TILT_ANGLE);

        if (Math.abs(pitchError) < TILT_DEADBAND && Math.abs(rollError) < TILT_DEADBAND
            && angularVelocityLocal.x * angularVelocityLocal.x + angularVelocityLocal.z * angularVelocityLocal.z < 0.0035) {
            return;
        }

        double maxDelta = MAX_DELTA_OMEGA * Mth.lerp(strengthFactor, 0.2, 1.0) * stepScale;
        double desiredDeltaOmegaX = Mth.clamp(
            (pitchError * ALIGNMENT_GAIN - angularVelocityLocal.x * DAMPING_GAIN) * strengthFactor * stepScale,
            -maxDelta,
            maxDelta
        );
        double desiredDeltaOmegaZ = Mth.clamp(
            (rollError * ALIGNMENT_GAIN - angularVelocityLocal.z * DAMPING_GAIN) * strengthFactor * stepScale,
            -maxDelta,
            maxDelta
        );

        if (!Double.isFinite(desiredDeltaOmegaX) || !Double.isFinite(desiredDeltaOmegaZ)) {
            return;
        }

        Vector3d unitX = new Vector3d(1.0, 0.0, 0.0);
        Vector3d unitZ = new Vector3d(0.0, 0.0, 1.0);
        Vector3d temp = new Vector3d();
        double inertiaX = Math.max(1.0, subLevel.getMassTracker().getInertiaTensor().transform(unitX, temp).dot(unitX));
        double inertiaZ = Math.max(1.0, subLevel.getMassTracker().getInertiaTensor().transform(unitZ, temp).dot(unitZ));
        if (!Double.isFinite(inertiaX) || !Double.isFinite(inertiaZ)) {
            return;
        }

        Vector3d torqueImpulse = new Vector3d(desiredDeltaOmegaX * inertiaX, 0.0, desiredDeltaOmegaZ * inertiaZ);
        if (!isFinite(torqueImpulse) || torqueImpulse.lengthSquared() < 1.0e-8) {
            return;
        }

        QueuedForceGroup forceGroup = subLevel.getOrCreateQueuedForceGroup(hi.init.CreateTheAirWarsModForceGroups.GYRO_TORQUE.get());
        forceGroup.getForceTotal().applyLinearAndAngularImpulse(new Vector3d(), torqueImpulse);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("create_the_air_wars.gui.goggles.gyroscope").withStyle(ChatFormatting.WHITE)
            .append(Component.literal(":").withStyle(ChatFormatting.DARK_GRAY)));

        float rpm = Math.abs(getSpeed());
        double rpmFactor = Math.min(1.0, rpm / 256.0);
        double redstoneFactor = this.redstoneLevel / 15.0;
        int strengthPct = (int) Math.round(redstoneFactor * rpmFactor * 100.0);
        boolean active = isStabilizing();

        tooltip.add(Component.translatable("create_the_air_wars.gui.goggles.gyroscope.status").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(" "))
            .append(Component.translatable(active
                    ? "create_the_air_wars.gui.goggles.gyroscope.status.stabilizing"
                    : "create_the_air_wars.gui.goggles.gyroscope.status.off")
                .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED)));

        tooltip.add(Component.translatable("create_the_air_wars.gui.goggles.gyroscope.rpm").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(" "))
            .append(Component.literal(String.format("%.0f RPM", rpm)).withStyle(ChatFormatting.AQUA)));

        tooltip.add(Component.translatable("create_the_air_wars.gui.goggles.gyroscope.strength").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(" "))
            .append(Component.literal(strengthPct + " %").withStyle(ChatFormatting.AQUA)));

        tooltip.add(Component.translatable("create_the_air_wars.gui.goggles.gyroscope.mode").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(" "))
            .append(Component.translatable("create_the_air_wars.gui.goggles.gyroscope.mode.pitch_roll").withStyle(ChatFormatting.AQUA)));

        return true;
    }

    public boolean isPoweredForStabilization() {
        return this.redstoneLevel > 0;
    }

    public boolean isStabilizing() {
        return isPoweredForStabilization() && Math.abs(this.getSpeed()) >= MIN_STABILIZING_RPM;
    }

    public float getFlywheelAngle(float partialTicks) {
        return this.flywheelAngle + this.visualSpeed.getValue(partialTicks) * 1.2f * partialTicks;
    }

    private static boolean isFinite(Vector3d vec) {
        return Double.isFinite(vec.x) && Double.isFinite(vec.y) && Double.isFinite(vec.z);
    }

    private static Vector3d getWorldUp(Vector3d gravity) {
        if (!isFinite(gravity) || gravity.lengthSquared() < 1.0e-6) {
            return new Vector3d(0.0, 1.0, 0.0);
        }
        return gravity.normalize().negate();
    }

    @Override
    protected void write(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider, boolean clientPacket) {
        super.write(tag, provider, clientPacket);
        tag.putInt("RedstoneLevel", redstoneLevel);
    }

    @Override
    protected void read(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider, boolean clientPacket) {
        super.read(tag, provider, clientPacket);
        redstoneLevel = tag.getInt("RedstoneLevel");
    }
}
