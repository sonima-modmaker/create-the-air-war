package hi.client.sound;

import hi.block.entity.MinigunBlockEntity;
import hi.init.CreateTheAirWarsModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MinigunLoopSound extends AbstractTickableSoundInstance {
    private final MinigunBlockEntity blockEntity;

    public MinigunLoopSound(MinigunBlockEntity blockEntity) {
        super(CreateTheAirWarsModSounds.GUN_12_7MM_LOOP.get(), SoundSource.BLOCKS, RandomSource.create());
        this.blockEntity = blockEntity;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.75F;
        this.pitch = 1.25F;
        BlockPos pos = blockEntity.getBlockPos();
        this.x = pos.getX() + 0.5D;
        this.y = pos.getY() + 0.5D;
        this.z = pos.getZ() + 0.5D;
    }

    @Override
    public void tick() {
        if (this.blockEntity.isRemoved()) {
            this.stop();
            return;
        }
        BlockPos pos = blockEntity.getBlockPos();
        this.x = pos.getX() + 0.5D;
        this.y = pos.getY() + 0.5D;
        this.z = pos.getZ() + 0.5D;
        this.volume = 0.25F + 0.65F * this.blockEntity.getBarrelSpeed();
        this.pitch = 0.75F + 0.65F * this.blockEntity.getBarrelSpeed();
        if (!this.blockEntity.getLevel().isClientSide || (!this.blockEntity.isPowered() && this.blockEntity.getBarrelSpeed() <= 0.02F)) {
            this.stop();
        }
    }
}
