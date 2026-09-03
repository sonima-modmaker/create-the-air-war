package hi.mixin.performance;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.seibel.distanthorizons.core.api.internal.ClientApi", remap = false)
public class ClientApiMixin {
    @Inject(method = "renderLods", at = @At("HEAD"), cancellable = true)
    private void createTheAirWars$cancelRenderLods(CallbackInfo ci) {
        if (hi.client.camera.CameraFeedRenderer.isRenderingFeed()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderDeferredLodsForShaders", at = @At("HEAD"), cancellable = true)
    private void createTheAirWars$cancelRenderDeferred(CallbackInfo ci) {
        if (hi.client.camera.CameraFeedRenderer.isRenderingFeed()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFadeOpaque", at = @At("HEAD"), cancellable = true)
    private void createTheAirWars$cancelRenderFadeOpaque(CallbackInfo ci) {
        if (hi.client.camera.CameraFeedRenderer.isRenderingFeed()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFadeTransparent", at = @At("HEAD"), cancellable = true)
    private void createTheAirWars$cancelRenderFadeTransparent(CallbackInfo ci) {
        if (hi.client.camera.CameraFeedRenderer.isRenderingFeed()) {
            ci.cancel();
        }
    }
}
