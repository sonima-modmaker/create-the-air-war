package hi.block.entity;

import hi.block.RocketEngineBlock;
// import hi.registry.ModBlockEntities;
import hi.init.CreateTheAirWarsModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.ryanhcode.sable.api.block.propeller.BlockEntityPropeller;
import dev.ryanhcode.sable.api.block.propeller.BlockEntitySubLevelPropellerActor;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.network.chat.Component;
import java.util.List;

public class RocketEngineBlockEntity extends BlockEntity implements BlockEntityPropeller, BlockEntitySubLevelPropellerActor, IHaveGoggleInformation {
    private static final float BASE_THRUST = 150f;
    private static final float MAX_APPLIED_THRUST = 150f;
    private static final int STARTUP_TICKS = 200;
    private static final int FUEL_CAPACITY = 200;
    private static final int FUEL_CONSUMPTION = 1;
    private static final int CLIENT_SOUND_UPDATE_INTERVAL = 4;
    private static final int CLIENT_SYNC_INTERVAL = 5;

    private float thrustPower = 1.0f;
    private boolean registered = false;

    public enum EngineState { OFF, STARTING, RUNNING, STOPPING }
    private EngineState engineState = EngineState.OFF;
    private int stateTimer = 0;
    private boolean wasThrottling = false;
    private boolean fuelStarved = false;

