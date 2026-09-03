package hi.procedures;

import hi.init.CreateTheAirWarsModParticleTypes;
import hi.util.ExplosionUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

public class CsdsKoghdaSnariadPopadaietVBlokProcedure {
    private static final ExplosionUtils.ProjectileExplosionProfile DEFAULT_PROFILE = new ExplosionUtils.ProjectileExplosionProfile(
        3f, true, 1.0, "create_the_air_wars:vzrivc-8", SoundSource.NEUTRAL, 2f, 1f,
        CreateTheAirWarsModParticleTypes.EXLOSION.get(), 4, 1.2, 1.2, 1.2, 0.35, 3, false
    );

    public static void execute(LevelAccessor world, double x, double y, double z) {
        execute(world, x, y, z, null);
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity projectile) {
        ExplosionUtils.explodeProjectileImpact(world, x, y, z, projectile, DEFAULT_PROFILE);
    }
}
