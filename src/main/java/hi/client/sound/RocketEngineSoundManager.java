package hi.client.sound;

import hi.block.entity.RocketEngineBlockEntity;
import hi.init.CreateTheAirWarsModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RocketEngineSoundManager {
    private static final int PRUNE_INTERVAL_TICKS = 20;
    private static final Map<BlockPos, RocketEngineSoundInstance> idleSounds = new HashMap<>();
    private static final Map<BlockPos, RocketEngineSoundInstance> throttleSounds = new HashMap<>();
    private static long lastPruneTick = Long.MIN_VALUE;

    public static void updateEngineSound(RocketEngineBlockEntity engine) {
        BlockPos pos = engine.getBlockPos();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() == null) return;
        Level level = engine.getLevel();
        if (mc.player == null || level == null || mc.level != level) {
            clear();
            return;
        }

        RocketEngineBlockEntity.EngineState state = engine.getEngineState();
        boolean needsIdleSound = state != RocketEngineBlockEntity.EngineState.OFF;
        boolean needsThrottleSound = engine.isThrottling();

        RocketEngineSoundInstance idleSound = idleSounds.get(pos);
        RocketEngineSoundInstance throttleSound = throttleSounds.get(pos);
        if (!needsIdleSound && !needsThrottleSound && idleSound == null && throttleSound == null) {
            pruneStopped(level.getGameTime());
            return;
        }

        if (needsIdleSound) {
            if (idleSound == null || idleSound.isStopped()) {
                idleSound = new RocketEngineSoundInstance(engine, CreateTheAirWarsModSounds.ROCKET_ENGINE_IDLE.get(), false);
                idleSounds.put(pos, idleSound);
                mc.getSoundManager().play(idleSound);
            }
        } else if (idleSound != null) {
            idleSound.stopSound();
            idleSounds.remove(pos);
        }

        if (needsThrottleSound) {
            if (throttleSound == null || throttleSound.isStopped()) {
                throttleSound = new RocketEngineSoundInstance(engine, CreateTheAirWarsModSounds.ROCKET_ENGINE_THROTTLE.get(), true);
                throttleSounds.put(pos, throttleSound);
                mc.getSoundManager().play(throttleSound);
            }
        } else if (throttleSound != null) {
            throttleSound.stopSound();
            throttleSounds.remove(pos);
        }

        pruneStopped(level.getGameTime());
    }

    public static void removeEngine(BlockPos pos) {
        RocketEngineSoundInstance idle = idleSounds.remove(pos);
        if (idle != null) idle.stopSound();
        
        RocketEngineSoundInstance throttle = throttleSounds.remove(pos);
        if (throttle != null) throttle.stopSound();
    }

    public static void clear() {
        idleSounds.values().forEach(RocketEngineSoundInstance::stopSound);
        idleSounds.clear();
        throttleSounds.values().forEach(RocketEngineSoundInstance::stopSound);
        throttleSounds.clear();
    }

    private static void pruneStopped(long gameTime) {
        if (gameTime - lastPruneTick < PRUNE_INTERVAL_TICKS) {
            return;
        }
        lastPruneTick = gameTime;

        Iterator<Map.Entry<BlockPos, RocketEngineSoundInstance>> idleIterator = idleSounds.entrySet().iterator();
        while (idleIterator.hasNext()) {
            RocketEngineSoundInstance sound = idleIterator.next().getValue();
            if (sound == null || sound.isStopped()) {
                idleIterator.remove();
            }
        }
        Iterator<Map.Entry<BlockPos, RocketEngineSoundInstance>> throttleIterator = throttleSounds.entrySet().iterator();
        while (throttleIterator.hasNext()) {
            RocketEngineSoundInstance sound = throttleIterator.next().getValue();
            if (sound == null || sound.isStopped()) {
                throttleIterator.remove();
            }
        }
    }
}
