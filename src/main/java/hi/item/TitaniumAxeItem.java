
package hi.item;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.AxeItem;
import net.minecraft.tags.BlockTags;

import hi.init.CreateTheAirWarsModItems;

public class TitaniumAxeItem extends AxeItem {
	public TitaniumAxeItem() {
		super(new Tier() {
			public int getUses() {
				return 500;
			}

			public float getSpeed() {
				return 10f;
			}

			public float getAttackDamageBonus() {
				return 7f;
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
		}, new Item.Properties().attributes(AxeItem.createAttributes(new Tier() {
			public int getUses() { return 500; }
			public float getSpeed() { return 10f; }
			public float getAttackDamageBonus() { return 7f; }
			public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() { return BlockTags.INCORRECT_FOR_DIAMOND_TOOL; }
			public int getEnchantmentValue() { return 3; }
			public Ingredient getRepairIngredient() { return Ingredient.of(new ItemStack(CreateTheAirWarsModItems.TITANIUMINGOT.get())); }
		}, 1, -2f)).fireResistant());
	}
}
