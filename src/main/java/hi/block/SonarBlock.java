
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
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import hi.procedures.SonarPriShchielchkiePKMPoBlokuProcedure;
import hi.procedures.SonarPriObnovlieniiTikaProcedure;

import java.util.EnumMap;

public class SonarBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final EnumMap<Direction, VoxelShape> SHAPES = createShapes();

	public SonarBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(5f, 20f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
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
		return getCurrentShape(state);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return getCurrentShape(state);
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
	public void onPlace(BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving) {
		super.onPlace(blockstate, world, pos, oldState, moving);
		world.scheduleTick(pos, this, 80);
	}

	@Override
	public void tick(BlockState blockstate, ServerLevel world, BlockPos pos, RandomSource random) {
		super.tick(blockstate, world, pos, random);
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		SonarPriObnovlieniiTikaProcedure.execute(world, x, y, z);
		world.scheduleTick(pos, this, 80);
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
		SonarPriShchielchkiePKMPoBlokuProcedure.execute(entity);
		return net.minecraft.world.ItemInteractionResult.SUCCESS;
	}

	private static EnumMap<Direction, VoxelShape> createShapes() {
		EnumMap<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
		VoxelShape north = Shapes.or(
			box(2, 0, 1, 14, 6, 11),
			box(1, 4, 9, 2, 13, 10),
			box(1, 4, 5, 2, 12, 6),
			box(2, 4.85195, 11, 14, 15.2388, 16)
		);
		shapes.put(Direction.NORTH, north);
		shapes.put(Direction.SOUTH, rotateNorthShape(north, Direction.SOUTH));
		shapes.put(Direction.EAST, rotateNorthShape(north, Direction.EAST));
		shapes.put(Direction.WEST, rotateNorthShape(north, Direction.WEST));
		return shapes;
	}

	private static VoxelShape getCurrentShape(BlockState state) {
		return SHAPES.getOrDefault(state.getValue(FACING), SHAPES.get(Direction.NORTH));
	}

	private static VoxelShape rotateNorthShape(VoxelShape shape, Direction direction) {
		VoxelShape[] result = new VoxelShape[] { Shapes.empty() };
		shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
			result[0] = Shapes.or(result[0], rotateBox(minX, minY, minZ, maxX, maxY, maxZ, direction))
		);
		return result[0];
	}

	private static VoxelShape rotateBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, Direction direction) {
		return switch (direction) {
			case SOUTH -> box(16 - maxX, minY, 16 - maxZ, 16 - minX, maxY, 16 - minZ);
			case EAST -> box(16 - maxZ, minY, minX, 16 - minZ, maxY, maxX);
			case WEST -> box(minZ, minY, 16 - maxX, maxZ, maxY, 16 - minX);
			default -> box(minX, minY, minZ, maxX, maxY, maxZ);
		};
	}
}
