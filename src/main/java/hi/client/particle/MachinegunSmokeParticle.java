package hi.client.particle;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.multiplayer.ClientLevel;

@OnlyIn(Dist.CLIENT)
public class MachinegunSmokeParticle extends TextureSheetParticle {
	public static MachinegunSmokeParticleProvider provider(SpriteSet spriteSet) {
		return new MachinegunSmokeParticleProvider(spriteSet);
	}

	public static class MachinegunSmokeParticleProvider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;

		public MachinegunSmokeParticleProvider(SpriteSet spriteSet) {
			this.spriteSet = spriteSet;
		}

		public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new MachinegunSmokeParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
		}
	}

	private final SpriteSet spriteSet;
	private final float baseSize;

	protected MachinegunSmokeParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
		super(world, x, y, z);
		this.spriteSet = spriteSet;
		this.setSize(0.9f, 0.9f);
		float velocityLen = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
		float sizeMultiplier = 1f + Math.min(4f, velocityLen);
		this.quadSize *= 2.2f * sizeMultiplier;
		this.baseSize = this.quadSize;
		this.lifetime = 64;
		this.gravity = 0f;
		this.hasPhysics = false;
		this.xd = vx * 0.01;
		this.yd = 0.01 + vy * 0.01;
		this.zd = vz * 0.01;
		this.setSpriteFromAge(spriteSet);
		this.alpha = 0.9f;
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
			int frame = Math.min(3, (int) (progress * 4f));
			this.setSprite(this.spriteSet.get(frame, 4));
			this.alpha = Math.max(0f, 0.9f - progress * 0.9f);
			this.quadSize = Math.max(0.2f, this.baseSize * (1f + progress * 0.7f));
			this.yd += 0.0005;
		}
	}
}
