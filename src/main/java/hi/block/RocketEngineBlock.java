package hi.block;

import hi.block.entity.RocketEngineBlockEntity;
import hi.item.RocketDataLinkItem;
// import hi.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import hi.init.CreateTheAirWarsModBlockEntities;
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
import org.jetbrains.annotations.Nullable;

public class RocketEngineBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public RocketEngineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.UP)
            .setValue(POWERED, false)
            .setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int signal = context.getLevel().getBestNeighborSignal(context.getClickedPos());
        return this.defaultBlockState()
            .setValue(FACING, context.getNearestLookingDirection().getOpposite())
            .setValue(POWERED, signal > 0);
    }
    
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            int signal = level.getBestNeighborSignal(pos);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RocketEngineBlockEntity engine) {
                engine.setThrustPower(signal / 15.0f);
            }
        }
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof RocketDataLinkItem) {
            return net.minecraft.world.ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof RocketEngineBlockEntity engine) {
            if (engine.getEngineState() == RocketEngineBlockEntity.EngineState.OFF && engine.getFuelAmount() <= 0) {
                player.displayClientMessage(Component.literal("No fuel! Use Rocket Fuel bucket or pump.")
                    .withStyle(ChatFormatting.RED), true);
                return InteractionResult.CONSUME;
            }
            engine.toggleEngine();
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, 
                                BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            // Get analog redstone signal (0-15)
            int signalStrength = level.getBestNeighborSignal(pos);
            boolean powered = signalStrength > 0;
            
            if (powered != state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, powered), 3);
            }
            
            // Update thrust power based on signal strength (0-15 -> 0-100%)
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RocketEngineBlockEntity engine) {
                float thrustPower = signalStrength / 15.0f;
                engine.setThrustPower(thrustPower);
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return CreateTheAirWarsModBlockEntities.ROCKET_ENGINE.get().create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == CreateTheAirWarsModBlockEntities.ROCKET_ENGINE.get() ?
            (lvl, p, st, be) -> {
                if (lvl.isClientSide) {
                    RocketEngineBlockEntity.clientTick(lvl, p, st, (RocketEngineBlockEntity) be);
                } else {
                    RocketEngineBlockEntity.serverTick(lvl, p, st, (RocketEngineBlockEntity) be);
                }
            } : null;
    }
}
