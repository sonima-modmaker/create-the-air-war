package hi.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;

public class Aim9xBlock extends Block implements net.minecraft.world.level.block.EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final EnumMap<Direction, VoxelShape> SHAPES = createShapes();

    public Aim9xBlock() {
        super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(1f, 10f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new hi.block.entity.Aim9xBlockEntity(pos, state);
    }

    @Override
    public <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        if (type != hi.init.CreateTheAirWarsModBlockEntities.AIM9X.get()) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof hi.block.entity.Aim9xBlockEntity aim9xBlockEntity) {
                if (lvl.isClientSide()) {
                    hi.block.entity.Aim9xBlockEntity.clientTick(lvl, pos, st, aim9xBlockEntity);
                } else {
                    hi.block.entity.Aim9xBlockEntity.serverTick(lvl, pos, st, aim9xBlockEntity);
                }
            }
        };
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
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return getMissileShape(state.getValue(FACING));
    }

    public static VoxelShape getMissileShape(Direction facing) {
        return SHAPES.getOrDefault(facing, SHAPES.get(Direction.NORTH));
    }

    private static EnumMap<Direction, VoxelShape> createShapes() {
        EnumMap<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
        VoxelShape north = Shapes.empty();

        north = Shapes.or(north, box(4.4645, 8.9645, -9.0, 11.5355, 16.0355, 32.0));
        north = Shapes.or(north, box(5.8787, 10.3787, -16.0, 10.1213, 14.6213, -14.0));
        north = Shapes.or(north, box(5.1716, 9.6716, -14.0, 10.8284, 15.3284, -9.0));
        north = Shapes.or(north, box(3.0503, 7.5503, -5.0, 12.9497, 17.4497, 4.0));
        north = Shapes.or(north, box(2.3431, 6.8431, 23.0, 13.6569, 18.1569, 29.0));
        north = Shapes.or(north, box(2.3431, 6.8431, 23.0, 13.6569, 18.1569, 29.0));
        north = Shapes.or(north, box(3.0503, 7.5503, -5.0, 12.9497, 17.4497, 4.0));

        shapes.put(Direction.NORTH, north);
        shapes.put(Direction.SOUTH, rotateNorthShape(Direction.SOUTH));
        shapes.put(Direction.EAST, rotateNorthShape(Direction.EAST));
        shapes.put(Direction.WEST, rotateNorthShape(Direction.WEST));
        return shapes;
    }

    private static VoxelShape rotateNorthShape(Direction facing) {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.or(shape, rotateBox(4.4645, 8.9645, -9.0, 11.5355, 16.0355, 32.0, facing));
        shape = Shapes.or(shape, rotateBox(5.8787, 10.3787, -16.0, 10.1213, 14.6213, -14.0, facing));
        shape = Shapes.or(shape, rotateBox(5.1716, 9.6716, -14.0, 10.8284, 15.3284, -9.0, facing));
        shape = Shapes.or(shape, rotateBox(3.0503, 7.5503, -5.0, 12.9497, 17.4497, 4.0, facing));
        shape = Shapes.or(shape, rotateBox(2.3431, 6.8431, 23.0, 13.6569, 18.1569, 29.0, facing));
        shape = Shapes.or(shape, rotateBox(2.3431, 6.8431, 23.0, 13.6569, 18.1569, 29.0, facing));
        shape = Shapes.or(shape, rotateBox(3.0503, 7.5503, -5.0, 12.9497, 17.4497, 4.0, facing));
        return shape;
    }

    private static VoxelShape rotateBox(double x1, double y1, double z1, double x2, double y2, double z2, Direction facing) {
        return switch (facing) {
            case SOUTH -> box(16.0 - x2, y1, 16.0 - z2, 16.0 - x1, y2, 16.0 - z1);
            case EAST -> box(16.0 - z2, y1, x1, 16.0 - z1, y2, x2);
            case WEST -> box(z1, y1, 16.0 - x2, z2, y2, 16.0 - x1);
            default -> box(x1, y1, z1, x2, y2, z2);
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public void neighborChanged(BlockState blockstate, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(blockstate, world, pos, neighborBlock, fromPos, moving);
    }
}
