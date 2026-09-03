package hi.mixin.camera;

import hi.client.camera.CameraFeedRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererDirtyMixin {
    @Shadow
    private net.minecraft.client.multiplayer.ClientLevel level;

    @Inject(method = "setSectionDirty(IIIZ)V", at = @At("HEAD"))
    private void ctaw$onSetSectionDirty(int x, int y, int z, boolean reRenderOnMainThread, CallbackInfo ci) {
        ViewArea cameraViewArea = CameraFeedRenderer.getCameraViewArea();
        if (cameraViewArea != null) {
            ViewAreaAccessor accessor = (ViewAreaAccessor) cameraViewArea;
            BlockPos requestedOrigin = new BlockPos(x << 4, y << 4, z << 4);
            SectionRenderDispatcher.RenderSection section = accessor.ctaw$invokeGetRenderSectionAt(requestedOrigin);
            if (section != null && section.getOrigin().equals(requestedOrigin)) {
                section.setDirty(reRenderOnMainThread);
            }
        }
    }

    @Inject(method = "setSectionDirty(III)V", at = @At("HEAD"))
    private void ctaw$onSetSectionDirtySimple(int x, int y, int z, CallbackInfo ci) {
        ViewArea cameraViewArea = CameraFeedRenderer.getCameraViewArea();
        if (cameraViewArea != null) {
            ViewAreaAccessor accessor = (ViewAreaAccessor) cameraViewArea;
            BlockPos requestedOrigin = new BlockPos(x << 4, y << 4, z << 4);
            SectionRenderDispatcher.RenderSection section = accessor.ctaw$invokeGetRenderSectionAt(requestedOrigin);
            if (section != null && section.getOrigin().equals(requestedOrigin)) {
                section.setDirty(true);
            }
        }
    }

    @Inject(method = "onChunkLoaded", at = @At("HEAD"))
    private void ctaw$onChunkLoaded(net.minecraft.world.level.ChunkPos chunkPos, CallbackInfo ci) {
        ViewArea cameraViewArea = CameraFeedRenderer.getCameraViewArea();
        if (cameraViewArea != null && this.level != null) {
            ViewAreaAccessor accessor = (ViewAreaAccessor) cameraViewArea;
            for (int y = this.level.getMinSection(); y < this.level.getMaxSection(); ++y) {
                BlockPos blockpos = new BlockPos(chunkPos.getMinBlockX(), SectionPos.sectionToBlockCoord(y), chunkPos.getMinBlockZ());
                SectionRenderDispatcher.RenderSection rendersection = accessor.ctaw$invokeGetRenderSectionAt(blockpos);
                if (rendersection != null && rendersection.getOrigin().equals(blockpos)) {
                    rendersection.setDirty(true);
                }
            }
        }
    }
}
