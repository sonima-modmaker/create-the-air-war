package hi.block;

import hi.block.entity.HeattrapBlockEntity;
import hi.init.CreateTheAirWarsModBlockEntities;
import hi.init.CreateTheAirWarsModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class HeattrapBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final EnumMap<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        SHAPES.put(Direction.UP, Shapes.or(
            Block.box(0, 0, 0, 16, 3, 16),
            Block.box(2, 3, 0, 14, 5, 16)
        ));
        SHAPES.put(Direction.DOWN, Shapes.or(
            Block.box(0, 13, 0, 16, 16, 16),
            Block.box(2, 11, 0, 14, 13, 16)
        ));
        SHAPES.put(Direction.NORTH, Shapes.or(
            Block.box(0, 0, 13, 16, 16, 16),
            Block.box(2, 0, 11, 14, 16, 13)
        ));
        SHAPES.put(Direction.SOUTH, Shapes.or(
            Block.box(0, 0, 0, 16, 16, 3),
            Block.box(2, 0, 3, 14, 16, 5)
        ));
        SHAPES.put(Direction.WEST, Shapes.or(
            Block.box(13, 0, 0, 16, 16, 16),
            Block.box(11, 0, 2, 13, 16, 14)
        ));
        SHAPES.put(Direction.EAST, Shapes.or(
            Block.box(0, 0, 0, 3, 16, 16),
            Block.box(3, 0, 2, 5, 16, 14)
        ));
    }

    public HeattrapBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.UP)
            .setValue(POWERED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.getOrDefault(state.getValue(FACING), SHAPES.get(Direction.UP));
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (!canAttachTo(level, pos.relative(facing.getOpposite()), facing)) {
            return null;
        }
        return this.defaultBlockState()
            .setValue(FACING, facing)
            .setValue(POWERED, level.getBestNeighborSignal(pos) > 0);
    }

    private boolean canAttachTo(LevelReader level, BlockPos pos, Direction facing) {
        return level.getBlockState(pos).isFaceSturdy(level, pos, facing);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return canAttachTo(level, pos.relative(facing.getOpposite()), facing);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) {
            return;
        }
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        boolean powered = level.getBestNeighborSignal(pos) > 0;
        if (state.getValue(POWERED) != powered) {
            level.setBlock(pos, state.setValue(POWERED, powered), 3);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(CreateTheAirWarsModItems.HEATTRAP_CHARGE.get())) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (!(level.getBlockEntity(pos) instanceof HeattrapBlockEntity blockEntity)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!blockEntity.addCharge()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.create_the_air_wars.heattrap.full"), true);
            }
            return ItemInteractionResult.SUCCESS;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("message.create_the_air_wars.heattrap.loaded", blockEntity.getChargeCount(), HeattrapBlockEntity.MAX_CHARGES), true);
        }
        level.playSound(player, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 0.95F + level.random.nextFloat() * 0.1F);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HeattrapBlockEntity blockEntity) {
            player.displayClientMessage(Component.translatable("message.create_the_air_wars.heattrap.loaded", blockEntity.getChargeCount(), HeattrapBlockEntity.MAX_CHARGES), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CreateTheAirWarsModBlockEntities.HEATTRAP.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != CreateTheAirWarsModBlockEntities.HEATTRAP.get()) {
            return null;
        }
        return (lvl, blockPos, blockState, blockEntity) -> {
            if (blockEntity instanceof HeattrapBlockEntity heatTrapBlockEntity) {
                HeattrapBlockEntity.serverTick(lvl, blockPos, blockState, heatTrapBlockEntity);
            }
        };
    }
}
