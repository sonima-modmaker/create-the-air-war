package hi.block;

import com.mojang.serialization.MapCodec;
import hi.block.entity.VihrLauncherBlockEntity;
import hi.init.CreateTheAirWarsModBlockEntities;
import hi.init.CreateTheAirWarsModBlocks;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;

public class VihrLauncherBlock extends Block implements EntityBlock {
    public static final MapCodec<VihrLauncherBlock> CODEC = simpleCodec(VihrLauncherBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Direction> HORIZONTAL_FACING = EnumProperty.create("horizontal_facing", Direction.class, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
    private static final EnumMap<Direction, VoxelShape> BASE_SHAPES = new EnumMap<>(Direction.class);

    static {
        BASE_SHAPES.put(Direction.UP, Shapes.or(
            Block.box(0, 2, -4, 16, 16, 23),
            Block.box(2, 0, 2, 14, 2, 14)
        ));
        BASE_SHAPES.put(Direction.DOWN, Shapes.or(
            Block.box(0, 0, -7, 16, 14, 20),
            Block.box(2, 14, 2, 14, 16, 14)
        ));
        BASE_SHAPES.put(Direction.NORTH, Shapes.or(
            Block.box(0, 0, 0, 16, 16, 20),
            Block.box(2, 2, 14, 14, 14, 16)
        ));
        BASE_SHAPES.put(Direction.SOUTH, Shapes.or(
            Block.box(0, 0, -4, 16, 16, 16),
            Block.box(2, 2, 0, 14, 14, 2)
        ));
        BASE_SHAPES.put(Direction.WEST, Shapes.or(
            Block.box(0, 0, 0, 20, 16, 16),
            Block.box(14, 2, 2, 16, 14, 14)
        ));
        BASE_SHAPES.put(Direction.EAST, Shapes.or(
            Block.box(-4, 0, 0, 16, 16, 16),
            Block.box(0, 2, 2, 2, 14, 14)
        ));
    }

    public VihrLauncherBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (!canAttachTo(level, pos.relative(facing.getOpposite()), facing)) {
            return null;
        }
        Direction horizontalFacing = context.getHorizontalDirection().getOpposite();
        if (facing.getAxis().isHorizontal()) {
            horizontalFacing = facing.getOpposite();
        }
        return this.defaultBlockState().setValue(FACING, facing).setValue(HORIZONTAL_FACING, horizontalFacing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer != null && level.getBlockEntity(pos) instanceof VihrLauncherBlockEntity launcher) {
            launcher.initializeRotationFromPlacer(placer);
        }
    }

    private static boolean canAttachTo(LevelReader level, BlockPos pos, Direction facing) {
        return level.getBlockState(pos).isFaceSturdy(level, pos, facing);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return canAttachTo(level, pos.relative(facing.getOpposite()), facing);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide && !canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        Direction horizontalFacing = state.getValue(HORIZONTAL_FACING);
        if (facing == Direction.UP || facing == Direction.DOWN) {
            VoxelShape base = BASE_SHAPES.getOrDefault(facing, BASE_SHAPES.get(Direction.UP));
            return rotateShapeY(base, horizontalFacing);
        }
        return BASE_SHAPES.getOrDefault(facing, BASE_SHAPES.get(Direction.UP));
    }

    private static VoxelShape rotateShapeY(VoxelShape shape, Direction facing) {
        if (facing == Direction.NORTH) {
            return shape;
        }
        VoxelShape[] buffer = new VoxelShape[] { Shapes.empty() };
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            switch (facing) {
                case SOUTH -> buffer[0] = Shapes.or(buffer[0], Block.box(
                    16.0D - maxX * 16.0D,
                    minY * 16.0D,
                    16.0D - maxZ * 16.0D,
                    16.0D - minX * 16.0D,
                    maxY * 16.0D,
                    16.0D - minZ * 16.0D
                ));
                case EAST -> buffer[0] = Shapes.or(buffer[0], Block.box(
                    16.0D - maxZ * 16.0D,
                    minY * 16.0D,
                    minX * 16.0D,
                    16.0D - minZ * 16.0D,
                    maxY * 16.0D,
                    maxX * 16.0D
                ));
                case WEST -> buffer[0] = Shapes.or(buffer[0], Block.box(
                    minZ * 16.0D,
                    minY * 16.0D,
                    16.0D - maxX * 16.0D,
                    maxZ * 16.0D,
                    maxY * 16.0D,
                    16.0D - minX * 16.0D
                ));
                default -> buffer[0] = Shapes.or(buffer[0], Block.box(
                    minX * 16.0D, minY * 16.0D, minZ * 16.0D,
                    maxX * 16.0D, maxY * 16.0D, maxZ * 16.0D
                ));
            }
        });
        return buffer[0];
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(CreateTheAirWarsModItems.CAMERA_LINK.get())) {
            if (!level.isClientSide) {
                CameraLinkItem.setSelectedVihr(stack, pos);
                player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.vihr_selected", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GREEN), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!stack.is(CreateTheAirWarsModItems.VIHR_ROCKET.get())) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }

        if (!(level.getBlockEntity(pos) instanceof VihrLauncherBlockEntity launcher)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!launcher.addRocket()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.create_the_air_wars.vihr.full"), true);
            }
            return ItemInteractionResult.SUCCESS;
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("message.create_the_air_wars.vihr.loaded", launcher.getRocketCount(), VihrLauncherBlockEntity.MAX_ROCKETS), true);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown() && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            if (!level.isClientSide) {
                Direction currentHorizontal = state.getValue(HORIZONTAL_FACING);
                Direction nextHorizontal = currentHorizontal.getClockWise();
                BlockState newState = state.setValue(HORIZONTAL_FACING, nextHorizontal);
                level.setBlock(pos, newState, 3);
                if (level.getBlockEntity(pos) instanceof VihrLauncherBlockEntity launcher) {
                    launcher.setBlockState(newState);
                    // Re-initialize dynamic rendering rotations
                    launcher.initializeRotationFromPlacer(player);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide && level.getBlockEntity(pos) instanceof VihrLauncherBlockEntity launcher) {
            player.displayClientMessage(Component.translatable("message.create_the_air_wars.vihr.loaded", launcher.getRocketCount(), VihrLauncherBlockEntity.MAX_ROCKETS), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VihrLauncherBlockEntity(pos, state);
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)))
                    .setValue(HORIZONTAL_FACING, rot.rotate(state.getValue(HORIZONTAL_FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirrorIn) {
        return state.setValue(FACING, mirrorIn.mirror(state.getValue(FACING)))
                    .setValue(HORIZONTAL_FACING, mirrorIn.mirror(state.getValue(HORIZONTAL_FACING)));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == CreateTheAirWarsModBlockEntities.VIHR.get() ? (lvl, pos, st, be) -> {
            if (be instanceof VihrLauncherBlockEntity launcher) {
                launcher.tick(lvl, pos, st);
            }
        } : null;
    }
}
