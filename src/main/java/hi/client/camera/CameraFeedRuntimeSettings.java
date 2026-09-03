package hi.client.camera;

import hi.config.CameraMonitorServerConfig;

public final class CameraFeedRuntimeSettings {
    private static int serverFeedFps = CameraMonitorServerConfig.getDefaultFps();
    private static int serverFeedResolution = CameraMonitorServerConfig.getDefaultResolution();
    private static int serverFeedViewDistance = CameraMonitorServerConfig.getDefaultViewDistance();

    private CameraFeedRuntimeSettings() {
    }

    public static int getFeedFps() {
        return serverFeedFps;
    }

    public static int getFeedResolution() {
        return serverFeedResolution;
    }

    public static int getFeedViewDistance() {
        return serverFeedViewDistance;
    }

    public static boolean shouldLoadChunksByCamera() {
        return true;
    }

    public static void applyServerSettings(int fps, int resolution, int viewDistance) {
        int normalizedFps = Math.clamp(fps, 1, 5);
        int normalizedResolution = CameraMonitorServerConfig.normalizeResolution(resolution);
        int normalizedViewDistance = Math.clamp(viewDistance, 1, 16);
        if (serverFeedFps == normalizedFps
            && serverFeedResolution == normalizedResolution
            && serverFeedViewDistance == normalizedViewDistance) {
            return;
        }
        serverFeedFps = normalizedFps;
        serverFeedResolution = normalizedResolution;
        serverFeedViewDistance = normalizedViewDistance;
        CameraFeedRenderer.clear();
        CameraFeedTextureManager.onServerSettingsChanged();
    }

    public static void resetToDefaults() {
        applyServerSettings(
            CameraMonitorServerConfig.getDefaultFps(),
            CameraMonitorServerConfig.getDefaultResolution(),
            CameraMonitorServerConfig.getDefaultViewDistance()
        );
    }
}
