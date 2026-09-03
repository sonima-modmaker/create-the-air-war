package hi.mixin.camera;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SectionOcclusionGraph.class)
public class SectionOcclusionGraphMixin {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void createTheAirWars$bypassUpdateDuringFeed(
        boolean smartCull,
        Camera camera,
        Frustum frustum,
        List<SectionRenderDispatcher.RenderSection> sections,
        CallbackInfo ci
    ) {
        if (hi.client.camera.CameraFeedRenderer.isRenderingFeed()) {
            ((SectionOcclusionGraph) (Object) this).addSectionsInFrustum(frustum, sections);
            ci.cancel();
        }
    }
}
