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
public class VihrThrusterSmokeParticle extends TextureSheetParticle {
    private static final int FULL_BRIGHT = 0xF000F0;

    public static VihrThrusterSmokeParticleProvider provider(SpriteSet spriteSet) {
        return new VihrThrusterSmokeParticleProvider(spriteSet);
    }

    public static class VihrThrusterSmokeParticleProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public VihrThrusterSmokeParticleProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new VihrThrusterSmokeParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }

    private final SpriteSet spriteSet;
    private final float startQuadSize;
    private final float endQuadSize;

    protected VihrThrusterSmokeParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
        super(world, x, y, z);
        this.spriteSet = spriteSet;
        this.setSize(0.14F, 0.14F);
        this.startQuadSize = this.quadSize * 2.7F;
        this.endQuadSize = this.quadSize * 6.0F;
        this.quadSize = this.startQuadSize;
        this.lifetime = 48 + this.random.nextInt(18);
        this.gravity = 0.0F;
        this.hasPhysics = true;
        this.friction = 0.95F;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return FULL_BRIGHT;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSpriteFromAge(this.spriteSet);
            float progress = (float) this.age / (float) this.lifetime;
            this.alpha = progress < 0.58F ? 0.46F : 0.46F * (1.0F - ((progress - 0.58F) / 0.42F));
            this.quadSize = this.startQuadSize + (this.endQuadSize - this.startQuadSize) * progress;
            this.xd *= 0.955D;
            this.yd *= 0.955D;
            this.zd *= 0.955D;
        }
    }
}
