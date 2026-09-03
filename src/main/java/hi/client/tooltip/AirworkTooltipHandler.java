package hi.client.tooltip;

import com.simibubi.create.foundation.item.ItemDescription;
import hi.CreateTheAirWarsMod;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.HashMap;
import java.util.Map;
import static java.util.Map.entry;

@net.neoforged.fml.common.EventBusSubscriber(modid = CreateTheAirWarsMod.MODID, value = Dist.CLIENT)
public final class AirworkTooltipHandler {
    private static final Map<String, String> TOOLTIP_KEYS = Map.ofEntries(
        entry("create_the_air_wars:target_gunnergadget", "item.create_the_air_wars.target_gunnergadget.tooltip"),
        entry("create_the_air_wars:gyro_stabilizer", "block.create_the_air_wars.gyro_stabilizer.tooltip"),
        entry("create_the_air_wars:rocket_engine", "block.create_the_air_wars.rocket_engine.tooltip"),
        entry("create_the_air_wars:rocket_data_link", "block.create_the_air_wars.rocket_data_link.tooltip"),
        entry("create_the_air_wars:tomahawk", "block.create_the_air_wars.tomahawk.tooltip"),
        entry("create_the_air_wars:aim9x", "block.create_the_air_wars.aim9x.tooltip"),
        entry("create_the_air_wars:heattrap", "block.create_the_air_wars.heattrap.tooltip"),
        entry("create_the_air_wars:heattrap_charge", "item.create_the_air_wars.heattrap_charge.tooltip"),
        entry("create_the_air_wars:vihr", "block.create_the_air_wars.vihr.tooltip"),
        entry("create_the_air_wars:x25ml", "block.create_the_air_wars.x25ml.tooltip"),
        entry("create_the_air_wars:vihr_rocket", "item.create_the_air_wars.vihr_rocket.tooltip"),
        entry("create_the_air_wars:c75", "item.create_the_air_wars.c75.tooltip"),
        entry("create_the_air_wars:monitor", "block.create_the_air_wars.monitor.tooltip"),
        entry("create_the_air_wars:camera_link", "item.create_the_air_wars.camera_link.tooltip"),
        entry("create_the_air_wars:anti_aircraft_launcher", "block.create_the_air_wars.anti_aircraft_launcher.tooltip"),
        entry("create_the_air_wars:minigun", "block.create_the_air_wars.minigun.tooltip")
    );
    private static final Map<String, ItemDescription> CACHE = new HashMap<>();
    private static String cachedLanguage = "";

    private AirworkTooltipHandler() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String idStr = id.toString();

        String tooltipKey = TOOLTIP_KEYS.get(idStr);
        if (tooltipKey == null) {
            return;
        }

        refreshCacheIfNeeded();
        ItemDescription description = CACHE.computeIfAbsent(tooltipKey,
            key -> ItemDescription.create(key, FontHelper.Palette.STANDARD_CREATE));
        if (description == null) {
            return;
        }

        event.getToolTip().addAll(Math.min(1, event.getToolTip().size()), description.getCurrentLines());
    }

    private static void refreshCacheIfNeeded() {
        String currentLanguage = Minecraft.getInstance()
            .getLanguageManager()
            .getSelected();
        if (!currentLanguage.equals(cachedLanguage)) {
            cachedLanguage = currentLanguage;
            CACHE.clear();
        }
    }
}
