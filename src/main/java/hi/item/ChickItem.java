
package hi.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class ChickItem extends Item {
	public ChickItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON));
	}
}
