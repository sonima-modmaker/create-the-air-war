
package hi.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;

import hi.procedures.M24PriVystrielieSnariadomIzPriedmietaProcedure;

import hi.entity.M24bultEntity;

public class M24Item extends Item {
	public M24Item() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON));
	}

	public UseAnim getUseAnimation(ItemStack itemstack) {
		return UseAnim.BOW;
	}

	public int getUseDuration(ItemStack itemstack) {
		return 72000;
	}

	@Override
	public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
		InteractionResultHolder<ItemStack> ar = InteractionResultHolder.fail(entity.getItemInHand(hand));
		if (entity.getAbilities().instabuild || findAmmo(entity) != ItemStack.EMPTY) {
			ar = InteractionResultHolder.success(entity.getItemInHand(hand));
			entity.startUsingItem(hand);
		}
		return ar;
	}

	@Override
	public void releaseUsing(ItemStack itemstack, Level world, LivingEntity entity, int time) {
		if (!world.isClientSide() && entity instanceof ServerPlayer player) {
			ItemStack stack = findAmmo(player);
			if (player.getAbilities().instabuild || stack != ItemStack.EMPTY) {
				M24bultEntity projectile = M24bultEntity.shoot(world, entity, world.getRandom());
				if (player.getAbilities().instabuild) {
					projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
				} else {
					if (stack.isDamageableItem()) {
						stack.setDamageValue(stack.getDamageValue() + 1);
						if (stack.getDamageValue() >= stack.getMaxDamage()) {
							stack.shrink(1);
							stack.setDamageValue(0);
							if (stack.isEmpty())
								player.getInventory().removeItem(stack);
						}
					} else {
						stack.shrink(1);
						if (stack.isEmpty())
							player.getInventory().removeItem(stack);
					}
				}
				M24PriVystrielieSnariadomIzPriedmietaProcedure.execute(entity, itemstack);
			}
		}
	}

	private ItemStack findAmmo(Player player) {
		return new ItemStack(M24bultEntity.PROJECTILE_ITEM.getItem());
	}
}
