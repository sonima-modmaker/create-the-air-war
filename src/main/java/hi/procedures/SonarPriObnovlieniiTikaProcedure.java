package hi.procedures;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

public class SonarPriObnovlieniiTikaProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if (world instanceof Level _level && !_level.isClientSide()) {
			_level.playSound(null, BlockPos.containing(x, y, z), net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:sonar_sound")), SoundSource.BLOCKS, 1, 1);
		} else if (world instanceof net.minecraft.world.level.Level _level) {
			_level.playLocalSound(x, y, z, net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:sonar_sound")), SoundSource.BLOCKS, 1, 1, false);
		}
	}
}
