package hi.procedures;

import hi.init.CreateTheAirWarsModParticleTypes;
import hi.util.ExplosionUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

public class GvrdcrcdKoghdaSnariadPopadaietVBlokProcedure {
    private static final ExplosionUtils.ProjectileExplosionProfile DEFAULT_PROFILE = new ExplosionUtils.ProjectileExplosionProfile(
        15f, true, 3.0, "create_the_air_wars:fire_big_cannon", SoundSource.BLOCKS, 15f, 1f,
        CreateTheAirWarsModParticleTypes.EXLOSION.get(), 10, 2.5, 2.5, 2.5, 0.7, 7, false
    );

    public static void execute(LevelAccessor world, double x, double y, double z) {
        execute(world, x, y, z, null);
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity projectile) {
        ExplosionUtils.explodeProjectileImpact(world, x, y, z, projectile, DEFAULT_PROFILE);
    }
}
