package hi.mixin.camera;

import hi.client.DroneControllerClientHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiFpvMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void createTheAirWars$hideCrosshairInFpv(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (DroneControllerClientHandler.isControllingFpv()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItemHotbar", at = @At("HEAD"), cancellable = true)
    private void createTheAirWars$hideHotbarInFpv(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (DroneControllerClientHandler.isControllingFpv()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V", at = @At("HEAD"), cancellable = true)
    private void createTheAirWars$hideSelectedItemNameInFpv(GuiGraphics guiGraphics, int yShift, CallbackInfo ci) {
        if (DroneControllerClientHandler.isControllingFpv()) {
            ci.cancel();
        }
    }
}
