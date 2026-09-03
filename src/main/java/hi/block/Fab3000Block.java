
package hi.block;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import hi.procedures.Fab3000RiedstounVkliuchionProcedure;

public class Fab3000Block extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public Fab3000Block() {
		super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(1f, 10f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
		return switch (state.getValue(FACING)) {
			default -> Shapes.or(box(1, 2, -1, 15, 16, 23), box(6, 16, 15, 10, 18, 16), box(6, 16, 7, 10, 18, 8), box(1, 2, -16, 15, 16, -9), box(3, 4, 23, 13, 14, 30), box(5, 6, 30, 11, 12, 32), box(2, 3, 24, 14, 15, 26), box(1, 2, 26, 15, 16, 27),
					box(3, 4, -9, 13, 14, -1), box(5, 6, -16, 11, 12, -9));
			case NORTH -> Shapes.or(box(1, 2, -7, 15, 16, 17), box(6, 16, 0, 10, 18, 1), box(6, 16, 8, 10, 18, 9), box(1, 2, 25, 15, 16, 32), box(3, 4, -14, 13, 14, -7), box(5, 6, -16, 11, 12, -14), box(2, 3, -10, 14, 15, -8),
					box(1, 2, -11, 15, 16, -10), box(3, 4, 17, 13, 14, 25), box(5, 6, 25, 11, 12, 32));
			case EAST -> Shapes.or(box(-1, 2, 1, 23, 16, 15), box(15, 16, 6, 16, 18, 10), box(7, 16, 6, 8, 18, 10), box(-16, 2, 1, -9, 16, 15), box(23, 4, 3, 30, 14, 13), box(30, 6, 5, 32, 12, 11), box(24, 3, 2, 26, 15, 14),
					box(26, 2, 1, 27, 16, 15), box(-9, 4, 3, -1, 14, 13), box(-16, 6, 5, -9, 12, 11));
			case WEST -> Shapes.or(box(-7, 2, 1, 17, 16, 15), box(0, 16, 6, 1, 18, 10), box(8, 16, 6, 9, 18, 10), box(25, 2, 1, 32, 16, 15), box(-14, 4, 3, -7, 14, 13), box(-16, 6, 5, -14, 12, 11), box(-10, 3, 2, -8, 15, 14),
					box(-11, 2, 1, -10, 16, 15), box(17, 4, 3, 25, 14, 13), box(25, 6, 5, 32, 12, 11));
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
		if (world.getBestNeighborSignal(pos) > 0) {
			Fab3000RiedstounVkliuchionProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ(), blockstate);
		}
	}
}
