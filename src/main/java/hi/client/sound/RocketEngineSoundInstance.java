package hi.client.sound;

import hi.block.entity.RocketEngineBlockEntity;
import hi.init.CreateTheAirWarsModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class RocketEngineSoundInstance extends AbstractTickableSoundInstance {
    private final RocketEngineBlockEntity engine;
    private final boolean isThrottleSound;

    public RocketEngineSoundInstance(RocketEngineBlockEntity engine, SoundEvent sound, boolean isThrottleSound) {
        super(sound, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.engine = engine;
        this.isThrottleSound = isThrottleSound;
        this.x = engine.getBlockPos().getX() + 0.5;
        this.y = engine.getBlockPos().getY() + 0.5;
        this.z = engine.getBlockPos().getZ() + 0.5;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0f;
        this.attenuation = Attenuation.LINEAR;
    }

    @Override
    public void tick() {
        if (engine.isRemoved()) {
            this.stop();
            return;
        }

        RocketEngineBlockEntity.EngineState state = engine.getEngineState();
        
        if (isThrottleSound) {
            if (!engine.isThrottling()) {
                this.stop();
                return;
            }
            this.volume = 1.0f;
        } else {
            if (state == RocketEngineBlockEntity.EngineState.OFF) {
                this.stop();
                return;
            }
            this.volume = engine.getVolumeProgress();
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public boolean canPlaySound() {
        return !engine.isRemoved();
    }

    public void stopSound() {
        this.stop();
    }
}
