
/*
 * MCreator note: This file will be REGENERATED on each build.
 */
package hi.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.fluids.FluidType;

import hi.fluid.types.AmalgamBucketFluidType;

import hi.CreateTheAirWarsMod;

public class CreateTheAirWarsModFluidTypes {
	public static final DeferredRegister<FluidType> REGISTRY = DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_TYPES, CreateTheAirWarsMod.MODID);
	public static final java.util.function.Supplier<net.neoforged.neoforge.fluids.FluidType> AMALGAM_BUCKET_TYPE = REGISTRY.register("amalgam_bucket", () -> new AmalgamBucketFluidType());
}
