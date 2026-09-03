package hi.mixin.camera;

import hi.client.DroneControllerClientHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererFpvMixin {
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void createTheAirWars$hideHandsInFpv(Camera camera, float partialTick, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (DroneControllerClientHandler.isControllingFpv()) {
            ci.cancel();
        }
    }
}
