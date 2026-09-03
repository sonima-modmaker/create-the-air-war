package hi.mixin.performance;

import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public class OptionsMixin {
    @Inject(method = "getEffectiveRenderDistance", at = @At("HEAD"), cancellable = true)
    private void createTheAirWars$limitRenderDistanceDuringFeed(CallbackInfoReturnable<Integer> cir) {
        if (hi.client.camera.CameraFeedRenderer.isRenderingFeed()) {
            cir.setReturnValue(hi.client.camera.CameraFeedRuntimeSettings.getFeedViewDistance());
        }
    }
}
