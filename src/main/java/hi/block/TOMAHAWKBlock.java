
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import hi.procedures.TOMAHAWKRiedstounVkliuchionProcedure;
import hi.block.entity.TomahawkBlockEntity;
import hi.init.CreateTheAirWarsModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class TOMAHAWKBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public TOMAHAWKBlock() {
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
			default -> Shapes.or(box(5, 8, -7, 11, 14, 27), box(6, 9, -13, 10, 13, -7), box(5.5, 8.5, 27, 10.5, 13.5, 29), box(7, 14, 2, 9, 16, 14));
			case NORTH -> Shapes.or(box(5, 8, -11, 11, 14, 23), box(6, 9, 23, 10, 13, 29), box(5.5, 8.5, -13, 10.5, 13.5, -11), box(7, 14, 2, 9, 16, 14));
			case EAST -> Shapes.or(box(-7, 8, 5, 27, 14, 11), box(-13, 9, 6, -7, 13, 10), box(27, 8.5, 5.5, 29, 13.5, 10.5), box(2, 14, 7, 14, 16, 9));
			case WEST -> Shapes.or(box(-11, 8, 5, 23, 14, 11), box(23, 9, 6, 29, 13, 10), box(-13, 8.5, 5.5, -11, 13.5, 10.5), box(2, 14, 7, 14, 16, 9));
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
			TOMAHAWKRiedstounVkliuchionProcedure.execute(world, pos.getX(), pos.getY(), pos.getZ(), blockstate);
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TomahawkBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) return null;
		if (type != CreateTheAirWarsModBlockEntities.TOMAHAWK.get()) return null;
		return (lvl, pos, st, be) -> {
			if (be instanceof TomahawkBlockEntity tbe) {
				TomahawkBlockEntity.serverTick(lvl, pos, st, tbe);
			}
		};
	}
}