    private final FluidTank fuelTank = new FluidTank(FUEL_CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid() == Fluids.LAVA;
        }
    };

    public RocketEngineBlockEntity(BlockPos pos, BlockState state) {
        super(CreateTheAirWarsModBlockEntities.ROCKET_ENGINE.get(), pos, state);
    }

    public void toggleEngine() {
        if (engineState == EngineState.OFF && fuelTank.getFluidAmount() > 0) {
            engineState = EngineState.STARTING;
            stateTimer = 0;
        } else if (engineState == EngineState.RUNNING) {
            engineState = EngineState.STOPPING;
            stateTimer = STARTUP_TICKS;
            wasThrottling = false;
        }
        setChanged();
        syncToClient();
    }

    private void register() {
        if (level == null || level.isClientSide || registered) return;
        registered = true;
    }

    public Direction getFuelInputSide() {
        return getBlockState().getValue(RocketEngineBlock.FACING).getOpposite();
    }

    public IFluidHandler getFluidHandler() {
        return fuelTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RocketEngineBlockEntity be) {
        if (level.isClientSide) return;
        if (!be.registered) {
            be.registered = true;
        }

        switch (be.engineState) {
            case STARTING -> {
                be.stateTimer++;
                if (be.stateTimer >= STARTUP_TICKS) {
                    be.engineState = EngineState.RUNNING;
                    be.stateTimer = STARTUP_TICKS;
                }
                if ((be.stateTimer % CLIENT_SYNC_INTERVAL) == 0 || be.stateTimer >= STARTUP_TICKS) {
                    be.syncToClient();
                }
            }
            case STOPPING -> {
                be.stateTimer--;
                if (be.stateTimer <= 0) {
                    be.engineState = EngineState.OFF;
                    be.stateTimer = 0;
                }
                if ((be.stateTimer % CLIENT_SYNC_INTERVAL) == 0 || be.stateTimer <= 0) {
                    be.syncToClient();
                }
            }
            default -> {}
        }

        boolean powered = state.getValue(RocketEngineBlock.POWERED);
        
        boolean hasFuel = be.fuelTank.getFluidAmount() >= FUEL_CONSUMPTION;
        boolean canThrust = be.engineState == EngineState.RUNNING && powered && hasFuel;

        if (canThrust) {
            be.fuelTank.drain(FUEL_CONSUMPTION, IFluidHandler.FluidAction.EXECUTE);
            be.setChanged();
        }

        if (canThrust != be.wasThrottling) {
            be.wasThrottling = canThrust;
            be.syncToClient();
        }

        boolean shouldBeActive = be.engineState == EngineState.RUNNING && powered && hasFuel;
        if (state.getValue(RocketEngineBlock.ACTIVE) != shouldBeActive) {
            level.setBlock(pos, state.setValue(RocketEngineBlock.ACTIVE, shouldBeActive), 3);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, RocketEngineBlockEntity be) {
        if ((level.getGameTime() & (CLIENT_SOUND_UPDATE_INTERVAL - 1)) != (pos.asLong() & (CLIENT_SOUND_UPDATE_INTERVAL - 1))) {
            return;
        }
        hi.client.sound.RocketEngineSoundManager.updateEngineSound(be);
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public boolean isActive() {
        return isThrottling();
    }

    @Override
    public double getThrust() {
        return isActive() ? BASE_THRUST * thrustPower : 0;
    }

    public double getAppliedThrust() {
        double s = getScaledThrust();
        if (s > MAX_APPLIED_THRUST) return MAX_APPLIED_THRUST;
        if (s < -MAX_APPLIED_THRUST) return -MAX_APPLIED_THRUST;
        return s;
    }

    @Override
    public double getScaledThrust() {
        double base = -getThrust();
        if (base > MAX_APPLIED_THRUST) return MAX_APPLIED_THRUST;
        if (base < -MAX_APPLIED_THRUST) return -MAX_APPLIED_THRUST;
        return base;
    }

    @Override
    public double getAirflowScaling() {
        return 1.0;
    }

    @Override
    public double getCurrentAirPressure() {
        return 1.0;
    }

    @Override
    public double getAirflow() {
        return getThrust() * 0.0000045; // AIRFLOW_FROM_THRUST from Forge version
    }

    @Override
    public Direction getBlockDirection() {
        return getBlockState().getValue(RocketEngineBlock.FACING);
    }

    @Override
    public BlockEntityPropeller getPropeller() {
        return this;
    }

    @Override
    public void sable$physicsTick(dev.ryanhcode.sable.sublevel.ServerSubLevel subLevel, dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle handle, double timeStep) {
        if (this.isActive()) {
            Direction facing = getBlockDirection();

            // Sable accumulates point forces between game-tick resets. To make the visible total match
            // MAX_APPLIED_THRUST regardless of physics tick rate, scale per-call by (timeStep * gameTPS).
            double scaledThrust = this.getAppliedThrust() * timeStep * 20.0;
            org.joml.Vector3d dir = dev.ryanhcode.sable.companion.math.JOMLConversion.atLowerCornerOf(facing.getNormal());
            dir.mul(scaledThrust);

            org.joml.Vector3d pos = dev.ryanhcode.sable.companion.math.JOMLConversion.atCenterOf(this.getBlockPos());

            dev.ryanhcode.sable.api.physics.force.QueuedForceGroup group = subLevel.getOrCreateQueuedForceGroup(hi.init.CreateTheAirWarsModForceGroups.ROCKET_THRUST.get());
            group.applyAndRecordPointForce(pos, dir);
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("block.create_the_air_wars.rocket_engine")
            .withStyle(net.minecraft.ChatFormatting.WHITE)
            .append(Component.literal(":")));

        String stateLabel = switch (engineState) {
            case OFF -> "OFF";
            case STARTING -> "Starting...";
            case RUNNING -> "Running";
            case STOPPING -> "Stopping...";
        };
        net.minecraft.ChatFormatting stateColor = engineState == EngineState.RUNNING
            ? net.minecraft.ChatFormatting.GREEN
            : (engineState == EngineState.OFF ? net.minecraft.ChatFormatting.RED : net.minecraft.ChatFormatting.GOLD);
        tooltip.add(Component.literal(" Status: ").withStyle(net.minecraft.ChatFormatting.GRAY)
            .append(Component.literal(stateLabel).withStyle(stateColor)));

        double appliedThrust = isActive() ? Math.abs(this.getAppliedThrust()) : 0.0;
        double maxThrust = MAX_APPLIED_THRUST;
        tooltip.add(Component.literal(" Thrust: ").withStyle(net.minecraft.ChatFormatting.GRAY)
            .append(Component.literal(String.format("%.0f", appliedThrust)).withStyle(net.minecraft.ChatFormatting.AQUA))
            .append(Component.literal(" / ").withStyle(net.minecraft.ChatFormatting.DARK_GRAY))
            .append(Component.literal(String.format("%.0f", maxThrust)).withStyle(net.minecraft.ChatFormatting.AQUA))
            .append(Component.literal(" N").withStyle(net.minecraft.ChatFormatting.AQUA)));

        tooltip.add(Component.literal(" Power: ").withStyle(net.minecraft.ChatFormatting.GRAY)
            .append(Component.literal(String.format("%.0f", thrustPower * 100f)).withStyle(net.minecraft.ChatFormatting.AQUA))
            .append(Component.literal(" %").withStyle(net.minecraft.ChatFormatting.AQUA)));

        tooltip.add(Component.literal(" Fuel: ").withStyle(net.minecraft.ChatFormatting.GRAY)
            .append(Component.literal(getFuelAmount() + " / " + getFuelCapacity() + " mB").withStyle(net.minecraft.ChatFormatting.AQUA)));

        return true;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide) {
            hi.client.sound.RocketEngineSoundManager.removeEngine(worldPosition);
        }
    }

    public EngineState getEngineState() { return engineState; }
    public int getStateTimer() { return stateTimer; }
    public float getVolumeProgress() { return (float) stateTimer / STARTUP_TICKS; }
    public float getThermalHighlightStrength(float partialTicks) {
        return switch (engineState) {
            case OFF -> 0.0f;
            case STARTING -> Math.max(0.0f, Math.min(1.0f, (stateTimer + partialTicks) / (float) STARTUP_TICKS));
            case RUNNING -> isThrottling() ? 1.0f : 0.75f;
            case STOPPING -> Math.max(0.0f, Math.min(1.0f, (stateTimer - partialTicks) / (float) STARTUP_TICKS));
        };
    }
    public boolean isThrottling() { return engineState == EngineState.RUNNING && getBlockState().getValue(RocketEngineBlock.POWERED) && fuelTank.getFluidAmount() >= FUEL_CONSUMPTION; }
    public float getThrustPower() { return thrustPower; }
    public void setThrustPower(float power) {
        float clamped = Math.max(0f, Math.min(1f, power));
        if (this.thrustPower == clamped) return;
        this.thrustPower = clamped;
        setChanged();
        syncToClient();
    }
    public int getFuelAmount() { return fuelTank.getFluidAmount(); }
    public int getFuelCapacity() { return FUEL_CAPACITY; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putFloat("thrustPower", thrustPower);
        tag.putInt("engineState", engineState.ordinal());
        tag.putInt("stateTimer", stateTimer);
        tag.put("fuel", fuelTank.writeToNBT(provider, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("thrustPower")) thrustPower = Math.max(0f, Math.min(1f, tag.getFloat("thrustPower")));
        if (tag.contains("engineState")) engineState = EngineState.values()[tag.getInt("engineState")];
        if (tag.contains("stateTimer")) stateTimer = tag.getInt("stateTimer");
        if (tag.contains("fuel")) fuelTank.readFromNBT(provider, tag.getCompound("fuel"));
        registered = false;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.putFloat("thrustPower", thrustPower);
        tag.putInt("engineState", engineState.ordinal());
        tag.putInt("stateTimer", stateTimer);
        tag.put("fuel", fuelTank.writeToNBT(provider, new CompoundTag()));
        return tag;
    }

    public void readUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        if (tag.contains("thrustPower")) thrustPower = Math.max(0f, Math.min(1f, tag.getFloat("thrustPower")));
        if (tag.contains("engineState")) engineState = EngineState.values()[tag.getInt("engineState")];
        if (tag.contains("stateTimer")) stateTimer = tag.getInt("stateTimer");
        if (tag.contains("fuel")) fuelTank.readFromNBT(provider, tag.getCompound("fuel"));
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) readUpdateTag(tag, provider);
    }
}
