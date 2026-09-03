package hi.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class MtsExplosionParticle extends TextureSheetParticle {
    public enum Mode {
        FLASH, HEAVY_FLASH, SMOKE, HEAVY_SMOKE, SPARK, FIRE, SHOCKWAVE, HEAVY_SHOCKWAVE, DEBRIS, BANG, PLUME
    }

    private final SpriteSet sprites;
    private final Mode mode;
    private final float initialSize;

    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites, Mode mode) {
        return (type, level, x, y, z, xSpeed, ySpeed, zSpeed) ->
            new MtsExplosionParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, mode);
    }

    private MtsExplosionParticle(ClientLevel level, double x, double y, double z,
                                 double xSpeed, double ySpeed, double zSpeed,
                                 SpriteSet sprites, Mode mode) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.mode = mode;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.friction = 0.94F;

        switch (mode) {
            case FLASH -> {
                this.lifetime = 6 + this.random.nextInt(3);
                this.quadSize = 4.5F + this.random.nextFloat() * 2.0F;
                this.xd = this.yd = this.zd = 0.0D;
            }
            case HEAVY_FLASH -> {
                this.lifetime = 9 + this.random.nextInt(4);
                this.quadSize = 14.0F + this.random.nextFloat() * 7.0F;
                this.xd = this.yd = this.zd = 0.0D;
            }
            case SMOKE -> {
                this.lifetime = 55 + this.random.nextInt(40);
                this.quadSize = 2.2F + this.random.nextFloat() * 2.4F;
                this.alpha = 0.82F;
                this.xd = xSpeed * 0.55D;
                this.yd = Math.abs(ySpeed) * 0.45D + 0.035D;
                this.zd = zSpeed * 0.55D;
                this.setPos(x + xSpeed * 1.8D, y + Math.abs(ySpeed), z + zSpeed * 1.8D);
            }
            case HEAVY_SMOKE -> {
                this.lifetime = 110 + this.random.nextInt(80);
                this.quadSize = 7.0F + this.random.nextFloat() * 7.0F;
                this.alpha = 0.78F;
                this.xd = xSpeed * 0.48D;
                this.yd = Math.abs(ySpeed) * 0.42D + 0.055D;
                this.zd = zSpeed * 0.48D;
                this.setPos(x + xSpeed * 3.5D, y + Math.abs(ySpeed) * 2.0D, z + zSpeed * 3.5D);
            }
            case SPARK -> {
                this.lifetime = 9 + this.random.nextInt(10);
                this.quadSize = 0.9F + this.random.nextFloat() * 1.8F;
                this.gravity = 0.12F;
                this.xd = xSpeed * 2.2D;
                this.yd = Math.abs(ySpeed) * 2.4D + 0.14D;
                this.zd = zSpeed * 2.2D;
            }
            case FIRE -> {
                this.lifetime = 15 + this.random.nextInt(16);
                this.quadSize = 1.7F + this.random.nextFloat() * 2.8F;
                this.alpha = 0.95F;
                this.xd = xSpeed * 0.5D;
                this.yd = Math.abs(ySpeed) * 0.55D + 0.045D;
                this.zd = zSpeed * 0.5D;
            }
            case SHOCKWAVE -> {
                this.lifetime = 11;
                this.quadSize = 5.5F;
                this.alpha = 0.72F;
                this.xd = this.yd = this.zd = 0.0D;
            }
            case HEAVY_SHOCKWAVE -> {
                this.lifetime = 16;
                this.quadSize = 18.0F;
                this.alpha = 0.82F;
                this.xd = this.yd = this.zd = 0.0D;
            }
            case DEBRIS -> {
                this.lifetime = 45 + this.random.nextInt(40);
                this.quadSize = 0.55F + this.random.nextFloat() * 1.25F;
                this.hasPhysics = true;
                this.gravity = 0.16F;
                this.xd = xSpeed * 1.45D;
                this.yd = Math.abs(ySpeed) * 1.8D + 0.12D;
                this.zd = zSpeed * 1.45D;
            }
            case BANG -> {
                // MTS bulletrocket/basicbomb bang layer: very fast radial shards
                // that collapse in size during the first few ticks.
                this.lifetime = 7 + this.random.nextInt(5);
                this.quadSize = 1.4F + this.random.nextFloat() * 2.6F;
                this.alpha = 1.0F;
                this.xd = xSpeed;
                this.yd = ySpeed;
                this.zd = zSpeed;
            }
            case PLUME -> {
                // Long-lived MTS alt/bangcluster column above the impact point.
                this.lifetime = 90 + this.random.nextInt(90);
                this.quadSize = 5.0F + this.random.nextFloat() * 6.0F;
                this.alpha = 0.9F;
                this.friction = 0.985F;
                this.xd = xSpeed;
                this.yd = ySpeed;
                this.zd = zSpeed;
            }
        }

        this.initialSize = this.quadSize;
        if (mode == Mode.FIRE) {
            this.setSpriteFromAge(sprites);
        } else {
            this.pickSprite(sprites);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return switch (this.mode) {
            case FLASH, HEAVY_FLASH, SPARK, FIRE, SHOCKWAVE, HEAVY_SHOCKWAVE, BANG, PLUME -> 15728880;
            default -> super.getLightColor(partialTick);
        };
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }
        float progress = (float) this.age / (float) this.lifetime;
        switch (this.mode) {
            case FLASH, HEAVY_FLASH -> {
                this.quadSize = this.initialSize * (1.0F + progress * 1.45F);
                this.alpha = Math.max(0.0F, 1.0F - progress * progress);
            }
            case SMOKE, HEAVY_SMOKE -> {
                this.quadSize = this.initialSize * (1.0F + progress * 1.1F);
                this.alpha = Math.max(0.0F, 0.82F * (1.0F - progress));
                this.yd += 0.0008D;
            }
            case SPARK -> {
                this.quadSize = this.initialSize * (1.0F - progress * 0.65F);
                this.alpha = Math.max(0.0F, 1.0F - progress);
            }
            case FIRE -> {
                this.setSpriteFromAge(this.sprites);
                this.quadSize = this.initialSize * (1.0F + progress * 0.45F);
                this.alpha = Math.max(0.0F, 0.95F * (1.0F - progress));
            }
            case SHOCKWAVE, HEAVY_SHOCKWAVE -> {
                this.quadSize = this.initialSize * (1.0F + progress * 2.8F);
                this.alpha = Math.max(0.0F, 0.82F * (1.0F - progress));
            }
            case DEBRIS -> this.alpha = Math.max(0.0F, 1.0F - progress * progress);
            case BANG -> {
                this.quadSize = Math.max(0.002F, this.initialSize * (1.0F - progress));
                this.alpha = Math.max(0.0F, 1.0F - progress);
                this.yd -= 0.008D;
            }
            case PLUME -> {
                this.quadSize = this.initialSize * (1.0F + progress * 0.35F);
                this.alpha = Math.max(0.0F, 0.9F * (1.0F - progress));
                this.yd -= 0.0004D;
            }
        }
    }
}
