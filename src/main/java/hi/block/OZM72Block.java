
package hi.block;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.BlockHitResult;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.ItemInteractionResult; import net.minecraft.world.InteractionResult;import net.minecraft.world.InteractionHand;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import hi.procedures.OZM72PriShchielchkiePKMPoBlokuProcedure;

public class OZM72Block extends Block implements net.minecraft.world.level.block.EntityBlock {
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

	public OZM72Block() {
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
			default -> Shapes.or(box(5, 0, 5, 11, 8, 11), box(7.5, 8, 5.5, 8.5, 11, 6.5));
			case NORTH -> Shapes.or(box(5, 0, 5, 11, 8, 11), box(7.5, 8, 9.5, 8.5, 11, 10.5));
			case EAST -> Shapes.or(box(5, 0, 5, 11, 8, 11), box(5.5, 8, 7.5, 6.5, 11, 8.5));
			case WEST -> Shapes.or(box(5, 0, 5, 11, 8, 11), box(9.5, 8, 7.5, 10.5, 11, 8.5));
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
	protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack itemstack, net.minecraft.world.level.block.state.BlockState blockstate, net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, net.minecraft.world.entity.player.Player entity, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
		super.useItemOn(itemstack, blockstate, world, pos, entity, hand, hit);
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		double hitX = hit.getLocation().x;
		double hitY = hit.getLocation().y;
		double hitZ = hit.getLocation().z;
		Direction direction = hit.getDirection();
		OZM72PriShchielchkiePKMPoBlokuProcedure.execute(world, x, y, z);
		return net.minecraft.world.ItemInteractionResult.SUCCESS;
	}
}
