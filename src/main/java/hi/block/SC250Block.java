
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

import hi.procedures.SC250RiedstounVkliuchionProcedure;

public class SC250Block extends Block implements net.minecraft.world.level.block.EntityBlock {
	@org.jetbrains.annotations.Nullable
	@Override
	public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new hi.block.entity.LauncherCrashBlockEntity(pos, state);
	}
	@org.jetbrains.annotations.Nullable
	@Override
	public <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
		if (level.isClientSide()) return null;
		if (type != hi.init.CreateTheAirWarsModBlockEntities.LAUNCHER_CRASH.get()) return null;
		return (lvl, pos, st, be) -> {
			if (be instanceof hi.block.entity.LauncherCrashBlockEntity lbe) {
				hi.block.entity.LauncherCrashBlockEntity.serverTick(lvl, pos, st, lbe);
			}
		};
	}
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public SC250Block() {
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
			default -> Shapes.or(box(7, 14, 7, 9, 16, 15), box(6.5, 8.5, -2, 9.5, 11.5, 2), box(5, 7, 2, 11, 13, 4), box(6, 8, 21, 10, 12, 23), box(4.5, 6.5, 18, 11.5, 13.5, 21), box(4, 6, 4, 12, 14, 18));
			case NORTH -> Shapes.or(box(7, 14, 1, 9, 16, 9), box(6.5, 8.5, 14, 9.5, 11.5, 18), box(5, 7, 12, 11, 13, 14), box(6, 8, -7, 10, 12, -5), box(4.5, 6.5, -5, 11.5, 13.5, -2), box(4, 6, -2, 12, 14, 12));
			case EAST -> Shapes.or(box(7, 14, 7, 15, 16, 9), box(-2, 8.5, 6.5, 2, 11.5, 9.5), box(2, 7, 5, 4, 13, 11), box(21, 8, 6, 23, 12, 10), box(18, 6.5, 4.5, 21, 13.5, 11.5), box(4, 6, 4, 18, 14, 12));
			case WEST -> Shapes.or(box(1, 14, 7, 9, 16, 9), box(14, 8.5, 6.5, 18, 11.5, 9.5), box(12, 7, 5, 14, 13, 11), box(-7, 8, 6, -5, 12, 10), box(-5, 6.5, 4.5, -2, 13.5, 11.5), box(-2, 6, 4, 12, 14, 12));
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
			SC250RiedstounVkliuchionProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ(), blockstate);
		}
	}
}
