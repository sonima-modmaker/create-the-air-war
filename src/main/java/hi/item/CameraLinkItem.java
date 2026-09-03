package hi.item;

import hi.block.entity.CameraBlockEntity;
import hi.block.entity.MonitorBlockEntity;
import hi.block.entity.VihrLauncherBlockEntity;
import hi.block.entity.X25mlBlockEntity;
import hi.init.CreateTheAirWarsModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CameraLinkItem extends Item {
    public CameraLinkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                clearSelectedCamera(stack);
                clearSelectedVihr(stack);
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof MonitorBlockEntity monitor) {
                    monitor.clearLinkedCamera();
                    monitor.clearLinkedVihr();
                }
                player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.cleared").withStyle(ChatFormatting.YELLOW), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.getBlockState(pos).is(CreateTheAirWarsModBlocks.CAMERA.get())) {
            if (!level.isClientSide) {
                clearSelectedVihr(stack);
                setSelectedCamera(stack, pos);
                player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.camera_selected", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.getBlockState(pos).is(CreateTheAirWarsModBlocks.VIHR.get())) {
            if (!level.isClientSide) {
                clearSelectedCamera(stack);
                setSelectedVihr(stack, pos);
                player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.vihr_selected", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.getBlockState(pos).is(CreateTheAirWarsModBlocks.X25ML.get())) {
            if (!level.isClientSide) {
                clearSelectedCamera(stack);
                setSelectedVihr(stack, pos);
                player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.x25ml_selected", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.getBlockState(pos).is(CreateTheAirWarsModBlocks.MONITOR.get())) {
            if (!level.isClientSide) {
                BlockPos selectedCamera = getSelectedCamera(stack);
                BlockPos selectedVihr = getSelectedVihr(stack);
                if (selectedCamera == null) {
                    if (selectedVihr == null) {
                        player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.no_camera").withStyle(ChatFormatting.RED), true);
                        return InteractionResult.CONSUME;
                    }
                }
                BlockEntity monitorBe = level.getBlockEntity(pos);
                if (!(monitorBe instanceof MonitorBlockEntity monitor)) {
                    player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.invalid").withStyle(ChatFormatting.RED), true);
                    return InteractionResult.CONSUME;
                }
                if (selectedCamera != null) {
                    BlockEntity cameraBe = level.getBlockEntity(selectedCamera);
                    if (!(cameraBe instanceof CameraBlockEntity)) {
                        player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.invalid").withStyle(ChatFormatting.RED), true);
                        return InteractionResult.CONSUME;
                    }
                    monitor.setLinkedCameraPos(selectedCamera);
                    clearSelectedCamera(stack);
                    player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.monitor_linked", selectedCamera.getX(), selectedCamera.getY(), selectedCamera.getZ()).withStyle(ChatFormatting.AQUA), true);
                }
                if (selectedVihr != null) {
                    BlockEntity vihrBe = level.getBlockEntity(selectedVihr);
                    if (!(vihrBe instanceof VihrLauncherBlockEntity) && !(vihrBe instanceof X25mlBlockEntity)) {
                        player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.invalid").withStyle(ChatFormatting.RED), true);
                        return InteractionResult.CONSUME;
                    }
                    monitor.addLinkedVihrPos(selectedVihr);
                    clearSelectedVihr(stack);
                    if (vihrBe instanceof X25mlBlockEntity) {
                        player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.x25ml_linked", selectedVihr.getX(), selectedVihr.getY(), selectedVihr.getZ()).withStyle(ChatFormatting.AQUA), true);
                    } else {
                        player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.vihr_linked", selectedVihr.getX(), selectedVihr.getY(), selectedVihr.getZ()).withStyle(ChatFormatting.AQUA), true);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    public static BlockPos getSelectedCamera(ItemStack stack) {
        net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }
        var tag = customData.copyTag();
        if (!tag.contains("SelectedCameraX")) {
            return null;
        }
        return new BlockPos(tag.getInt("SelectedCameraX"), tag.getInt("SelectedCameraY"), tag.getInt("SelectedCameraZ"));
    }

    public static void setSelectedCamera(ItemStack stack, BlockPos pos) {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt("SelectedCameraX", pos.getX());
            tag.putInt("SelectedCameraY", pos.getY());
            tag.putInt("SelectedCameraZ", pos.getZ());
        });
    }

    public static void clearSelectedCamera(ItemStack stack) {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove("SelectedCameraX");
            tag.remove("SelectedCameraY");
            tag.remove("SelectedCameraZ");
        });
    }

    public static BlockPos getSelectedVihr(ItemStack stack) {
        net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }
        var tag = customData.copyTag();
        if (!tag.contains("SelectedVihrX")) {
            return null;
        }
        return new BlockPos(tag.getInt("SelectedVihrX"), tag.getInt("SelectedVihrY"), tag.getInt("SelectedVihrZ"));
    }

    public static void setSelectedVihr(ItemStack stack, BlockPos pos) {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt("SelectedVihrX", pos.getX());
            tag.putInt("SelectedVihrY", pos.getY());
            tag.putInt("SelectedVihrZ", pos.getZ());
        });
    }

    public static void clearSelectedVihr(ItemStack stack) {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove("SelectedVihrX");
            tag.remove("SelectedVihrY");
            tag.remove("SelectedVihrZ");
        });
    }
}
