package hi.block;

import com.mojang.serialization.MapCodec;
import hi.block.entity.CameraBlockEntity;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
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

public class CameraBlock extends DirectionalBlock implements EntityBlock {
    public static final MapCodec<CameraBlock> CODEC = simpleCodec(CameraBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Direction> HORIZONTAL_FACING = EnumProperty.create("horizontal_facing", Direction.class, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
    private static final EnumMap<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        VoxelShape floor = Shapes.or(
            Block.box(4, 0, 4, 12, 2, 12),
            Block.box(6, 2, 6, 10, 5, 10),
            Block.box(3, 5, 3, 13, 11, 13),
            Block.box(6, 6, 1, 10, 10, 3)
        );
        VoxelShape ceiling = Shapes.or(
            Block.box(4, 14, 4, 12, 16, 12),
            Block.box(6, 11, 6, 10, 14, 10),
            Block.box(3, 5, 3, 13, 11, 13),
            Block.box(6, 6, 13, 10, 10, 15)
        );
        SHAPES.put(Direction.UP, floor);
        SHAPES.put(Direction.DOWN, ceiling);
        SHAPES.put(Direction.NORTH, Shapes.or(
            Block.box(4, 4, 14, 12, 12, 16),
            Block.box(6, 6, 11, 10, 10, 14),
            Block.box(3, 3, 5, 13, 13, 11),
            Block.box(6, 1, 6, 10, 3, 10)
        ));
        SHAPES.put(Direction.SOUTH, Shapes.or(
            Block.box(4, 4, 0, 12, 12, 2),
            Block.box(6, 6, 2, 10, 10, 5),
            Block.box(3, 3, 5, 13, 13, 11),
            Block.box(6, 13, 6, 10, 15, 10)
        ));
        SHAPES.put(Direction.WEST, Shapes.or(
            Block.box(14, 4, 4, 16, 12, 12),
            Block.box(11, 6, 6, 14, 10, 10),
            Block.box(5, 3, 3, 11, 13, 13),
            Block.box(6, 6, 1, 10, 10, 3)
        ));
        SHAPES.put(Direction.EAST, Shapes.or(
            Block.box(0, 4, 4, 2, 12, 12),
            Block.box(2, 6, 6, 5, 10, 10),
            Block.box(5, 3, 3, 11, 13, 13),
            Block.box(6, 6, 13, 10, 10, 15)
        ));
    }

    public CameraBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
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
        Direction horizontalFacing = facing.getAxis().isVertical() ? context.getHorizontalDirection() : facing;
        return this.defaultBlockState().setValue(FACING, facing).setValue(HORIZONTAL_FACING, horizontalFacing);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return canAttachTo(level, pos.relative(facing.getOpposite()), facing);
    }

    private static boolean canAttachTo(LevelReader level, BlockPos pos, Direction facing) {
        BlockState state = level.getBlockState(pos);
        return state.isFaceSturdy(level, pos, facing);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide && !canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(CreateTheAirWarsModItems.CAMERA_LINK.get())) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (!level.isClientSide) {
            CameraLinkItem.setSelectedCamera(stack, pos);
            player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_link.camera_selected", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GREEN), true);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        VoxelShape shape = SHAPES.getOrDefault(facing, SHAPES.get(Direction.NORTH));
        if (facing == Direction.UP) {
            return rotateShapeY(shape, state.getValue(HORIZONTAL_FACING));
        }
        return shape;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        Direction facing = state.getValue(FACING);
        Direction horizontalFacing = state.getValue(HORIZONTAL_FACING);
        if (facing.getAxis().isVertical()) {
            return state.setValue(HORIZONTAL_FACING, rotation.rotate(horizontalFacing));
        }
        return state.setValue(FACING, rotation.rotate(facing)).setValue(HORIZONTAL_FACING, rotation.rotate(horizontalFacing));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        Direction reference = state.getValue(FACING).getAxis().isVertical() ? state.getValue(HORIZONTAL_FACING) : state.getValue(FACING);
        return this.rotate(state, mirror.getRotation(reference));
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
                    minX * 16.0D,
                    minY * 16.0D,
                    minZ * 16.0D,
                    maxX * 16.0D,
                    maxY * 16.0D,
                    maxZ * 16.0D
                ));
            }
        });
        return buffer[0];
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CameraBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != CreateTheAirWarsModBlockEntities.CAMERA.get()) {
            return null;
        }
        return (lvl, blockPos, blockState, blockEntity) -> {
            if (!lvl.isClientSide && blockEntity instanceof hi.block.entity.CameraBlockEntity camera) {
                hi.block.entity.CameraBlockEntity.serverTick(lvl, blockPos, blockState, camera);
            }
        };
    }
}
