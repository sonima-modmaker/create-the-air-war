package hi.procedures;

import net.neoforged.neoforge.items.IItemHandlerModifiable;

import net.minecraft.world.item.ItemStack;

import hi.init.CreateTheAirWarsModBlocks;

public class Ozm72debugProcedure {
	public static void execute(net.minecraft.world.level.LevelAccessor world, double x, double y, double z, ItemStack itemstack) {
		if (world instanceof net.minecraft.world.level.Level _level) {
			ItemStack _isc = itemstack;
			final ItemStack _setstack = new ItemStack(CreateTheAirWarsModBlocks.GDFFGDGDG.get()).copy();
			final int _sltid = 1;
			_setstack.setCount(1);
			if (_isc != null) {
				/* net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK.getCapability(world, BlockPos.containing(x, y, z), null).setStackInSlot(_sltid, _setstack); */
			}
		}
	}
}
