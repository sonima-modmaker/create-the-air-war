
package hi.item;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.HoeItem;
import net.minecraft.tags.BlockTags;

import hi.init.CreateTheAirWarsModItems;

public class TitaniumHoeItem extends HoeItem {
	public TitaniumHoeItem() {
		super(new Tier() {
			public int getUses() {
				return 500;
			}

			public float getSpeed() {
				return 10f;
			}

			public float getAttackDamageBonus() {
				return 5f;
			}

			public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
				return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
			}

			public int getEnchantmentValue() {
				return 3;
			}

			public Ingredient getRepairIngredient() {
				return Ingredient.of(new ItemStack(CreateTheAirWarsModItems.TITANIUMINGOT.get()));
			}
		}, new Item.Properties().attributes(HoeItem.createAttributes(new Tier() {
			public int getUses() { return 500; }
			public float getSpeed() { return 10f; }
			public float getAttackDamageBonus() { return 5f; }
			public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() { return BlockTags.INCORRECT_FOR_DIAMOND_TOOL; }
			public int getEnchantmentValue() { return 3; }
			public Ingredient getRepairIngredient() { return Ingredient.of(new ItemStack(CreateTheAirWarsModItems.TITANIUMINGOT.get())); }
		}, 0, -3f)));
	}
}
