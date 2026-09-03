package hi.mixin.performance;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.seibel.distanthorizons.core.render.renderer.LodRenderer", remap = false)
public class LodRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void createTheAirWars$cancelLodRenderDuringFeed(CallbackInfo ci) {
        if (hi.client.camera.CameraFeedRenderer.isRenderingFeed()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderDeferred", at = @At("HEAD"), cancellable = true)
    private void createTheAirWars$cancelLodDeferredRenderDuringFeed(CallbackInfo ci) {
        if (hi.client.camera.CameraFeedRenderer.isRenderingFeed()) {
            ci.cancel();
        }
    }
}
