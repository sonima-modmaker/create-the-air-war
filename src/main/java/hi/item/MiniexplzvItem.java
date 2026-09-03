
package hi.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class MiniexplzvItem extends Item {
	public MiniexplzvItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON));
	}
}
