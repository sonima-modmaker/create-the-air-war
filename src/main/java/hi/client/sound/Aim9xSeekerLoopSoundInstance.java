package hi.client.sound;

import hi.block.entity.Aim9xBlockEntity;
import hi.init.CreateTheAirWarsModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class Aim9xSeekerLoopSoundInstance extends AbstractTickableSoundInstance {
    private static final double MAX_AUDIBLE_DISTANCE_SQR = 4.0 * 4.0;
    private final Aim9xBlockEntity blockEntity;
    private final Aim9xBlockEntity.SeekerState expectedState;

    public Aim9xSeekerLoopSoundInstance(Aim9xBlockEntity blockEntity, Aim9xBlockEntity.SeekerState expectedState) {
        super(resolveSound(expectedState), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.blockEntity = blockEntity;
        this.expectedState = expectedState;
        this.looping = true;
        this.delay = 0;
        this.attenuation = Attenuation.LINEAR;
        this.volume = expectedState == Aim9xBlockEntity.SeekerState.LOCKING ? 0.65f : 0.45f;
        this.pitch = expectedState == Aim9xBlockEntity.SeekerState.LOCKING ? 1.08f : 1.0f;
        Vec3 worldPos = dev.ryanhcode.sable.Sable.HELPER.projectOutOfSubLevel(blockEntity.getLevel(), Vec3.atCenterOf(blockEntity.getBlockPos()));
        this.x = worldPos.x;
        this.y = worldPos.y;
        this.z = worldPos.z;
    }

    private static SoundEvent resolveSound(Aim9xBlockEntity.SeekerState state) {
        return state == Aim9xBlockEntity.SeekerState.LOCKING
            ? CreateTheAirWarsModSounds.LOCK.get()
            : CreateTheAirWarsModSounds.SEARCH.get();
    }

    public Aim9xBlockEntity.SeekerState getExpectedState() {
        return expectedState;
    }

    public void stopSound() {
        this.stop();
    }

    @Override
    public void tick() {
        if (blockEntity.isRemoved() || blockEntity.getLevel() == null || blockEntity.getSeekerState() != expectedState) {
            stop();
            return;
        }
        Vec3 worldPos = dev.ryanhcode.sable.Sable.HELPER.projectOutOfSubLevel(blockEntity.getLevel(), Vec3.atCenterOf(blockEntity.getBlockPos()));
        this.x = worldPos.x;
        this.y = worldPos.y;
        this.z = worldPos.z;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.player.distanceToSqr(this.x, this.y, this.z) > MAX_AUDIBLE_DISTANCE_SQR) {
            this.volume = 0.0f;
        } else {
            this.volume = expectedState == Aim9xBlockEntity.SeekerState.LOCKING ? 0.65f : 0.45f;
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }
}
