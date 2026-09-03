package hi.world.inventory;

import hi.block.entity.AntiAircraftLauncherBlockEntity;
import hi.init.CreateTheAirWarsModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;

public class AntiAircraftLauncherMenu extends AbstractContainerMenu {
    public final Level world;
    public final Player entity;
    public int x, y, z;
    private ContainerLevelAccess access = ContainerLevelAccess.NULL;
    private BlockEntity boundBlockEntity = null;

    public AntiAircraftLauncherMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        super(CreateTheAirWarsModMenus.ANTI_AIRCRAFT_LAUNCHER.get(), id);
        this.entity = inv.player;
        this.world = inv.player.level();
        BlockPos pos = null;
        if (extraData != null) {
            pos = extraData.readBlockPos();
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            access = ContainerLevelAccess.create(world, pos);
            boundBlockEntity = this.world.getBlockEntity(pos);
        }

        // No slots needed for enemy list UI

    }

    public BlockEntity getBlockEntity() {
        return boundBlockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.boundBlockEntity != null) {
            return AbstractContainerMenu.stillValid(this.access, player, this.boundBlockEntity.getBlockState().getBlock());
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 0) { // No custom slot matching
                if (!this.moveItemStackTo(itemstack1, 0, this.slots.size(), true))
                    return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(itemstack1, 0, 0, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.getCount() == 0)
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();
        }
        return itemstack;
    }
}
