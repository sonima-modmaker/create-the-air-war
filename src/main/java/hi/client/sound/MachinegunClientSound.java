package hi.client.sound;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class MachinegunClientSound {
	private static final double MAX_SOUND_DISTANCE_SQR = 96.0 * 96.0;
	private static final int PRUNE_INTERVAL_TICKS = 20;
	private static final Map<Long, MachinegunLoopSound> ACTIVE = new HashMap<>();
	private static long lastPruneTick = Long.MIN_VALUE;

	public static void tick(hi.block.entity.MachinegunBlockEntity blockEntity) {
		if (blockEntity == null || blockEntity.getLevel() == null) return;
		Level level = blockEntity.getLevel();
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.level != level) {
			clear();
			return;
		}
		BlockPos pos = blockEntity.getBlockPos();
		boolean powered = blockEntity.isWasPowered();
		SoundManager soundManager = minecraft.getSoundManager();
		long key = pos.asLong();
		MachinegunLoopSound current = ACTIVE.get(key);
		if (minecraft.player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) > MAX_SOUND_DISTANCE_SQR) {
			if (current != null) {
				soundManager.stop(current);
				ACTIVE.remove(key);
			}
			pruneStopped(soundManager, level.getGameTime());
			return;
		}
		if (powered) {
			if (current == null || current.isStopped()) {
				MachinegunLoopSound sound = new MachinegunLoopSound(blockEntity);
				ACTIVE.put(key, sound);
				soundManager.play(sound);
			}
		} else {
			if (current != null) {
				soundManager.stop(current);
				ACTIVE.remove(key);
			}
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
			MachinegunLoopSound sound = iterator.next().getValue();
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
