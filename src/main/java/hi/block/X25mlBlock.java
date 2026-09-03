package hi.block;

import com.mojang.serialization.MapCodec;
import hi.block.entity.X25mlBlockEntity;
import hi.init.CreateTheAirWarsModBlockEntities;
import hi.init.CreateTheAirWarsModItems;
import hi.item.CameraLinkItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class X25mlBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<X25mlBlock> CODEC = simpleCodec(X25mlBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE_NORTH = Shapes.or(
        Block.box(5.0D, 10.0D, -16.0D, 11.0D, 16.0D, 32.0D),
        Block.box(4.0D, 9.0D, -6.0D, 12.0D, 22.0D, 25.0D),
        Block.box(5.0D, 10.0D, -14.0D, 11.0D, 16.0D, -8.0D)
    );

    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
        Block.box(5.0D, 10.0D, -16.0D, 11.0D, 16.0D, 32.0D),
        Block.box(4.0D, 9.0D, -9.0D, 12.0D, 22.0D, 22.0D),
        Block.box(5.0D, 10.0D, 24.0D, 11.0D, 16.0D, 30.0D)
    );

    private static final VoxelShape SHAPE_EAST = Shapes.or(
        Block.box(-16.0D, 10.0D, 5.0D, 32.0D, 16.0D, 11.0D),
        Block.box(-6.0D, 9.0D, 4.0D, 25.0D, 22.0D, 12.0D),
        Block.box(-14.0D, 10.0D, 5.0D, -8.0D, 16.0D, 11.0D)
    );

    private static final VoxelShape SHAPE_WEST = Shapes.or(
        Block.box(-16.0D, 10.0D, 5.0D, 32.0D, 16.0D, 11.0D),
        Block.box(-9.0D, 9.0D, 4.0D, 22.0D, 22.0D, 12.0D),
        Block.box(24.0D, 10.0D, 5.0D, 30.0D, 16.0D, 11.0D)
    );

    public X25mlBlock(Properties properties) {
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
        Direction facing = state.getValue(FACING);
        switch (facing) {
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
            case NORTH:
            default:
                return SHAPE_NORTH;
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(CreateTheAirWarsModItems.CAMERA_LINK.get())) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (!level.isClientSide) {
            CameraLinkItem.setSelectedVihr(stack, pos);
            player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.x25ml_selected", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GREEN), true);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new X25mlBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == CreateTheAirWarsModBlockEntities.X25ML.get() ? (lvl, pos, st, be) -> {
            if (be instanceof X25mlBlockEntity x25ml) {
                x25ml.tick(lvl, pos, st);
            }
        } : null;
    }
}
