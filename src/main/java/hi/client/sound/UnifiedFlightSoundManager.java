package hi.client.sound;

import hi.entity.*;
import hi.init.CreateTheAirWarsModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class UnifiedFlightSoundManager {
    private static final double SEARCH_RADIUS = 256.0;
    private static final Map<Integer, UnifiedFlightSoundInstance> ACTIVE = new HashMap<>();
    private static final Set<Integer> VISIBLE_PROJECTILES = new HashSet<>();

    private UnifiedFlightSoundManager() {
    }

    public static void clientTick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null) {
            clear();
            return;
        }

        SoundManager soundManager = minecraft.getSoundManager();
        VISIBLE_PROJECTILES.clear();
        AABB searchBox = minecraft.player.getBoundingBox().inflate(SEARCH_RADIUS);

        for (Entity entity : level.getEntitiesOfClass(Entity.class, searchBox, entity -> entity.isAlive() && isTargetProjectile(entity))) {
            VISIBLE_PROJECTILES.add(entity.getId());

            if (!isMovementActive(entity)) {
                continue;
            }
            UnifiedFlightSoundInstance current = ACTIVE.get(entity.getId());
            if (current == null || current.isStopped()) {
                SoundEvent soundEvent = getProjectileSound(entity);
                UnifiedFlightSoundInstance sound = new UnifiedFlightSoundInstance(entity, soundEvent);
                ACTIVE.put(entity.getId(), sound);
                soundManager.play(sound);
            }
        }

        Iterator<Map.Entry<Integer, UnifiedFlightSoundInstance>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, UnifiedFlightSoundInstance> entry = iterator.next();
            UnifiedFlightSoundInstance sound = entry.getValue();
            if (sound == null || sound.isStopped() || !VISIBLE_PROJECTILES.contains(entry.getKey())) {
                if (sound != null && !sound.isStopped()) {
                    sound.stopSound();
                }
                iterator.remove();
            }
        }
    }

    private static boolean isTargetProjectile(Entity entity) {
        if (entity instanceof M24bultEntity || entity instanceof MachinegunShellEntity) {
            return false;
        }

        return entity instanceof Aim9xbultEntity
            || entity instanceof TomahawkbultEntity
            || entity instanceof VihrRocketEntity
            || entity instanceof X25mlEntity
            || entity instanceof C75RocketEntity
            || entity instanceof C25Entity
            || entity instanceof C25actvbultEntity
            || entity instanceof C3ktrueEntity
            || entity instanceof Fab3000trueEntity
            || entity instanceof Ninek119mactvbultEntity
            || entity instanceof Rim7actvbultEntity
            || entity instanceof AstrkEntity
            || entity instanceof BgghEntity;
    }

    private static boolean isMovementActive(Entity entity) {
        if (entity instanceof Aim9xbultEntity) {
            return ((Aim9xbultEntity) entity).isEngineActive();
        }
        if (entity instanceof TomahawkbultEntity) {
            return ((TomahawkbultEntity) entity).isEngineActive();
        }
        return entity.getDeltaMovement().lengthSqr() >= 0.0625;
    }

    private static SoundEvent getProjectileSound(Entity entity) {
        if (entity instanceof TomahawkbultEntity) {
            return CreateTheAirWarsModSounds.TOMAHAWK_FLIGHT.get();
        }
        if (entity instanceof Fab3000trueEntity) {
            return CreateTheAirWarsModSounds.SHELL_FLY.get();
        }
        return CreateTheAirWarsModSounds.ROCKET_FLY.get();
    }

    public static void clear() {
        for (UnifiedFlightSoundInstance sound : ACTIVE.values()) {
            if (sound != null && !sound.isStopped()) {
                sound.stopSound();
            }
        }
        ACTIVE.clear();
    }
}
