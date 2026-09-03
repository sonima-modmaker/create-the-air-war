package hi.block.entity;

import hi.block.X25mlBlock;
import hi.entity.X25mlEntity;
import hi.init.CreateTheAirWarsModBlockEntities;
import hi.init.CreateTheAirWarsModEntities;
import hi.util.SableCoordinateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class X25mlBlockEntity extends BlockEntity implements MonitorLaunchableBlockEntity {
    public X25mlBlockEntity(BlockPos pos, BlockState state) {
        super(CreateTheAirWarsModBlockEntities.X25ML.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
    }

    @Override
    public boolean hasLaunchPayload() {
        return this.level != null && !this.level.getBlockState(this.worldPosition).isAir();
    }

    @Override
    public int getLaunchPayloadCount() {
        return this.hasLaunchPayload() ? 1 : 0;
    }

    @Override
    public int getMaxLaunchPayloadCount() {
        return 1;
    }

    @Override
    public boolean launch(CameraBlockEntity camera) {
        if (!(this.level instanceof ServerLevel serverLevel) || camera == null) {
            return false;
        }

        Direction facing = this.getBlockState().getValue(X25mlBlock.FACING);
        Vec3 forward = Vec3.atLowerCornerOf(facing.getNormal()).normalize();
        Vec3 localSpawn = Vec3.atCenterOf(this.worldPosition).add(forward.scale(1.15D)).add(0.0D, 0.05D, 0.0D);
        Vec3 projectedSpawn = SableCoordinateHelper.projectOut(serverLevel, localSpawn);
        Vec3 projectedDirection = SableCoordinateHelper.projectDirectionOut(serverLevel, localSpawn, forward);
        Vec3 spawn = projectedSpawn.lengthSqr() > 1.0E-8D ? projectedSpawn : localSpawn;
        Vec3 direction = projectedDirection.lengthSqr() > 1.0E-6D ? projectedDirection.normalize() : forward;

        X25mlEntity missile = new X25mlEntity(CreateTheAirWarsModEntities.X25ML_MISSILE.get(), serverLevel);
        missile.setPos(spawn);
        missile.setCameraPos(camera.getBlockPos());
        missile.setInitialDirection(direction);
        missile.setLaunchStartPos(spawn);
        missile.shoot(direction.x, direction.y, direction.z, (float) X25mlEntity.INITIAL_FORWARD_SPEED, 0.0F);
        missile.refreshOrientation();
        serverLevel.addFreshEntity(missile);

        serverLevel.setBlock(this.worldPosition, Blocks.AIR.defaultBlockState(), 3);
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
