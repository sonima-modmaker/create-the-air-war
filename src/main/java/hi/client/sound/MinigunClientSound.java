package hi.client.sound;

import hi.block.entity.MinigunBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class MinigunClientSound {
    private static final double MAX_SOUND_DISTANCE_SQR = 96.0D * 96.0D;
    private static final int PRUNE_INTERVAL_TICKS = 20;
    private static final Map<Long, MinigunLoopSound> ACTIVE = new HashMap<>();
    private static long lastPruneTick = Long.MIN_VALUE;

    public static void tick(MinigunBlockEntity blockEntity) {
        if (blockEntity == null || blockEntity.getLevel() == null) return;
        Level level = blockEntity.getLevel();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level != level) {
            clear();
            return;
        }

        BlockPos pos = blockEntity.getBlockPos();
        SoundManager soundManager = minecraft.getSoundManager();
        long key = pos.asLong();
        MinigunLoopSound current = ACTIVE.get(key);
        if (minecraft.player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) > MAX_SOUND_DISTANCE_SQR) {
            if (current != null) {
                soundManager.stop(current);
                ACTIVE.remove(key);
            }
            pruneStopped(soundManager, level.getGameTime());
            return;
        }

        boolean audible = blockEntity.isPowered() || blockEntity.getBarrelSpeed() > 0.02F;
        if (audible) {
            if (current == null || current.isStopped()) {
                MinigunLoopSound sound = new MinigunLoopSound(blockEntity);
                ACTIVE.put(key, sound);
                soundManager.play(sound);
            }
        } else if (current != null) {
            soundManager.stop(current);
            ACTIVE.remove(key);
        }
        pruneStopped(soundManager, level.getGameTime());
    }

    private static void pruneStopped(SoundManager soundManager, long gameTime) {
        if (gameTime - lastPruneTick < PRUNE_INTERVAL_TICKS) {
            return;
        }
        lastPruneTick = gameTime;

        var iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            MinigunLoopSound sound = iterator.next().getValue();
            if (sound == null || sound.isStopped()) {
                if (sound != null) {
                    soundManager.stop(sound);
                }
                iterator.remove();
            }
        }
    }

    private static void clear() {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        ACTIVE.values().forEach(soundManager::stop);
        ACTIVE.clear();
    }
}
