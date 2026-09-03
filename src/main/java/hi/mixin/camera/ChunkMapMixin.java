package hi.mixin.camera;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    @Inject(method = "isChunkTracked", at = @At("HEAD"), cancellable = true)
    private void createTheAirWars$isCameraChunkTracked(ServerPlayer player, int x, int z, CallbackInfoReturnable<Boolean> cir) {
        if (hi.util.CameraChunkTracker.isTracking(player, x, z)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isChunkOnTrackedBorder", at = @At("HEAD"), cancellable = true)
    private void createTheAirWars$isCameraChunkOnTrackedBorder(ServerPlayer player, int x, int z, CallbackInfoReturnable<Boolean> cir) {
        if (hi.util.CameraChunkTracker.isTracking(player, x, z)) {
            cir.setReturnValue(true);
        }
    }
}
