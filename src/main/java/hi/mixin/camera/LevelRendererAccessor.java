package hi.mixin.camera;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
    @Accessor("sectionOcclusionGraph")
    SectionOcclusionGraph ctaw$getSectionOcclusionGraph();

    @Accessor("viewArea")
    net.minecraft.client.renderer.ViewArea ctaw$getViewArea();

    @Accessor("viewArea")
    void ctaw$setViewArea(net.minecraft.client.renderer.ViewArea value);

    @Accessor("sectionRenderDispatcher")
    SectionRenderDispatcher ctaw$getSectionRenderDispatcher();

    @Accessor("lastViewDistance")
    int ctaw$getLastViewDistance();

    @Accessor("lastViewDistance")
    void ctaw$setLastViewDistance(int value);

    @Accessor("lastCameraSectionX")
    int ctaw$getLastCameraSectionX();

    @Accessor("lastCameraSectionX")
    void ctaw$setLastCameraSectionX(int value);

    @Accessor("lastCameraSectionY")
    int ctaw$getLastCameraSectionY();

    @Accessor("lastCameraSectionY")
    void ctaw$setLastCameraSectionY(int value);

    @Accessor("lastCameraSectionZ")
    int ctaw$getLastCameraSectionZ();

    @Accessor("lastCameraSectionZ")
    void ctaw$setLastCameraSectionZ(int value);

    @Accessor("prevCamX")
    double ctaw$getPrevCamX();

    @Accessor("prevCamX")
    void ctaw$setPrevCamX(double value);

    @Accessor("prevCamY")
    double ctaw$getPrevCamY();

    @Accessor("prevCamY")
    void ctaw$setPrevCamY(double value);

    @Accessor("prevCamZ")
    double ctaw$getPrevCamZ();

    @Accessor("prevCamZ")
    void ctaw$setPrevCamZ(double value);

    @Accessor("prevCamRotX")
    double ctaw$getPrevCamRotX();

    @Accessor("prevCamRotX")
    void ctaw$setPrevCamRotX(double value);

    @Accessor("prevCamRotY")
    double ctaw$getPrevCamRotY();

    @Accessor("prevCamRotY")
    void ctaw$setPrevCamRotY(double value);

    @Accessor("visibleSections")
    ObjectArrayList<SectionRenderDispatcher.RenderSection> ctaw$getVisibleSections();

    @Accessor("visibleSections")
    void ctaw$setVisibleSections(ObjectArrayList<SectionRenderDispatcher.RenderSection> sections);
}
