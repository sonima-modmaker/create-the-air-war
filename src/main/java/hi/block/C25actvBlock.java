
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

import hi.procedures.C25actvRiedstounVkliuchionProcedure;

public class C25actvBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public C25actvBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.GRAVEL).strength(1f, 10f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
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
			default -> Shapes.or(box(5.5, 5.5, -12, 10.5, 10.5, 17), box(4.5, 4.5, 17, 11.5, 11.5, 26), box(6.5, 6.5, 26, 9.5, 9.5, 30), box(8.5, 8.5, -15, 10.5, 10.5, -12), box(5.5, 8.5, -15, 7.5, 10.5, -12), box(5.5, 5.5, -15, 7.5, 7.5, -12),
					box(8.5, 5.5, -15, 10.5, 7.5, -12));
			case NORTH -> Shapes.or(box(5.5, 5.5, -1, 10.5, 10.5, 28), box(4.5, 4.5, -10, 11.5, 11.5, -1), box(6.5, 6.5, -14, 9.5, 9.5, -10), box(5.5, 8.5, 28, 7.5, 10.5, 31), box(8.5, 8.5, 28, 10.5, 10.5, 31), box(8.5, 5.5, 28, 10.5, 7.5, 31),
					box(5.5, 5.5, 28, 7.5, 7.5, 31));
			case EAST -> Shapes.or(box(-12, 5.5, 5.5, 17, 10.5, 10.5), box(17, 4.5, 4.5, 26, 11.5, 11.5), box(26, 6.5, 6.5, 30, 9.5, 9.5), box(-15, 8.5, 5.5, -12, 10.5, 7.5), box(-15, 8.5, 8.5, -12, 10.5, 10.5), box(-15, 5.5, 8.5, -12, 7.5, 10.5),
					box(-15, 5.5, 5.5, -12, 7.5, 7.5));
			case WEST -> Shapes.or(box(-1, 5.5, 5.5, 28, 10.5, 10.5), box(-10, 4.5, 4.5, -1, 11.5, 11.5), box(-14, 6.5, 6.5, -10, 9.5, 9.5), box(28, 8.5, 8.5, 31, 10.5, 10.5), box(28, 8.5, 5.5, 31, 10.5, 7.5), box(28, 5.5, 5.5, 31, 7.5, 7.5),
					box(28, 5.5, 8.5, 31, 7.5, 10.5));
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
			C25actvRiedstounVkliuchionProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ(), blockstate);
		}
	}
}
