package hi.init;

import hi.CreateTheAirWarsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;

public class CreateTheAirWarsModSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, CreateTheAirWarsMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_ENGINE_IDLE = reg("rocket_engine_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_ENGINE_THROTTLE = reg("rocket_engine_throttle");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_12_7MM_LASTSHOT = reg("gun_12_7mm_lastshot");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_12_7MM_LOOP = reg("gun_12_7mm_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> BULLET_PASSBY = reg("bullet_passby");
    public static final DeferredHolder<SoundEvent, SoundEvent> TOMAHAWK_FLIGHT = reg("tomahawk_flight");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEARCH = reg("search");
    public static final DeferredHolder<SoundEvent, SoundEvent> LOCK = reg("lock");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_CLOSE = reg("rocket_explosion_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM = reg("rocket_explosion_medium");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_FAR = reg("rocket_explosion_far");
    public static final DeferredHolder<SoundEvent, SoundEvent> MTS_ROCKETBOOM_CLOSE = reg("mts_rocketboom_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> MTS_ROCKETBOOM_DISTANT = reg("mts_rocketboom_distant");
    public static final DeferredHolder<SoundEvent, SoundEvent> MTS_EXPLOSION_CLOSE = reg("mts_explosion_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> MTS_EXPLOSION_DISTANT = reg("mts_explosion_distant");
    public static final DeferredHolder<SoundEvent, SoundEvent> MTS_HEAVY_EXPLOSION_CLOSE = reg("mts_heavy_explosion_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> MTS_HEAVY_EXPLOSION_DISTANT = reg("mts_heavy_explosion_distant");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_CLOSE_01 = reg("rocket_explosion_close_01");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_CLOSE_02 = reg("rocket_explosion_close_02");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_CLOSE_03 = reg("rocket_explosion_close_03");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM_01 = reg("rocket_explosion_medium_01");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM_02 = reg("rocket_explosion_medium_02");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM_03 = reg("rocket_explosion_medium_03");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM_04 = reg("rocket_explosion_medium_04");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM_05 = reg("rocket_explosion_medium_05");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM_06 = reg("rocket_explosion_medium_06");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM_01_S = reg("rocket_explosion_medium_01_s");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM_02_S = reg("rocket_explosion_medium_02_s");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM_03_S = reg("rocket_explosion_medium_03_s");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM_04_S = reg("rocket_explosion_medium_04_s");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM_05_S = reg("rocket_explosion_medium_05_s");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM_07_S = reg("rocket_explosion_medium_07_s");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM_08_S = reg("rocket_explosion_medium_08_s");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM_09_S = reg("rocket_explosion_medium_09_s");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_MEDIUM_10_S = reg("rocket_explosion_medium_10_s");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_FAR_01 = reg("rocket_explosion_far_01");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_FAR_02 = reg("rocket_explosion_far_02");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_FAR_03 = reg("rocket_explosion_far_03");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_FAR_04 = reg("rocket_explosion_far_04");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_FAR_05 = reg("rocket_explosion_far_05");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_FAR_07 = reg("rocket_explosion_far_07");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_FAR_08 = reg("rocket_explosion_far_08");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_FAR_09 = reg("rocket_explosion_far_09");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_EXPLOSION_FAR_10 = reg("rocket_explosion_far_10");

    public static final DeferredHolder<SoundEvent, SoundEvent> EXPLOTION = reg("explotion");
    public static final DeferredHolder<SoundEvent, SoundEvent> POLET = reg("polet");
    public static final DeferredHolder<SoundEvent, SoundEvent> VILET = reg("vilet");
    public static final DeferredHolder<SoundEvent, SoundEvent> FDGDG = reg("fdgdg");
    public static final DeferredHolder<SoundEvent, SoundEvent> VZRIV = reg("vzriv");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZAPUSK = reg("zapusk");
    public static final DeferredHolder<SoundEvent, SoundEvent> VZRIVC_8 = reg("vzrivc-8");
    public static final DeferredHolder<SoundEvent, SoundEvent> FIRE_AUTOCANNON = reg("fire_autocannon");
    public static final DeferredHolder<SoundEvent, SoundEvent> FIRE_BIG_CANNON = reg("fire_big_cannon");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHELLEXP1 = reg("shellexp1");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHELLEXP2 = reg("shellexp2");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHELLEX3 = reg("shellex3");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHELL_FLYING = reg("shell_flying");
    public static final DeferredHolder<SoundEvent, SoundEvent> GREANATEACTV = reg("greanateactv");
    public static final DeferredHolder<SoundEvent, SoundEvent> EXPLGRENADE = reg("explgrenade");
    public static final DeferredHolder<SoundEvent, SoundEvent> SONAR_SOUND = reg("sonar_sound");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_127MM_LOOP_HYPHEN = reg("12-7mm_gun_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_127MM_LASTSHOT_HYPHEN = reg("12-7mm_gun_lastshot");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_FLY = reg("rocket_fly");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHELL_FLY = reg("shell_fly");

    static {
        for (String id : new String[] {
            "mts_exact_bomb_whistle",
            "mts_exact_bomblet_close0",
            "mts_exact_bomblet_close1",
            "mts_exact_bomblet_close2",
            "mts_exact_bomblet_close3",
            "mts_exact_bomblet_close4",
            "mts_exact_bomblet_close5",
            "mts_exact_bomblet_distant0",
            "mts_exact_bomblet_distant1",
            "mts_exact_bomblet_distant2",
            "mts_exact_bomblet_distant3",
            "mts_exact_crashfire",
            "mts_exact_debrissettle_dirtsmall0",
            "mts_exact_debrissettle_dirtsmall1",
            "mts_exact_debrissettle_dirtsmall2",
            "mts_exact_debrissettle_dirtsmall3",
            "mts_exact_debrissettle_sand0",
            "mts_exact_debrissettle_sand1",
            "mts_exact_debrissettle_sand2",
            "mts_exact_debrissettle_sandsmall0",
            "mts_exact_debrissettle_sandsmall1",
            "mts_exact_debrissettle_sandsmall2",
            "mts_exact_debrissettle_stonesmall0",
            "mts_exact_debrissettle_stonesmall1",
            "mts_exact_debrissettle_stonesmall2",
            "mts_exact_debrissettle_stonesmall3",
            "mts_exact_debrissettle_water0",
            "mts_exact_debrissettle_water1",
            "mts_exact_debrissettle_woodlarge0",
            "mts_exact_debrissettle_woodlarge1",
            "mts_exact_debrissettle_woodlarge2",
            "mts_exact_explosion_distant",
            "mts_exact_explosion_distant_heavy",
            "mts_exact_explosion_distant_heavy1",
            "mts_exact_explosion_distant_heavy2",
            "mts_exact_explosion_distant2",
            "mts_exact_explosion_distant3",
            "mts_exact_explosion_distant4",
            "mts_exact_explosion_heavy",
            "mts_exact_explosion_heavy1",
            "mts_exact_explosion1",
            "mts_exact_explosion2",
            "mts_exact_explosion3",
            "mts_exact_explosion4",
            "mts_exact_rocketboom_distant0",
            "mts_exact_rocketboom_distant1",
            "mts_exact_rocketboom_distant2",
            "mts_exact_rocketboom_distant3",
            "mts_exact_rocketboom0",
            "mts_exact_rocketboom1",
            "mts_exact_rocketboom2",
            "mts_exact_rocketboom3"
        }) reg(id);
    }

    private static DeferredHolder<SoundEvent, SoundEvent> reg(String id) {
        return REGISTRY.register(id,
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, id)));
    }
}
