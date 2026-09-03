
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package hi.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;

import hi.CreateTheAirWarsMod;

public class CreateTheAirWarsModParticleTypes {
	public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(net.minecraft.core.registries.Registries.PARTICLE_TYPE, CreateTheAirWarsMod.MODID);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> QWE = REGISTRY.register("qwe", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> SDF = REGISTRY.register("sdf", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> EXLOSION = REGISTRY.register("exlosion", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> EXP = REGISTRY.register("exp", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> MACHINEGUN_SMOKE = REGISTRY.register("machinegun_smoke", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> HEAT_TRAP_SMOKE = REGISTRY.register("heat_trap_smoke", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> VIHR_THRUSTER_SMOKE = REGISTRY.register("vihr_thruster_smoke", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> MTS_EXPLOSION_FLASH = REGISTRY.register("mts_explosion_flash", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> MTS_HEAVY_EXPLOSION_FLASH = REGISTRY.register("mts_heavy_explosion_flash", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> MTS_EXPLOSION_SMOKE = REGISTRY.register("mts_explosion_smoke", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> MTS_HEAVY_EXPLOSION_SMOKE = REGISTRY.register("mts_heavy_explosion_smoke", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> MTS_EXPLOSION_SPARK = REGISTRY.register("mts_explosion_spark", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> MTS_EXPLOSION_FIRE = REGISTRY.register("mts_explosion_fire", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> MTS_EXPLOSION_SHOCKWAVE = REGISTRY.register("mts_explosion_shockwave", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> MTS_HEAVY_EXPLOSION_SHOCKWAVE = REGISTRY.register("mts_heavy_explosion_shockwave", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> MTS_EXPLOSION_DEBRIS = REGISTRY.register("mts_explosion_debris", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> MTS_EXPLOSION_BANG = REGISTRY.register("mts_explosion_bang", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> MTS_EXPLOSION_PLUME = REGISTRY.register("mts_explosion_plume", CreateTheAirWarsModParticleTypes::createParticleType);
	public static final java.util.function.Supplier<net.minecraft.core.particles.SimpleParticleType> MTS_CONFIGURED = REGISTRY.register("mts_configured", CreateTheAirWarsModParticleTypes::createParticleType);

	private static SimpleParticleType createParticleType() {
		return new SimpleParticleType(true) {
		};
	}
}
