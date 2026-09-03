package hi.item;

import hi.block.entity.TomahawkBlockEntity;
import hi.init.CreateTheAirWarsModBlocks;
import hi.world.inventory.FdgddMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public class TargetGunnergadgetItem extends Item {
	public TargetGunnergadgetItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
		ItemStack stack = entity.getItemInHand(hand);
		openTargetGunnerMenu(entity, hand);
		return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (world.getBlockState(pos).getBlock() == CreateTheAirWarsModBlocks.TOMAHAWK.get()) {
			if (!world.isClientSide()) {
				ItemStack stack = context.getItemInHand();
				net.minecraft.nbt.CompoundTag tag = stack.has(DataComponents.CUSTOM_DATA)
					? stack.get(DataComponents.CUSTOM_DATA).copyTag()
					: null;
				if (tag != null && world.getBlockEntity(pos) instanceof TomahawkBlockEntity be) {
					int targetX = tag.getInt("targetX");
					int targetY = tag.contains("targetY") ? tag.getInt("targetY") : pos.getY();
					int targetZ = tag.getInt("targetZ");
					be.setTarget(targetX, targetY, targetZ);
					world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
					if (context.getPlayer() != null) {
						context.getPlayer().displayClientMessage(
							Component.translatable("message.create_the_air_wars.target_gunner.target_set", targetX, targetY, targetZ),
							true
						);
					}
				}
			}
			return InteractionResult.sidedSuccess(world.isClientSide());
		}

		openTargetGunnerMenu(context.getPlayer(), context.getHand());
		return InteractionResult.sidedSuccess(world.isClientSide());
	}

	private static void openTargetGunnerMenu(Player player, InteractionHand hand) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}

		MenuProvider provider = new MenuProvider() {
			@Override
			public Component getDisplayName() {
				return Component.translatable("screen.create_the_air_wars.target_gunner.title");
			}

			@Override
			public AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inventory, Player menuPlayer) {
				return new FdgddMenu(id, inventory, null);
			}
		};

		try {
			Method openMenuWithData = ServerPlayer.class.getMethod("openMenu", MenuProvider.class, Consumer.class);
			Consumer<Object> writer = buffer -> writeMenuData(buffer, player.blockPosition(), hand);
			openMenuWithData.invoke(serverPlayer, provider, writer);
		} catch (ReflectiveOperationException ignored) {
			serverPlayer.openMenu(provider);
		}
	}

	private static void writeMenuData(Object buffer, BlockPos pos, InteractionHand hand) {
		try {
			Method writeBlockPos = buffer.getClass().getMethod("writeBlockPos", BlockPos.class);
			Method writeByte = buffer.getClass().getMethod("writeByte", int.class);
			writeBlockPos.invoke(buffer, pos);
			writeByte.invoke(buffer, hand == InteractionHand.MAIN_HAND ? 0 : 1);
		} catch (ReflectiveOperationException ignored) {
		}
	}
}
