
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package hi.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.api.distmarker.Dist;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

import hi.client.particle.SdfParticle;
import hi.client.particle.QweParticle;
import hi.client.particle.ExpParticle;
import hi.client.particle.ExlosionParticle;
import hi.client.particle.HeatTrapSmokeParticle;
import hi.client.particle.MachinegunSmokeParticle;
import hi.client.particle.VihrThrusterSmokeParticle;
import hi.client.particle.MtsExplosionParticle;

import java.lang.reflect.Proxy;
import java.lang.reflect.Method;

@net.neoforged.fml.common.EventBusSubscriber(bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CreateTheAirWarsModParticles {
	@SubscribeEvent
	public static void registerParticles(RegisterParticleProvidersEvent event) {
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.SDF.get(), SdfParticle::provider);
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.QWE.get(), QweParticle::provider);
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.EXP.get(), ExpParticle::provider);
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.EXLOSION.get(), ExlosionParticle::provider);
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.MACHINEGUN_SMOKE.get(), MachinegunSmokeParticle::provider);
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.HEAT_TRAP_SMOKE.get(), HeatTrapSmokeParticle::provider);
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.VIHR_THRUSTER_SMOKE.get(), VihrThrusterSmokeParticle::provider);
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.MTS_EXPLOSION_FLASH.get(), sprites -> MtsExplosionParticle.provider(sprites, MtsExplosionParticle.Mode.FLASH));
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.MTS_HEAVY_EXPLOSION_FLASH.get(), sprites -> MtsExplosionParticle.provider(sprites, MtsExplosionParticle.Mode.HEAVY_FLASH));
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.MTS_EXPLOSION_SMOKE.get(), sprites -> MtsExplosionParticle.provider(sprites, MtsExplosionParticle.Mode.SMOKE));
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.MTS_HEAVY_EXPLOSION_SMOKE.get(), sprites -> MtsExplosionParticle.provider(sprites, MtsExplosionParticle.Mode.HEAVY_SMOKE));
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.MTS_EXPLOSION_SPARK.get(), sprites -> MtsExplosionParticle.provider(sprites, MtsExplosionParticle.Mode.SPARK));
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.MTS_EXPLOSION_FIRE.get(), sprites -> MtsExplosionParticle.provider(sprites, MtsExplosionParticle.Mode.FIRE));
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.MTS_EXPLOSION_SHOCKWAVE.get(), sprites -> MtsExplosionParticle.provider(sprites, MtsExplosionParticle.Mode.SHOCKWAVE));
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.MTS_HEAVY_EXPLOSION_SHOCKWAVE.get(), sprites -> MtsExplosionParticle.provider(sprites, MtsExplosionParticle.Mode.HEAVY_SHOCKWAVE));
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.MTS_EXPLOSION_DEBRIS.get(), sprites -> MtsExplosionParticle.provider(sprites, MtsExplosionParticle.Mode.DEBRIS));
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.MTS_EXPLOSION_BANG.get(), sprites -> MtsExplosionParticle.provider(sprites, MtsExplosionParticle.Mode.BANG));
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.MTS_EXPLOSION_PLUME.get(), sprites -> MtsExplosionParticle.provider(sprites, MtsExplosionParticle.Mode.PLUME));
		registerSpriteSet(event, CreateTheAirWarsModParticleTypes.MTS_CONFIGURED.get(), sprites -> MtsExplosionParticle.provider(sprites, MtsExplosionParticle.Mode.FLASH));
	}

	private static void registerSpriteSet(RegisterParticleProvidersEvent event, SimpleParticleType type, SpriteParticleFactory factory) {
		try {
			Class<?> registrationClass = Class.forName("net.minecraft.client.particle.ParticleEngine$SpriteParticleRegistration");
			Method method = RegisterParticleProvidersEvent.class.getMethod("registerSpriteSet", net.minecraft.core.particles.ParticleType.class, registrationClass);
			Object proxy = Proxy.newProxyInstance(
				CreateTheAirWarsModParticles.class.getClassLoader(),
				new Class<?>[] { registrationClass },
				(proxyInstance, invokedMethod, args) -> {
					if ("create".equals(invokedMethod.getName()) && args != null && args.length == 1 && args[0] instanceof SpriteSet spriteSet) {
						return factory.create(spriteSet);
					}
					return null;
				}
			);
			method.invoke(event, type, proxy);
		} catch (ReflectiveOperationException ignored) {
		}
	}

	@FunctionalInterface
	private interface SpriteParticleFactory {
		ParticleProvider<SimpleParticleType> create(SpriteSet spriteSet);
	}
}
