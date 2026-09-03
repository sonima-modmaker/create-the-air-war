package hi.mixin.creativetab;

import hi.init.CreateTheAirWarsModTabs;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeModeInventoryScreenIconMixin {
    @Redirect(
            method = "renderTabButton",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/CreativeModeTab;getIconItem()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack ctaw$useAnimatedIcon(CreativeModeTab tab) {
        if (tab == CreateTheAirWarsModTabs.CREATETHEAIRWAR.get()) {
            return CreateTheAirWarsModTabs.cyclingIcon();
        }
        return tab.getIconItem();
    }
}
