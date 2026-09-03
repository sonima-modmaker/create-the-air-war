package hi.block;

import hi.block.entity.RocketDataLinkBlockEntity;
// import hi.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import hi.init.CreateTheAirWarsModBlockEntities;

import java.util.EnumMap;

public class RocketDataLinkBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    
    // Коллизии для каждого направления - блок "прилипает" к поверхности
    private static final EnumMap<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);
    
    static {
        // UP - на полу, смотрит вверх
        SHAPES.put(Direction.UP, Block.box(1, 0, 1, 15, 10, 15));
        // DOWN - на потолке, смотрит вниз
        SHAPES.put(Direction.DOWN, Block.box(1, 6, 1, 15, 16, 15));
        // NORTH - на южной стене
        SHAPES.put(Direction.NORTH, Block.box(1, 1, 6, 15, 15, 16));
        // SOUTH - на северной стене
        SHAPES.put(Direction.SOUTH, Block.box(1, 1, 0, 15, 15, 10));
        // WEST - на восточной стене
        SHAPES.put(Direction.WEST, Block.box(6, 1, 1, 16, 15, 15));
        // EAST - на западной стене
        SHAPES.put(Direction.EAST, Block.box(0, 1, 1, 10, 15, 15));
    }
    
    public RocketDataLinkBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.UP)
            .setValue(POWERED, false));
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.getOrDefault(state.getValue(FACING), SHAPES.get(Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Блок крепится к поверхности на которую кликнули
        Direction facing = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        
        // Проверяем что можно прикрепиться
        if (!canAttachTo(level, pos.relative(facing.getOpposite()), facing)) {
            return null;
        }
        
        int power = level.getBestNeighborSignal(pos);
        return defaultBlockState()
            .setValue(FACING, facing)
            .setValue(POWERED, power > 0);
    }
    
    private boolean canAttachTo(LevelReader level, BlockPos pos, Direction facing) {
        BlockState state = level.getBlockState(pos);
        return state.isFaceSturdy(level, pos, facing);
    }
    
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos attachedTo = pos.relative(facing.getOpposite());
        return canAttachTo(level, attachedTo, facing);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            // Проверяем что блок всё ещё может держаться
            if (!canSurvive(state, level, pos)) {
                level.destroyBlock(pos, true);
                return;
            }
            
            int newPower = level.getBestNeighborSignal(pos);
            boolean wasPowered = state.getValue(POWERED);
            boolean isPowered = newPower > 0;
            
            if (isPowered != wasPowered) {
                level.setBlock(pos, state.setValue(POWERED, isPowered), 3);
            }
            
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RocketDataLinkBlockEntity dataLink) {
                dataLink.updateAllEnginesPower(newPower);
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            int power = level.getBestNeighborSignal(pos);
            if (power > 0) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof RocketDataLinkBlockEntity dataLink) {
                    dataLink.updateAllEnginesPower(power);
                }
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RocketDataLinkBlockEntity dataLink) {
                openClientScreen(dataLink);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void openClientScreen(RocketDataLinkBlockEntity dataLink) {
        try {
            Class<?> helperClass = Class.forName("hi.client.RocketDataLinkClientScreenOpener");
            helperClass.getMethod("open", RocketDataLinkBlockEntity.class).invoke(null, dataLink);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CreateTheAirWarsModBlockEntities.ROCKET_DATA_LINK.get().create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }
}
