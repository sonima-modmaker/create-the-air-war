
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package hi.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.world.effect.MobEffect;

import hi.potion.LocationMobEffect;

import hi.CreateTheAirWarsMod;

public class CreateTheAirWarsModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(net.minecraft.core.registries.Registries.MOB_EFFECT, CreateTheAirWarsMod.MODID);
	public static final DeferredHolder<net.minecraft.world.effect.MobEffect, net.minecraft.world.effect.MobEffect> LOCATION = REGISTRY.register("location", () -> new LocationMobEffect());
}
