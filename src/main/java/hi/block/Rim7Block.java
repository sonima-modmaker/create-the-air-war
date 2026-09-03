
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

import hi.procedures.Rim7RiedstounVkliuchionProcedure;

public class Rim7Block extends Block implements net.minecraft.world.level.block.EntityBlock {
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

	public Rim7Block() {
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
			default -> Shapes.or(box(5.5, 9.5, -16, 10.5, 14.5, 28), box(6, 10, 28, 10, 14, 31), box(7, 14, 1, 9, 16, 12));
			case NORTH -> Shapes.or(box(5.5, 9.5, -12, 10.5, 14.5, 32), box(6, 10, -15, 10, 14, -12), box(7, 14, 4, 9, 16, 15));
			case EAST -> Shapes.or(box(-16, 9.5, 5.5, 28, 14.5, 10.5), box(28, 10, 6, 31, 14, 10), box(1, 14, 7, 12, 16, 9));
			case WEST -> Shapes.or(box(-12, 9.5, 5.5, 32, 14.5, 10.5), box(-15, 10, 6, -12, 14, 10), box(4, 14, 7, 15, 16, 9));
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
			Rim7RiedstounVkliuchionProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ(), blockstate);
		}
	}
}
