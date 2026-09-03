
package hi.fluid;

import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.LiquidBlock;

import hi.init.CreateTheAirWarsModItems;
import hi.init.CreateTheAirWarsModFluids;
import hi.init.CreateTheAirWarsModFluidTypes;
import hi.init.CreateTheAirWarsModBlocks;

public abstract class AmalgamBucketFluid extends BaseFlowingFluid {
	public static final BaseFlowingFluid.Properties PROPERTIES = new BaseFlowingFluid.Properties(() -> CreateTheAirWarsModFluidTypes.AMALGAM_BUCKET_TYPE.get(), () -> CreateTheAirWarsModFluids.AMALGAM_BUCKET.get(),
			() -> CreateTheAirWarsModFluids.FLOWING_AMALGAM_BUCKET.get()).explosionResistance(100f).tickRate(2).levelDecreasePerBlock(2).bucket(() -> CreateTheAirWarsModItems.AMALGAM_BUCKET_BUCKET.get())
			.block(() -> (LiquidBlock) CreateTheAirWarsModBlocks.AMALGAM_BUCKET.get());

	private AmalgamBucketFluid() {
		super(PROPERTIES);
	}

	public static class Source extends AmalgamBucketFluid {
		public int getAmount(FluidState state) {
			return 8;
		}

		public boolean isSource(FluidState state) {
			return true;
		}
	}

	public static class Flowing extends AmalgamBucketFluid {
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		public int getAmount(FluidState state) {
			return state.getValue(LEVEL);
		}

		public boolean isSource(FluidState state) {
			return false;
		}
	}
}
