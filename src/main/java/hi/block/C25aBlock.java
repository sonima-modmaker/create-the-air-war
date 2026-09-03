
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

public class C25aBlock extends Block implements net.minecraft.world.level.block.EntityBlock {
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

	public C25aBlock() {
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
			default -> Shapes.or(box(5.5, 8.5, -12, 10.5, 13.5, 17), box(4.5, 7.5, 17, 11.5, 14.5, 26), box(6.5, 9.5, 26, 9.5, 12.5, 30), box(8.5, 11.5, -15, 10.5, 13.5, -12), box(5.5, 11.5, -15, 7.5, 13.5, -12), box(5.5, 8.5, -15, 7.5, 10.5, -12),
					box(8.5, 8.5, -15, 10.5, 10.5, -12), box(7, 13, -1, 9, 16, 11));
			case NORTH -> Shapes.or(box(5.5, 8.5, -1, 10.5, 13.5, 28), box(4.5, 7.5, -10, 11.5, 14.5, -1), box(6.5, 9.5, -14, 9.5, 12.5, -10), box(5.5, 11.5, 28, 7.5, 13.5, 31), box(8.5, 11.5, 28, 10.5, 13.5, 31), box(8.5, 8.5, 28, 10.5, 10.5, 31),
					box(5.5, 8.5, 28, 7.5, 10.5, 31), box(7, 13, 5, 9, 16, 17));
			case EAST -> Shapes.or(box(-12, 8.5, 5.5, 17, 13.5, 10.5), box(17, 7.5, 4.5, 26, 14.5, 11.5), box(26, 9.5, 6.5, 30, 12.5, 9.5), box(-15, 11.5, 5.5, -12, 13.5, 7.5), box(-15, 11.5, 8.5, -12, 13.5, 10.5),
					box(-15, 8.5, 8.5, -12, 10.5, 10.5), box(-15, 8.5, 5.5, -12, 10.5, 7.5), box(-1, 13, 7, 11, 16, 9));
			case WEST -> Shapes.or(box(-1, 8.5, 5.5, 28, 13.5, 10.5), box(-10, 7.5, 4.5, -1, 14.5, 11.5), box(-14, 9.5, 6.5, -10, 12.5, 9.5), box(28, 11.5, 8.5, 31, 13.5, 10.5), box(28, 11.5, 5.5, 31, 13.5, 7.5), box(28, 8.5, 5.5, 31, 10.5, 7.5),
					box(28, 8.5, 8.5, 31, 10.5, 10.5), box(5, 13, 7, 17, 16, 9));
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
