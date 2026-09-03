package hi.world.inventory;

import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.IItemHandler;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
 
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;

import hi.init.CreateTheAirWarsModMenus;

public class DfgdfgMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
	public final static HashMap<String, Object> guistate = new HashMap<>();
	public final Level world;
	public final Player entity;
	public int x, y, z;
	private ContainerLevelAccess access = ContainerLevelAccess.NULL;
	private IItemHandler internal;
	private final Map<Integer, Slot> customSlots = new HashMap<>();
	private boolean bound = false;
	private Supplier<Boolean> boundItemMatcher = null;
	private Entity boundEntity = null;
	private BlockEntity boundBlockEntity = null;

	public DfgdfgMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		super(CreateTheAirWarsModMenus.DFGDFG.get(), id);
		this.entity = inv.player;
		this.world = inv.player.level();
		this.internal = new ItemStackHandler(26);
		BlockPos pos = null;
		if (extraData != null) {
			pos = extraData.readBlockPos();
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
			access = ContainerLevelAccess.create(world, pos);
		}
		setupSlots(inv);
	}

	private void setupSlots(Inventory inv) {
		int startX = 20;
		int startY = 20;
		int slotIndex = 0;
		for (int row = 0; row < 5; row++) {
			for (int col = 0; col < 5; col++) {
				int xPos = startX + col * 18;
				int yPos = startY + row * 18;
				Slot slot = new SlotItemHandler(internal, slotIndex, xPos, yPos);
				customSlots.put(slotIndex, this.addSlot(slot));
				slotIndex++;
			}
		}
		Slot outputSlot = new SlotItemHandler(internal, 25, startX + 5 * 18 + 8, startY + 2 * 18);
		customSlots.put(25, this.addSlot(outputSlot));

		int invStartX = 8;
		int invStartY = 110;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new Slot(inv, col + row * 9 + 9, invStartX + col * 18, invStartY + row * 18));
			}
		}
		int hotbarY = invStartY + 58;
		for (int col = 0; col < 9; col++) {
			this.addSlot(new Slot(inv, col, invStartX + col * 18, hotbarY));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		if (this.bound) {
			if (this.boundItemMatcher != null)
				return this.boundItemMatcher.get();
			else if (this.boundBlockEntity != null)
				return AbstractContainerMenu.stillValid(this.access, player, this.boundBlockEntity.getBlockState().getBlock());
			else if (this.boundEntity != null)
				return this.boundEntity.isAlive();
		}
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
	}

	public Map<Integer, Slot> get() {
		return customSlots;
	}
}
