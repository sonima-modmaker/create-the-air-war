
package hi.fluid.types;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class AmalgamBucketFluidType extends FluidType {
	public AmalgamBucketFluidType() {
		super(FluidType.Properties.create().canSwim(false).canDrown(false).pathType(net.minecraft.world.level.pathfinder.PathType.LAVA).adjacentPathType(null).motionScale(0.014D).sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
				.sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY).sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH));
	}

	@Override
	public void initializeClient(java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
		consumer.accept(new net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions() {
			private static final ResourceLocation STILL_TEXTURE = net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:block/error");
			private static final ResourceLocation FLOWING_TEXTURE = net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:block/error");

			@Override
			public ResourceLocation getStillTexture() {
				return STILL_TEXTURE;
			}

			@Override
			public ResourceLocation getFlowingTexture() {
				return FLOWING_TEXTURE;
			}
		});
	}
}
