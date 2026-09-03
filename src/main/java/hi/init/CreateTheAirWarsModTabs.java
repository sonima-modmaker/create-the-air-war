
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package hi.init;

import net.minecraft.Util;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import hi.creative.CreativeTabContentManager;

import hi.CreateTheAirWarsMod;

public class CreateTheAirWarsModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateTheAirWarsMod.MODID);
	public static final DeferredHolder<net.minecraft.world.item.CreativeModeTab, net.minecraft.world.item.CreativeModeTab> CREATETHEAIRWAR = REGISTRY.register("createtheairwar",
			() -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0).title(Component.translatable("item_group.create_the_air_wars.createtheairwar")).icon(CreateTheAirWarsModTabs::cyclingIcon).displayItems((parameters, tabData) -> {
				CreativeTabContentManager.reloadFromDisk();
				CreativeTabContentManager.fillCreativeTab(tabData);
			}).build());

	public static ItemStack cyclingIcon() {
		long second = Util.getMillis() / 1000L;
		Item item = switch ((int) (second % 3L)) {
			case 1 -> CreateTheAirWarsModItems.GYRO_STABILIZER.get();
			case 2 -> CreateTheAirWarsModItems.ROCKET_DATA_LINK_ITEM.get();
			default -> CreateTheAirWarsModItems.ROCKET_ENGINE.get();
		};
		return new ItemStack(item);
	}
}
