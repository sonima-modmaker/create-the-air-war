
package hi.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class ChipItem extends Item {
	public ChipItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC));
	}
}
