package hi.events;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidUtil;

import net.minecraft.world.ItemInteractionResult; import net.minecraft.world.InteractionResult;import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import hi.init.CreateTheAirWarsModItems;

import com.simibubi.create.content.processing.basin.BasinBlock;

@net.neoforged.fml.common.EventBusSubscriber
public class AmalgamBasinFillHandler {
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Level level = event.getLevel();
		if (level.isClientSide())
			return;
		if (event.getItemStack().getItem() != CreateTheAirWarsModItems.AMALGAM_BUCKET_BUCKET.get())
			return;
		BlockState state = level.getBlockState(event.getPos());
		if (!(state.getBlock() instanceof BasinBlock))
			return;
		boolean success = FluidUtil.interactWithFluidHandler(event.getEntity(), event.getHand(), level, event.getPos(), event.getFace());
		if (success) {
			//event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}
}
