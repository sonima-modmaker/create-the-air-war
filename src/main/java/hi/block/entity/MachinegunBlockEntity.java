package hi.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import hi.entity.MachinegunShellEntity;
import hi.init.CreateTheAirWarsModBlockEntities;
import hi.init.CreateTheAirWarsModEntities;
import hi.init.CreateTheAirWarsModParticleTypes;
import hi.init.CreateTheAirWarsModSounds;
import hi.util.ProjectileLaunchHelper;

public class MachinegunBlockEntity extends BlockEntity {
	private static final int CLIENT_SOUND_UPDATE_INTERVAL = 4;
	private double accumulator;
	private boolean wasPowered;

	public MachinegunBlockEntity(BlockPos pos, BlockState state) {
		super(CreateTheAirWarsModBlockEntities.MACHINEGUN.get(), pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, MachinegunBlockEntity blockEntity) {
		if (level.isClientSide) {
			tickClient(level, pos, blockEntity);
			return;
		}
		boolean powered = level.getBestNeighborSignal(pos) > 0;
		if (powered != blockEntity.wasPowered) {
			blockEntity.wasPowered = powered;
			blockEntity.accumulator = 0;
			level.sendBlockUpdated(pos, state, state, 3);
			if (!powered) {
				level.playSound(null, pos, CreateTheAirWarsModSounds.GUN_12_7MM_LASTSHOT.get(), SoundSource.BLOCKS, 0.9f, 1f);
			}
		}
		if (powered) {
			blockEntity.accumulator += 0.6;
			while (blockEntity.accumulator >= 1) {
				blockEntity.accumulator -= 1;
				blockEntity.fireOnce(level, pos, state);
			}
		}
	}

	private void fireOnce(Level level, BlockPos pos, BlockState state) {
		Direction facing = state.getValue(hi.block.MachinegunBlock.FACING);
		Vec3 dir = Vec3.atLowerCornerOf(facing.getNormal()).normalize();
		double sx = pos.getX() + 0.5 + dir.x * 1.15;
		double sy = pos.getY() + 0.25;
		double sz = pos.getZ() + 0.5 + dir.z * 1.15;
		MachinegunShellEntity shell = new MachinegunShellEntity(CreateTheAirWarsModEntities.MACHINEGUN_SHELL.get(), level);
		double spread = 0.04;
		double rx = dir.x + (level.getRandom().nextDouble() - 0.5) * spread;
		double ry = dir.y + (level.getRandom().nextDouble() - 0.5) * spread;
		double rz = dir.z + (level.getRandom().nextDouble() - 0.5) * spread;

		if (level instanceof ServerLevel serverLevel) {
			ProjectileLaunchHelper.LaunchTransform launch = ProjectileLaunchHelper.resolveBlockLaunch(
				serverLevel,
				pos,
				sx,
				sy,
				sz,
				rx,
				ry,
				rz
			);
			shell.setPos(launch.position().x, launch.position().y, launch.position().z);
			shell.shoot(launch.direction().x, launch.direction().y, launch.direction().z, 8.8f, 0);
			shell.setLaunchContext(launch.position(), launch.inheritedVelocity());
			ProjectileLaunchHelper.applyInheritedVelocity(shell, launch.inheritedVelocity());
		} else {
			shell.setPos(sx, sy, sz);
			shell.shoot(rx, ry, rz, 8.8f, 0);
		}

		shell.setBaseDamage(4);

		level.addFreshEntity(shell);
		spawnSmoke(level, pos, state);
	}

	private void spawnSmoke(Level level, BlockPos pos, BlockState state) {
		if (level instanceof ServerLevel serverLevel) {
			Direction facing = state.getValue(hi.block.MachinegunBlock.FACING);
			Vec3 dir = Vec3.atLowerCornerOf(facing.getNormal()).normalize();
			double px = pos.getX() + 0.5 + dir.x * 1.1;
			double py = pos.getY() + 0.7;
			double pz = pos.getZ() + 0.5 + dir.z * 1.1;
			if (serverLevel.getRandom().nextFloat() > 0.4f) {
				return;
			}
			serverLevel.sendParticles(CreateTheAirWarsModParticleTypes.MACHINEGUN_SMOKE.get(), px, py, pz, 1, 0.05, 0.02, 0.05, 0.02);
		}
	}

	private static void tickClient(Level level, BlockPos pos, MachinegunBlockEntity blockEntity) {
		if (((level.getGameTime() + pos.asLong()) & (CLIENT_SOUND_UPDATE_INTERVAL - 1)) != 0L) {
			return;
		}
		hi.client.sound.MachinegunClientSound.tick(blockEntity);
	}

	public boolean isWasPowered() {
		return wasPowered;
	}

	@Override
	protected void loadAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		this.accumulator = tag.getDouble("accumulator");
		this.wasPowered = tag.getBoolean("wasPowered");
	}

	@Override
	protected void saveAdditional(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		tag.putDouble("accumulator", this.accumulator);
		tag.putBoolean("wasPowered", this.wasPowered);
	}

	@Override
	public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
		return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		saveAdditional(tag, provider);
		return tag;
	}
}
