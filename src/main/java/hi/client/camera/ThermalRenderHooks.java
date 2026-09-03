package hi.client.camera;

import hi.client.CtawShaders;
import net.minecraft.client.renderer.ShaderInstance;

public final class ThermalRenderHooks {
    private ThermalRenderHooks() {
    }

    public static boolean isThermalCameraFeedActive() {
        return CameraFeedRenderer.isRenderingFeed() && CameraMonitorClientHandler.isThermalModeEnabled();
    }

    public static ShaderInstance getThermalBlockShaderOrFallback(ShaderInstance fallback) {
        if (!isThermalCameraFeedActive()) {
            return fallback;
        }
        ShaderInstance shader = CtawShaders.getThermalBlockShader();
        return shader != null ? shader : fallback;
    }
}
