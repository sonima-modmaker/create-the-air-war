package hi.client.sound;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;

import hi.init.CreateTheAirWarsModSounds;

import hi.block.entity.MachinegunBlockEntity;

@OnlyIn(Dist.CLIENT)
public class MachinegunLoopSound extends AbstractTickableSoundInstance {
	private final MachinegunBlockEntity blockEntity;

	public MachinegunLoopSound(MachinegunBlockEntity blockEntity) {
		super(CreateTheAirWarsModSounds.GUN_12_7MM_LOOP.get(), SoundSource.BLOCKS, RandomSource.create());
		this.blockEntity = blockEntity;
		this.looping = true;
		this.delay = 0;
		this.volume = 0.8f;
		this.pitch = 1f;
		BlockPos pos = blockEntity.getBlockPos();
		this.x = pos.getX() + 0.5;
		this.y = pos.getY() + 0.5;
		this.z = pos.getZ() + 0.5;
	}

	@Override
	public void tick() {
		if (this.blockEntity.isRemoved()) {
			this.stop();
			return;
		}
		BlockPos pos = blockEntity.getBlockPos();
		this.x = pos.getX() + 0.5;
		this.y = pos.getY() + 0.5;
		this.z = pos.getZ() + 0.5;
		if (!this.blockEntity.getLevel().isClientSide || !this.blockEntity.isWasPowered()) {
			this.stop();
		}
	}
}
