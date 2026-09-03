
package hi.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class AntennaItem extends Item {
	public AntennaItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON));
	}
}
