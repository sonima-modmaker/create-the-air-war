
package hi.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class WiresItem extends Item {
	public WiresItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON));
	}
}
