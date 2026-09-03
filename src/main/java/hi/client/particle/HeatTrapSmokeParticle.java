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
public class HeatTrapSmokeParticle extends TextureSheetParticle {
    public static HeatTrapSmokeParticleProvider provider(SpriteSet spriteSet) {
        return new HeatTrapSmokeParticleProvider(spriteSet);
    }

    public static class HeatTrapSmokeParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public HeatTrapSmokeParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new HeatTrapSmokeParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }

    private final SpriteSet spriteSet;
    private final float baseSize;

    protected HeatTrapSmokeParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(world, x, y, z);
        this.spriteSet = spriteSet;
        this.setSize(0.35f, 0.35f);
        this.quadSize *= 0.85f;
        this.baseSize = this.quadSize;
        this.lifetime = 42 + this.random.nextInt(20);
        this.gravity = 0f;
        this.hasPhysics = false;
        this.xd = vx * 0.12;
        this.yd = 0.008 + vy * 0.08;
        this.zd = vz * 0.12;
        this.alpha = 0.42f;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            float progress = (float) this.age / (float) this.lifetime;
            this.setSpriteFromAge(this.spriteSet);
            this.alpha = Math.max(0f, 0.42f - progress * 0.4f);
            this.quadSize = Math.max(0.08f, this.baseSize * (1.0f + progress * 0.55f));
            this.yd += 0.00035;
        }
    }
}
