
package hi.block;

import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.LiquidBlock;

import hi.init.CreateTheAirWarsModFluids;

public class AmalgamBucketBlock extends LiquidBlock {
	public AmalgamBucketBlock() {
		super(CreateTheAirWarsModFluids.AMALGAM_BUCKET.get(), BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).strength(100f).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable());
	}
}

