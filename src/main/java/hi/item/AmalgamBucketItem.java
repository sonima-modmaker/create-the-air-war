
package hi.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.ItemInteractionResult; import net.minecraft.world.InteractionResult;import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.fluids.FluidUtil;

import hi.init.CreateTheAirWarsModFluids;

public class AmalgamBucketItem extends BucketItem {
	public AmalgamBucketItem() {
		super(CreateTheAirWarsModFluids.AMALGAM_BUCKET.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).rarity(Rarity.COMMON));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		boolean success = FluidUtil.interactWithFluidHandler(context.getPlayer(), context.getHand(), context.getLevel(), context.getClickedPos(), context.getClickedFace());
		if (!success) {
			success = FluidUtil.interactWithFluidHandler(context.getPlayer(), context.getHand(), context.getLevel(), context.getClickedPos(), null);
		}
		if (success) {
			return InteractionResult.SUCCESS;
		}
		return super.useOn(context);
	}
}
