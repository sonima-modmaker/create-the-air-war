package hi.procedures;

import hi.init.CreateTheAirWarsModParticleTypes;
import hi.util.ExplosionUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

public class AIM9ROCKETTRUEKoghdaSnariadPopadaietVBlokProcedure {
    private static final ExplosionUtils.ProjectileExplosionProfile DEFAULT_PROFILE = new ExplosionUtils.ProjectileExplosionProfile(
        10f, true, 2.5, "create_the_air_wars:shellex3", SoundSource.NEUTRAL, 5f, 1f,
        CreateTheAirWarsModParticleTypes.EXLOSION.get(), 8, 2.0, 2.0, 2.0, 0.6, 6, false
    );

    public static void execute(LevelAccessor world, double x, double y, double z) {
        execute(world, x, y, z, null);
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity projectile) {
        ExplosionUtils.explodeProjectileImpact(world, x, y, z, projectile, DEFAULT_PROFILE);
    }
}
