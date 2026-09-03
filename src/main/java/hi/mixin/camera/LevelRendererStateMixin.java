package hi.mixin.camera;

import hi.client.camera.LevelRendererStateBridge;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererStateMixin implements LevelRendererStateBridge {
    @Shadow
    @Final
    @Mutable
    private SectionOcclusionGraph sectionOcclusionGraph;

    @Override
    public void ctaw$replaceSectionOcclusionGraph(SectionOcclusionGraph graph) {
        this.sectionOcclusionGraph = graph;
    }
}
