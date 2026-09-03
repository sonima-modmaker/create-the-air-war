package hi.mixin.performance;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.engine_room.flywheel.impl.BackendManagerImpl", remap = false)
public class FlywheelBackendManagerMixin {
    @Inject(method = "currentBackend", at = @At("HEAD"), cancellable = true)
    private static void createTheAirWars$forceOffBackendDuringFeed(CallbackInfoReturnable<Object> cir) {
        if (hi.client.camera.CameraFeedRenderer.isRenderingFeed()) {
            cir.setReturnValue(dev.engine_room.flywheel.api.backend.BackendManager.offBackend());
        }
    }

    @Inject(method = "isBackendOn", at = @At("HEAD"), cancellable = true)
    private static void createTheAirWars$forceBackendOffDuringFeed(CallbackInfoReturnable<Boolean> cir) {
        if (hi.client.camera.CameraFeedRenderer.isRenderingFeed()) {
            cir.setReturnValue(false);
        }
    }
}
