package hi.mixin.creativetab;

import hi.creative.CreativeTabContentManager;
import hi.init.CreateTheAirWarsModTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

@Mixin(CreativeModeTab.class)
public class CreativeModeTabMixin {
    @Shadow
    private Collection<ItemStack> displayItems;

    @Shadow
    private Set<ItemStack> displayItemsSearchTab;

    @Inject(method = "buildContents", at = @At("HEAD"), cancellable = true)
    private void ctaw$buildContents(CreativeModeTab.ItemDisplayParameters parameters, CallbackInfo ci) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;
        if (self != CreateTheAirWarsModTabs.CREATETHEAIRWAR.get()) {
            return;
        }

        LinkedList<ItemStack> displayItems = new LinkedList<>();
        LinkedHashSet<ItemStack> searchItems = new LinkedHashSet<>();
        CreativeTabContentManager.reloadFromDisk();
        CreativeTabContentManager.populateCreativeTab(displayItems, searchItems);
        this.displayItems = displayItems;
        this.displayItemsSearchTab = searchItems;
        ci.cancel();
    }
}
