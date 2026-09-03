package hi.client.camera;

import hi.mixin.camera.LevelRendererAccessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;

public final class CameraFeedLevelRendererState {
    private net.minecraft.client.renderer.ViewArea viewArea;
    private SectionOcclusionGraph sectionOcclusionGraph;
    private int lastViewDistance;
    private int lastCameraSectionX;
    private int lastCameraSectionY;
    private int lastCameraSectionZ;
    private double prevCamX;
    private double prevCamY;
    private double prevCamZ;
    private double prevCamRotX;
    private double prevCamRotY;
    private ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections;

    private CameraFeedLevelRendererState() {
    }

    public static CameraFeedLevelRendererState capture(LevelRenderer levelRenderer) {
        LevelRendererAccessor accessor = (LevelRendererAccessor) levelRenderer;
        CameraFeedLevelRendererState state = new CameraFeedLevelRendererState();
        state.viewArea = accessor.ctaw$getViewArea();
        state.sectionOcclusionGraph = accessor.ctaw$getSectionOcclusionGraph();
        state.lastViewDistance = accessor.ctaw$getLastViewDistance();
        state.lastCameraSectionX = accessor.ctaw$getLastCameraSectionX();
        state.lastCameraSectionY = accessor.ctaw$getLastCameraSectionY();
        state.lastCameraSectionZ = accessor.ctaw$getLastCameraSectionZ();
        state.prevCamX = accessor.ctaw$getPrevCamX();
        state.prevCamY = accessor.ctaw$getPrevCamY();
        state.prevCamZ = accessor.ctaw$getPrevCamZ();
        state.prevCamRotX = accessor.ctaw$getPrevCamRotX();
        state.prevCamRotY = accessor.ctaw$getPrevCamRotY();
        // Keep the exact list instance. The vanilla occlusion graph owns this
        // object; replacing it with a copy disconnects the player's renderer
        // and makes all chunks disappear after a monitor frame.
        state.visibleSections = accessor.ctaw$getVisibleSections();
        return state;
    }

    public void apply(LevelRenderer levelRenderer) {
        LevelRendererAccessor accessor = (LevelRendererAccessor) levelRenderer;
        accessor.ctaw$setViewArea(this.viewArea);
        ((LevelRendererStateBridge) levelRenderer).ctaw$replaceSectionOcclusionGraph(this.sectionOcclusionGraph);
        accessor.ctaw$setLastViewDistance(this.lastViewDistance);
        accessor.ctaw$setLastCameraSectionX(this.lastCameraSectionX);
        accessor.ctaw$setLastCameraSectionY(this.lastCameraSectionY);
        accessor.ctaw$setLastCameraSectionZ(this.lastCameraSectionZ);
        accessor.ctaw$setPrevCamX(this.prevCamX);
        accessor.ctaw$setPrevCamY(this.prevCamY);
        accessor.ctaw$setPrevCamZ(this.prevCamZ);
        accessor.ctaw$setPrevCamRotX(this.prevCamRotX);
        accessor.ctaw$setPrevCamRotY(this.prevCamRotY);
        accessor.ctaw$setVisibleSections(this.visibleSections);
    }
}
