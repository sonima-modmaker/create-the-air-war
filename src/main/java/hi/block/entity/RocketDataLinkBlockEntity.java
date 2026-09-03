package hi.block.entity;

import hi.block.RocketEngineBlock;
// import hi.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class RocketDataLinkBlockEntity extends BlockEntity {
    private final List<BlockPos> linkedEngines = new ArrayList<>();

    public RocketDataLinkBlockEntity(BlockPos pos, BlockState state) {
        super(hi.init.CreateTheAirWarsModBlockEntities.ROCKET_DATA_LINK.get(), pos, state);
    }

    public List<BlockPos> getLinkedEngines() {
        return linkedEngines;
    }

    public void addEngine(BlockPos enginePos) {
        if (!linkedEngines.contains(enginePos)) {
            linkedEngines.add(enginePos);
            setChanged();
            syncToClient();
        }
    }

    public void removeEngine(BlockPos enginePos) {
        if (linkedEngines.remove(enginePos)) {
            setChanged();
            syncToClient();
        }
    }

    public void clearEngines() {
        linkedEngines.clear();
        setChanged();
        syncToClient();
    }

    public void startAllEngines() {
        if (level == null || level.isClientSide) return;
        for (BlockPos pos : linkedEngines) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RocketEngineBlockEntity engine) {
                if (engine.getEngineState() == RocketEngineBlockEntity.EngineState.OFF) {
                    engine.toggleEngine();
                }
            }
        }
    }

    public void stopAllEngines() {
        if (level == null || level.isClientSide) return;
        for (BlockPos pos : linkedEngines) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RocketEngineBlockEntity engine) {
                if (engine.getEngineState() == RocketEngineBlockEntity.EngineState.RUNNING) {
                    engine.toggleEngine();
                }
            }
        }
    }

    public void updateAllEnginesPower(int power) {
        if (level == null || level.isClientSide) return;
        float thrustPower = power / 15.0f;
        boolean shouldBePowered = power > 0;
        
        for (BlockPos pos : linkedEngines) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RocketEngineBlockEntity engine) {
                // Устанавливаем мощность тяги
                engine.setThrustPower(thrustPower);
                
                // Устанавливаем POWERED состояние блока двигателя
                BlockState engineState = level.getBlockState(pos);
                if (engineState.hasProperty(RocketEngineBlock.POWERED)) {
                    boolean currentlyPowered = engineState.getValue(RocketEngineBlock.POWERED);
                    if (currentlyPowered != shouldBePowered) {
                        level.setBlock(pos, engineState.setValue(RocketEngineBlock.POWERED, shouldBePowered), 3);
                    }
                }
            }
        }
    }

    public int getCurrentPower() {
        if (level == null) return 0;
        return level.getBestNeighborSignal(worldPosition);
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ListTag enginesTag = new ListTag();
        for (BlockPos enginePos : linkedEngines) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", enginePos.getX());
            posTag.putInt("Y", enginePos.getY());
            posTag.putInt("Z", enginePos.getZ());
            enginesTag.add(posTag);
        }
        tag.put("LinkedEngines", enginesTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        linkedEngines.clear();
        if (tag.contains("LinkedEngines", Tag.TAG_LIST)) {
            ListTag enginesTag = tag.getList("LinkedEngines", Tag.TAG_COMPOUND);
            for (int i = 0; i < enginesTag.size(); i++) {
                CompoundTag posTag = enginesTag.getCompound(i);
                linkedEngines.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
            }
        }
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
}
