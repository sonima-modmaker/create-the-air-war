package hi.events;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidUtil;

import net.minecraft.world.ItemInteractionResult; import net.minecraft.world.InteractionResult;import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

import hi.init.CreateTheAirWarsModItems;

@net.neoforged.fml.common.EventBusSubscriber
public class CreateFluidTankFillHandler {
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Level level = event.getLevel();
		if (level.isClientSide())
			return;
		ItemStack held = event.getItemStack();
		if (held.isEmpty() || held.getCount() != 1)
			return;
		if (held.getItem() != CreateTheAirWarsModItems.AMALGAM_BUCKET_BUCKET.get())
			return;
		Player player = event.getEntity();
		InteractionHand hand = event.getHand();
		BlockPos pos = event.getPos();
		Direction face = event.getFace();
		boolean success = FluidUtil.interactWithFluidHandler(player, hand, level, pos, face);
		if (!success) {
			success = FluidUtil.interactWithFluidHandler(player, hand, level, pos, null);
		}
		if (success) {
			//event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}
}
