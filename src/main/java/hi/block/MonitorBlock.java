package hi.block;

import com.mojang.serialization.MapCodec;
import hi.block.entity.CameraBlockEntity;
import hi.block.entity.X25mlBlockEntity;
import hi.block.entity.MonitorBlockEntity;
import hi.init.CreateTheAirWarsModBlockEntities;
import hi.init.CreateTheAirWarsModItems;
import hi.item.CameraLinkItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MonitorBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<MonitorBlock> CODEC = simpleCodec(MonitorBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public MonitorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(CreateTheAirWarsModItems.CAMERA_LINK.get())) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (!level.isClientSide) {
            BlockPos selectedCamera = CameraLinkItem.getSelectedCamera(stack);
            BlockPos selectedVihr = CameraLinkItem.getSelectedVihr(stack);
            BlockEntity monitorBe = level.getBlockEntity(pos);
            if (selectedCamera == null && selectedVihr == null) {
                player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.no_camera").withStyle(ChatFormatting.RED), true);
                return ItemInteractionResult.SUCCESS;
            }
            if (!(monitorBe instanceof MonitorBlockEntity monitor)) {
                player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.invalid").withStyle(ChatFormatting.RED), true);
                return ItemInteractionResult.SUCCESS;
            }
            if (selectedCamera != null) {
                if (!(level.getBlockEntity(selectedCamera) instanceof CameraBlockEntity)) {
                    player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.invalid").withStyle(ChatFormatting.RED), true);
                    return ItemInteractionResult.SUCCESS;
                }
                monitor.setLinkedCameraPos(selectedCamera);
                CameraLinkItem.clearSelectedCamera(stack);
                player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.monitor_linked", selectedCamera.getX(), selectedCamera.getY(), selectedCamera.getZ()).withStyle(ChatFormatting.AQUA), true);
            }
            if (selectedVihr != null) {
                BlockEntity launcherBe = level.getBlockEntity(selectedVihr);
                if (!(launcherBe instanceof hi.block.entity.VihrLauncherBlockEntity) && !(launcherBe instanceof X25mlBlockEntity)) {
                    player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.invalid").withStyle(ChatFormatting.RED), true);
                    return ItemInteractionResult.SUCCESS;
                }
                monitor.addLinkedVihrPos(selectedVihr);
                CameraLinkItem.clearSelectedVihr(stack);
                if (launcherBe instanceof X25mlBlockEntity) {
                    player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.x25ml_linked", selectedVihr.getX(), selectedVihr.getY(), selectedVihr.getZ()).withStyle(ChatFormatting.AQUA), true);
                } else {
                    player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.vihr_linked", selectedVihr.getX(), selectedVihr.getY(), selectedVihr.getZ()).withStyle(ChatFormatting.AQUA), true);
                }
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            toggleClientAdjustmentMode(level, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void toggleClientAdjustmentMode(Level level, BlockPos pos) {
        try {
            Class<?> handler = Class.forName("hi.client.camera.CameraMonitorClientHandler");
            handler.getMethod("toggleAdjustmentMode", Level.class, BlockPos.class).invoke(null, level, pos);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MonitorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == CreateTheAirWarsModBlockEntities.MONITOR.get() ? (lvl, pos, st, be) -> {
            if (be instanceof MonitorBlockEntity monitor) {
                monitor.tick(lvl, pos, st);
            }
        } : null;
    }
}
