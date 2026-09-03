
/*
 * MCreator note: This file will be REGENERATED on each build.
 */
package hi.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;

import hi.fluid.AmalgamBucketFluid;

import hi.CreateTheAirWarsMod;

public class CreateTheAirWarsModFluids {
	public static final DeferredRegister<Fluid> REGISTRY = DeferredRegister.create(net.minecraft.core.registries.Registries.FLUID, CreateTheAirWarsMod.MODID);
	public static final java.util.function.Supplier<net.minecraft.world.level.material.FlowingFluid> AMALGAM_BUCKET = REGISTRY.register("amalgam_bucket", () -> new AmalgamBucketFluid.Source());
	public static final java.util.function.Supplier<net.minecraft.world.level.material.FlowingFluid> FLOWING_AMALGAM_BUCKET = REGISTRY.register("flowing_amalgam_bucket", () -> new AmalgamBucketFluid.Flowing());

	@net.neoforged.fml.common.EventBusSubscriber(bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class FluidsClientSideHandler {
		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event) {
			event.enqueueWork(() -> {
			});
		}
	}
}
