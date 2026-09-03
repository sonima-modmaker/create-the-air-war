package hi.client.sound;

import hi.block.entity.Aim9xBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class Aim9xSeekerSoundManager {
    private static final double MAX_TRACK_DISTANCE_SQR = 96.0 * 96.0;
    private static final Map<Long, Aim9xSeekerLoopSoundInstance> ACTIVE = new HashMap<>();

    private Aim9xSeekerSoundManager() {
    }

    public static void clientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            clear();
            return;
        }

        Iterator<Map.Entry<Long, Aim9xSeekerLoopSoundInstance>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Aim9xSeekerLoopSoundInstance> entry = iterator.next();
            Aim9xSeekerLoopSoundInstance sound = entry.getValue();
            if (sound == null || sound.isStopped()) {
                iterator.remove();
            }
        }
    }

    public static void update(Aim9xBlockEntity launcher) {
        if (launcher == null || launcher.isRemoved() || launcher.getLevel() == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        if (minecraft.player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(launcher.getBlockPos())) > MAX_TRACK_DISTANCE_SQR) {
            stopAndRemove(launcher.getBlockPos().asLong() ^ System.identityHashCode(launcher));
            return;
        }

        long key = launcher.getBlockPos().asLong() ^ System.identityHashCode(launcher);
        Aim9xBlockEntity.SeekerState state = launcher.getSeekerState();
        if (state == Aim9xBlockEntity.SeekerState.IDLE) {
            stopAndRemove(key);
            return;
        }

        SoundManager soundManager = minecraft.getSoundManager();
        Aim9xSeekerLoopSoundInstance current = ACTIVE.get(key);
        if (current == null || current.isStopped() || current.getExpectedState() != state) {
            stopAndRemove(key);
            Aim9xSeekerLoopSoundInstance replacement = new Aim9xSeekerLoopSoundInstance(launcher, state);
            ACTIVE.put(key, replacement);
            soundManager.play(replacement);
        }
    }

    public static void clear() {
        for (Aim9xSeekerLoopSoundInstance sound : ACTIVE.values()) {
            if (sound != null && !sound.isStopped()) {
                sound.stopSound();
            }
        }
        ACTIVE.clear();
    }

    private static void stopAndRemove(long key) {
        Aim9xSeekerLoopSoundInstance sound = ACTIVE.remove(key);
        if (sound != null && !sound.isStopped()) {
            sound.stopSound();
        }
    }
}
