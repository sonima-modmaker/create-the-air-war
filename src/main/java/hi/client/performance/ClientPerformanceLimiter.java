package hi.client.performance;

import hi.client.particle.VihrThrusterSmokeParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.Entity;

public final class ClientPerformanceLimiter {
    private static final double SHADOW_DISABLE_DISTANCE_SQR = 48.0 * 48.0;
    private static final double PARTICLE_NEAR_DISTANCE_SQR = 18.0 * 18.0;
    private static final double PARTICLE_MID_DISTANCE_SQR = 36.0 * 36.0;
    private static final double PARTICLE_FAR_DISTANCE_SQR = 64.0 * 64.0;

    private ClientPerformanceLimiter() {
    }

    public static boolean shouldCullParticle(Minecraft minecraft, Particle particle) {
        if (minecraft == null || minecraft.player == null || particle == null) {
            return false;
        }

        if (particle instanceof VihrThrusterSmokeParticle) {
            return false;
        }

        ParticleStatus status = minecraft.options.particles().get();
        int fps = Math.max(1, minecraft.getFps());
        if (status == ParticleStatus.ALL && fps >= 75) {
            return false;
        }

        boolean grouped = particle.getParticleGroup().isPresent();

        if (status == ParticleStatus.MINIMAL) {
            return true;
        }

        if (fps < 20) {
            if (!grouped) {
                return true;
            }
            if (minecraft.level != null) {
                return minecraft.level != null && minecraft.level.random.nextFloat() < 0.8f;
            }
        }

        if (fps < 35) {
            if (!grouped) {
                return minecraft.level != null && minecraft.level.random.nextFloat() < 0.65f;
            }
            if (minecraft.level != null) {
                return minecraft.level != null && minecraft.level.random.nextFloat() < 0.85f;
            }
        }

        if (fps < 50 && !grouped) {
            return minecraft.level != null && minecraft.level.random.nextFloat() < 0.5f;
        }

        return false;
    }

    public static boolean shouldDisableEntityShadows(Minecraft minecraft, Entity entity) {
        if (minecraft == null || minecraft.player == null || entity == null) {
            return false;
        }

        int fps = Math.max(1, minecraft.getFps());
        if (fps >= 45) {
            return false;
        }

        return minecraft.player.distanceToSqr(entity) > SHADOW_DISABLE_DISTANCE_SQR;
    }
}
