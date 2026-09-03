package hi.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class RadarServerConfig {
    private static final int DEFAULT_DETECTION_RANGE = 1024;

    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.IntValue DETECTION_RANGE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("radar");

        DETECTION_RANGE = builder
            .comment(
                "Detection range radius of SAM anti-aircraft launchers (ПВО) and radars in blocks.",
                "Default: 1024 blocks. Supported range: 16 to 8192 blocks."
            )
            .defineInRange("detection_range", DEFAULT_DETECTION_RANGE, 16, 8192);

        builder.pop();
        SPEC = builder.build();
    }

    private RadarServerConfig() {
    }

    public static int getDetectionRange() {
        return DETECTION_RANGE.get();
    }
}
