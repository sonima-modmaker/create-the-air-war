package hi.block.entity;

import hi.init.CreateTheAirWarsModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class MonitorBlockEntity extends BlockEntity {
    private BlockPos linkedCameraPos;
    private final List<BlockPos> linkedVihrPositions = new ArrayList<>();
    private int nextVihrIndex;

    public MonitorBlockEntity(BlockPos pos, BlockState state) {
        super(CreateTheAirWarsModBlockEntities.MONITOR.get(), pos, state);
    }

    public BlockPos getLinkedCameraPos() {
        return this.linkedCameraPos;
    }

    public boolean hasLinkedCamera() {
        return this.linkedCameraPos != null;
    }

    public void setLinkedCameraPos(BlockPos linkedCameraPos) {
        this.linkedCameraPos = linkedCameraPos;
        this.setChangedAndSync();
    }

    public void clearLinkedCamera() {
        this.linkedCameraPos = null;
        this.setChangedAndSync();
    }

    public List<BlockPos> getLinkedVihrPositions() {
        return List.copyOf(this.linkedVihrPositions);
    }

    public boolean hasLinkedVihr() {
        return !this.linkedVihrPositions.isEmpty();
    }

    public void addLinkedVihrPos(BlockPos linkedVihrPos) {
        if (linkedVihrPos == null) {
            return;
        }
        if (!this.linkedVihrPositions.contains(linkedVihrPos)) {
            this.linkedVihrPositions.add(linkedVihrPos.immutable());
        }
        this.setChangedAndSync();
    }

    public void clearLinkedVihr() {
        this.linkedVihrPositions.clear();
        this.nextVihrIndex = 0;
        this.setChangedAndSync();
    }

    public BlockPos getCurrentLinkedVihrPos() {
        if (this.linkedVihrPositions.isEmpty()) {
            return null;
        }
        this.nextVihrIndex = Math.floorMod(this.nextVihrIndex, this.linkedVihrPositions.size());
        return this.linkedVihrPositions.get(this.nextVihrIndex);
    }

    public BlockPos selectNextLinkedVihrPos() {
        if (this.linkedVihrPositions.isEmpty()) {
            return null;
        }
        this.nextVihrIndex = Math.floorMod(this.nextVihrIndex, this.linkedVihrPositions.size());
        BlockPos selected = this.linkedVihrPositions.get(this.nextVihrIndex);
        this.nextVihrIndex = (this.nextVihrIndex + 1) % this.linkedVihrPositions.size();
        this.setChangedAndSync();
        return selected;
    }

    public int getTotalVihrRocketCount() {
        if (this.level == null) {
            return 0;
        }
        int total = 0;
        for (BlockPos pos : this.linkedVihrPositions) {
            if (this.level.getBlockEntity(pos) instanceof MonitorLaunchableBlockEntity launcher) {
                total += launcher.getLaunchPayloadCount();
            }
        }
        return total;
    }

    public int getTotalLinkedLauncherCapacity() {
        if (this.level == null) {
            return 0;
        }
        int total = 0;
        for (BlockPos pos : this.linkedVihrPositions) {
            if (this.level.getBlockEntity(pos) instanceof MonitorLaunchableBlockEntity launcher) {
                total += launcher.getMaxLaunchPayloadCount();
            }
        }
        return total;
    }

    public void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) {
            return;
        }
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
        if (this.linkedCameraPos != null) {
            tag.putInt("LinkedCameraX", this.linkedCameraPos.getX());
            tag.putInt("LinkedCameraY", this.linkedCameraPos.getY());
            tag.putInt("LinkedCameraZ", this.linkedCameraPos.getZ());
        }
        if (!this.linkedVihrPositions.isEmpty()) {
            ListTag vihrs = new ListTag();
            for (BlockPos pos : this.linkedVihrPositions) {
                CompoundTag vihrTag = new CompoundTag();
                vihrTag.putInt("X", pos.getX());
                vihrTag.putInt("Y", pos.getY());
                vihrTag.putInt("Z", pos.getZ());
                vihrs.add(vihrTag);
            }
            tag.put("LinkedVihrs", vihrs);
        }
        tag.putInt("NextVihrIndex", this.nextVihrIndex);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("LinkedCameraX")) {
            this.linkedCameraPos = new BlockPos(tag.getInt("LinkedCameraX"), tag.getInt("LinkedCameraY"), tag.getInt("LinkedCameraZ"));
        } else {
            this.linkedCameraPos = null;
        }
        this.linkedVihrPositions.clear();
        if (tag.contains("LinkedVihrs", Tag.TAG_LIST)) {
            ListTag vihrs = tag.getList("LinkedVihrs", Tag.TAG_COMPOUND);
            for (int i = 0; i < vihrs.size(); i++) {
                CompoundTag vihrTag = vihrs.getCompound(i);
                this.linkedVihrPositions.add(new BlockPos(vihrTag.getInt("X"), vihrTag.getInt("Y"), vihrTag.getInt("Z")));
            }
        }
        this.nextVihrIndex = tag.getInt("NextVihrIndex");
        if (!this.linkedVihrPositions.isEmpty()) {
            this.nextVihrIndex = Math.floorMod(this.nextVihrIndex, this.linkedVihrPositions.size());
        } else {
            this.nextVihrIndex = 0;
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
}
