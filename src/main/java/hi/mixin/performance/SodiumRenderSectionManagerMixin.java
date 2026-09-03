package hi.mixin.performance;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager", remap = false)
public class SodiumRenderSectionManagerMixin {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true, require = 0)
    private void createTheAirWars$bypassUpdateDuringFeed(
        net.minecraft.client.Camera camera,
        @org.spongepowered.asm.mixin.injection.Coerce Object viewport,
        boolean updateVisible,
        CallbackInfo ci
    ) {
        // Let Sodium/Embeddium build the correct visible section graph for the
        // monitor camera. CameraFeedRenderer captures and restores Sodium state
        // around the offscreen pass so the player's main render does not inherit
        // the monitor camera's render lists.
    }
}
