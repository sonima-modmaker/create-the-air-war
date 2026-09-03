package hi.block.entity;

import hi.entity.HeattrapFlareEntity;
import hi.init.CreateTheAirWarsModBlockEntities;
import hi.init.CreateTheAirWarsModEntities;
import hi.util.ProjectileLaunchHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class HeattrapBlockEntity extends BlockEntity {
    public static final int MAX_CHARGES = 18;
    private static final int FIRE_INTERVAL_TICKS = 10;

    private int chargeCount;
    private int cooldownTicks;

    public HeattrapBlockEntity(BlockPos pos, BlockState state) {
        super(CreateTheAirWarsModBlockEntities.HEATTRAP.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HeattrapBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (blockEntity.cooldownTicks > 0) {
            blockEntity.cooldownTicks--;
        }
        if (level.getBestNeighborSignal(pos) <= 0 || blockEntity.chargeCount <= 0 || blockEntity.cooldownTicks > 0) {
            return;
        }
        if (blockEntity.launch(serverLevel, pos, state)) {
            blockEntity.chargeCount--;
            blockEntity.cooldownTicks = FIRE_INTERVAL_TICKS;
            blockEntity.setChanged();
            blockEntity.syncToClient();
        }
    }

    public boolean addCharge() {
        if (chargeCount >= MAX_CHARGES) {
            return false;
        }
        chargeCount++;
        setChanged();
        syncToClient();
        return true;
    }

    public int getChargeCount() {
        return chargeCount;
    }

    private boolean launch(ServerLevel level, BlockPos pos, BlockState state) {
        Direction facing = state.hasProperty(hi.block.HeattrapBlock.FACING) ? state.getValue(hi.block.HeattrapBlock.FACING) : Direction.UP;
        Vec3 direction = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        if (direction.lengthSqr() < 1.0E-6) {
            direction = new Vec3(0.0, 1.0, 0.0);
        }

        ProjectileLaunchHelper.LaunchTransform launch = ProjectileLaunchHelper.resolveBlockLaunch(
            level,
            pos,
            pos.getX() + 0.5,
            pos.getY() + 0.5,
            pos.getZ() + 0.5,
            direction.x,
            direction.y,
            direction.z
        );

        HeattrapFlareEntity flare = new HeattrapFlareEntity(CreateTheAirWarsModEntities.HEATTRAP_FLARE.get(), level);
        flare.setPos(launch.position().x, launch.position().y, launch.position().z);
        Vec3 launchDirection = launch.direction().lengthSqr() > 1.0E-6 ? launch.direction().normalize() : direction.normalize();
        flare.setDeltaMovement(launch.inheritedVelocity().add(launchDirection.scale(0.68)));
        level.addFreshEntity(flare);
        return true;
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("ChargeCount", chargeCount);
        tag.putInt("CooldownTicks", cooldownTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        chargeCount = tag.getInt("ChargeCount");
        cooldownTicks = tag.getInt("CooldownTicks");
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket packet, net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            loadAdditional(tag, provider);
        }
    }
}
