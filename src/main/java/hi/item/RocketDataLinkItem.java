package hi.item;

import hi.block.entity.RocketDataLinkBlockEntity;
import hi.init.CreateTheAirWarsModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class RocketDataLinkItem extends BlockItem {
    public RocketDataLinkItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        // Shift+click to clear selection
        if (player.isShiftKeyDown() && stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
            if (!level.isClientSide) {
                clearSelectedEngines(stack);
                player.displayClientMessage(
                    Component.translatable("create_the_air_wars.rocket_data_link.cleared")
                        .withStyle(ChatFormatting.YELLOW), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Check if clicking on a Rocket Engine
        if (level.getBlockState(pos).is(CreateTheAirWarsModBlocks.ROCKET_ENGINE.get())) {
            if (level.isClientSide) return InteractionResult.SUCCESS;

            List<BlockPos> selected = getSelectedEngines(stack);
            
            if (selected.contains(pos)) {
                // Deselect
                selected.remove(pos);
                setSelectedEngines(stack, selected);
                player.displayClientMessage(
                    Component.translatable("create_the_air_wars.rocket_data_link.deselected")
                        .withStyle(ChatFormatting.YELLOW), true);
            } else {
                selected.add(pos);
                setSelectedEngines(stack, selected);
                player.displayClientMessage(
                    Component.translatable("create_the_air_wars.rocket_data_link.selected", selected.size())
                        .withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResult.CONSUME;
        }

        // Place block normally - engines will be transferred in placeBlock
        return super.useOn(context);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean placed = super.placeBlock(context, state);
        
        if (placed && !context.getLevel().isClientSide) {
            BlockPos placedPos = context.getClickedPos();
            if (!state.canSurvive(context.getLevel(), placedPos)) {
                placedPos = placedPos.relative(context.getClickedFace());
            }
            
            BlockEntity be = context.getLevel().getBlockEntity(placedPos);
            if (be instanceof RocketDataLinkBlockEntity dataLink) {
                List<BlockPos> engines = getSelectedEngines(context.getItemInHand());
                for (BlockPos enginePos : engines) {
                    dataLink.addEngine(enginePos);
                }
            }
            
            // Clear selection after placing
            clearSelectedEngines(context.getItemInHand());
        }
        
        return placed;
    }

    public static List<BlockPos> getSelectedEngines(ItemStack stack) {
        List<BlockPos> engines = new ArrayList<>();
        net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("SelectedEngines", Tag.TAG_LIST)) {
                ListTag list = tag.getList("SelectedEngines", Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag posTag = list.getCompound(i);
                    engines.add(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
                }
            }
        }
        return engines;
    }

    public static void setSelectedEngines(ItemStack stack, List<BlockPos> engines) {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag -> {
            ListTag list = new ListTag();
            for (BlockPos pos : engines) {
                CompoundTag posTag = new CompoundTag();
                posTag.putInt("X", pos.getX());
                posTag.putInt("Y", pos.getY());
                posTag.putInt("Z", pos.getZ());
                list.add(posTag);
            }
            tag.put("SelectedEngines", list);
        });
    }

    public static void clearSelectedEngines(ItemStack stack) {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove("SelectedEngines");
        });
    }
}
