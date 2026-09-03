package hi.procedures;

import hi.init.CreateTheAirWarsModParticleTypes;
import hi.util.ExplosionUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

public class Fab3000trueKoghdaSnariadPopadaietVBlokProcedure {
    private static final ExplosionUtils.ProjectileExplosionProfile DEFAULT_PROFILE = new ExplosionUtils.ProjectileExplosionProfile(
        34f, true, 4.5, "create_the_air_wars:shellexp2", SoundSource.NEUTRAL, 25f, 1f,
        CreateTheAirWarsModParticleTypes.EXP.get(), 3, 3.0, 3.0, 3.0, 1.0, 45, true
    );

    public static void execute(LevelAccessor world, double x, double y, double z) {
        execute(world, x, y, z, null);
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity projectile) {
        ExplosionUtils.explodeProjectileImpact(world, x, y, z, projectile, DEFAULT_PROFILE);
    }
}
