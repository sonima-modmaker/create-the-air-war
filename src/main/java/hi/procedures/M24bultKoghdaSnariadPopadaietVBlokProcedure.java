package hi.procedures;

import hi.init.CreateTheAirWarsModParticleTypes;
import hi.util.ExplosionUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

public class M24bultKoghdaSnariadPopadaietVBlokProcedure {
    private static final ExplosionUtils.ProjectileExplosionProfile DEFAULT_PROFILE = new ExplosionUtils.ProjectileExplosionProfile(
        4f, false, 1.2, "create_the_air_wars:shellex3", SoundSource.NEUTRAL, 1f, 1f,
        CreateTheAirWarsModParticleTypes.EXLOSION.get(), 4, 0.8, 0.8, 0.8, 0.25, 0, false
    );

    public static void execute(LevelAccessor world, double x, double y, double z) {
        execute(world, x, y, z, null);
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity projectile) {
        ExplosionUtils.explodeProjectileImpact(world, x, y, z, projectile, DEFAULT_PROFILE);
    }
}
