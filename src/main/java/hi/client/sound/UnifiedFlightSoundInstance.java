package hi.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

/** Projectile fly loop adapted from SuperbWarfare (GPL-3.0). */
public class UnifiedFlightSoundInstance extends AbstractTickableSoundInstance {
    private final Entity entity;
    private double lastDistance;
    private int fade;
    private boolean dying;

    public UnifiedFlightSoundInstance(Entity entity, SoundEvent soundEvent) {
        super(soundEvent, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0f;
        this.pitch = 1.0F;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    public void stopSound() {
        this.stop();
    }

    @Override
    public void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || entity.isRemoved() || !entity.isAlive()) {
            this.stop();
            return;
        }

        if (!canPlayProjectileSound()) dying = true;
        if (dying) {
            if (fade > 0) fade--;
            else {
                stop();
                return;
            }
        } else if (fade < 3) {
            fade++;
        }

        double speed = entity.getDeltaMovement().length();
        float projectileVolume = entity instanceof hi.entity.Fab3000trueEntity ? 0.5F : 0.7F;
        this.volume = (float) Math.min(projectileVolume * 0.1D * speed, 1.5D) * fade;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
        this.pitch = 1.0F;

        double distance = entity.position().subtract(minecraft.gameRenderer.getMainCamera().getPosition()).length();
        this.pitch += (float) (0.16D * Math.atan(lastDistance - distance));
        this.lastDistance = distance;
    }

    private boolean canPlayProjectileSound() {
        if (entity instanceof hi.entity.Aim9xbultEntity missile) return missile.isEngineActive();
        if (entity instanceof hi.entity.TomahawkbultEntity missile) return missile.isEngineActive();
        return entity.getDeltaMovement().lengthSqr() >= 0.04D;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public boolean canPlaySound() {
        return !entity.isRemoved() && entity.isAlive() && !dying;
    }
}
