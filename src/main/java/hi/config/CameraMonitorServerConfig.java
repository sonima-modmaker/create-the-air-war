package hi.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CameraMonitorServerConfig {
    private static final int[] ALLOWED_RESOLUTIONS = new int[] { 64, 96, 128, 192, 256, 384, 512, 768, 1024 };
    private static final int DEFAULT_FPS = 4;
    private static final int DEFAULT_RESOLUTION = 256;
    private static final int DEFAULT_VIEW_DISTANCE = 6;

    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.IntValue MONITOR_FEED_FPS;
    private static final ModConfigSpec.IntValue MONITOR_FEED_RESOLUTION;
    private static final ModConfigSpec.IntValue MONITOR_FEED_VIEW_DISTANCE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("camera_monitor");
        MONITOR_FEED_FPS = builder
            .comment("Maximum camera monitor refresh rate in frames per second.", "Recommended range: 4-12")
            .defineInRange("feed_fps", DEFAULT_FPS, 1, 20);
        MONITOR_FEED_RESOLUTION = builder
            .comment("Camera monitor render resolution. Supported values: 64, 96, 128, 192, 256, 384, 512, 768, 1024")
            .defineInRange("feed_resolution", DEFAULT_RESOLUTION, ALLOWED_RESOLUTIONS[0], ALLOWED_RESOLUTIONS[ALLOWED_RESOLUTIONS.length - 1]);
        MONITOR_FEED_VIEW_DISTANCE = builder
            .comment("Extra chunk radius loaded and rendered around every active camera.",
                "These chunks are added to the player's normal chunks; they do not replace them.",
                "Default: 6, supported range: 1-16. High values are expensive.")
            .defineInRange("feed_view_distance", DEFAULT_VIEW_DISTANCE, 1, 16);
        builder.pop();
        SPEC = builder.build();
    }

    private CameraMonitorServerConfig() {
    }

    public static boolean shouldLoadChunksByCamera() {
        return true;
    }

    public static int getMonitorFeedFps() {
        return Math.clamp(MONITOR_FEED_FPS.get(), 1, 5);
    }

    public static int getMonitorFeedResolution() {
        return normalizeResolution(MONITOR_FEED_RESOLUTION.get());
    }

    public static int getMonitorFeedViewDistance() {
        return Math.clamp(MONITOR_FEED_VIEW_DISTANCE.get(), 1, 16);
    }

    public static int getDefaultFps() {
        return DEFAULT_FPS;
    }

    public static int getDefaultResolution() {
        return DEFAULT_RESOLUTION;
    }

    public static int getDefaultViewDistance() {
        return DEFAULT_VIEW_DISTANCE;
    }

    public static int normalizeResolution(int resolution) {
        int best = ALLOWED_RESOLUTIONS[0];
        int bestDistance = Math.abs(resolution - best);
        for (int allowed : ALLOWED_RESOLUTIONS) {
            int distance = Math.abs(resolution - allowed);
            if (distance < bestDistance) {
                best = allowed;
                bestDistance = distance;
            }
        }
        return best;
    }
}
