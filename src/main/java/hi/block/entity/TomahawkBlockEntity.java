package hi.block.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import hi.init.CreateTheAirWarsModBlockEntities;

public class TomahawkBlockEntity extends BlockEntity {
	private static final double CRASH_DELTA_THRESHOLD = 8.0;
	private static final int CRASH_CHECK_INTERVAL_TICKS = 4;
	private boolean hasTarget = false;
	private double targetX;
	private double targetY;
	private double targetZ;
	private final Vector3d lastVelocity = new Vector3d();
	private boolean velocityInitialized = false;
	private int crashCheckCooldown;

	public TomahawkBlockEntity(BlockPos pos, BlockState state) {
		super(CreateTheAirWarsModBlockEntities.TOMAHAWK.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, TomahawkBlockEntity be) {
		if (level == null || level.isClientSide()) return;
		if (be.crashCheckCooldown-- <= 0) {
			be.crashCheckCooldown = CRASH_CHECK_INTERVAL_TICKS;
			try {
				if (hi.util.LauncherCrashDetector.detectCrash(be, level, be.lastVelocity, be.velocityInitialized)) {
					hi.procedures.TomahawktrueKoghdaSnariadPopadaietVBlokProcedure.execute(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
					return;
				}
				be.velocityInitialized = hi.util.LauncherCrashDetector.updateVelocity(be, be.lastVelocity);
			} catch (Throwable ignored) {
			}
		}
	}

	public boolean hasTarget() {
		return hasTarget;
	}

	public double getTargetX() {
		return targetX;
	}

	public double getTargetY() {
		return targetY;
	}

	public double getTargetZ() {
		return targetZ;
	}

	public void setTarget(double x, double y, double z) {
		this.hasTarget = true;
		this.targetX = x;
		this.targetY = y;
		this.targetZ = z;
		this.setChanged();
	}

	@Override
	protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		this.hasTarget = tag.getBoolean("hasTarget");
		this.targetX = tag.getDouble("targetX");
		this.targetY = tag.getDouble("targetY");
		this.targetZ = tag.getDouble("targetZ");
	}

	@Override
	protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		tag.putBoolean("hasTarget", this.hasTarget);
		tag.putDouble("targetX", this.targetX);
		tag.putDouble("targetY", this.targetY);
		tag.putDouble("targetZ", this.targetZ);
	}
}
