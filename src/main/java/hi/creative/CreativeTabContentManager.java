package hi.creative;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hi.CreateTheAirWarsMod;
import hi.init.CreateTheAirWarsModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@net.neoforged.fml.common.EventBusSubscriber(modid = CreateTheAirWarsMod.MODID)
public final class CreativeTabContentManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("create_the_air_wars_tab_items.json");
    private static final String AIRWORK_NAMESPACE = "create_the_air_wars";
    private static final int DEFAULT_COLUMNS = 9;
    private static final int DEFAULT_ROWS = 15;
    private static final int DEFAULT_SLOT_COUNT = DEFAULT_COLUMNS * DEFAULT_ROWS;
    private static final int LAYOUT_VERSION = 16;
    private static final ResourceLocation SPACER_ID = ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "spacer");
    private static final ArrayList<ResourceLocation> LAYOUT = new ArrayList<>();
    private static final String[] DEFAULT_TEMPLATE = {
        "create_the_air_wars:nine_k_119m",
        "create_the_air_wars:c_25a",
        "create_the_air_wars:rim_7",
        "create_the_air_wars:tomahawk",
        "create_the_air_wars:sc_250",
        "create_the_air_wars:machinegun",
        "create_the_air_wars:minigun",
        "create_the_air_wars:ozm_72",
        "create_the_air_wars:aim9x",
        "create_the_air_wars:heattrap",
        "create_the_air_wars:heattrap_charge",
        "create_the_air_wars:deep_state_amalgam_ore",
        "create_the_air_wars:deepdlatesulfurore",
        "create_the_air_wars:deepslatetitaniumore",
        "create_the_air_wars:amalgam_ore",
        "create_the_air_wars:sulfur_ore",
        "create_the_air_wars:titanium_ore",
        "create_the_air_wars:rough_amalgam",
        "create_the_air_wars:raw_sulfur",
        "create_the_air_wars:rawtitanium",
        "create_the_air_wars:crushed_amalgam",
        "create_the_air_wars:crashedsulfur",
        "create_the_air_wars:crashedtitanium",
        "create_the_air_wars:activator",
        "create_the_air_wars:ampilifier",
        "create_the_air_wars:chip",
        "create_the_air_wars:wires",
        "create_the_air_wars:display",
        "create_the_air_wars:antenna",
        "create_the_air_wars:titaniumingot",
        "create_the_air_wars:titaniumsheet",
        "create_the_air_wars:titaniumblock",
        "create_the_air_wars:titaniumnugget",
        "create_the_air_wars:bigexplzv",
        "create_the_air_wars:explosive",
        "create_the_air_wars:miniexplzv",
        "create_the_air_wars:rocketengine",
        "create_the_air_wars:pieceof_mirror",
        "create_the_air_wars:mirror",
        "create_the_air_wars:target_gunnergadget",
        "create_the_air_wars:titanium_axe",
        "create_the_air_wars:titaniumshovel",
        "create_the_air_wars:titaniumpickaxe",
        "create_the_air_wars:titanium_hoe",
        "create_the_air_wars:wt",
        "create_the_air_wars:camera",
        "create_the_air_wars:monitor",
        "create_the_air_wars:camera_link",
        "create_the_air_wars:vihr",
        "create_the_air_wars:vihr_rocket",
        "create_the_air_wars:x25ml",
        "create_the_air_wars:c75",
        "create_the_air_wars:anti_aircraft_launcher",
        "create_the_air_wars:rocket_engine",
        "create_the_air_wars:rocket_data_link",
        "create_the_air_wars:gyro_stabilizer"
    };
    private static boolean loaded = false;

    private CreativeTabContentManager() {
    }

    public static synchronized void fillCreativeTab(CreativeModeTab.Output tabData) {
        ensureLoaded();
        for (ResourceLocation id : LAYOUT) {
            if (id == null || SPACER_ID.equals(id)) {
                continue;
            }
            if (isRemovedItem(id)) {
                continue;
            }
            if (isRemovedItem(id)) {
                continue;
            }
            Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id);
            if (item != null && item != Items.AIR) {
                tabData.accept(new ItemStack(item));
            }
        }
    }

    public static synchronized void reloadFromDisk() {
        loaded = false;
        LAYOUT.clear();
        ensureLoaded();
    }

    public static synchronized List<ResourceLocation> getLayout() {
        ensureLoaded();
        return new ArrayList<>(LAYOUT);
    }

    public static synchronized List<ResourceLocation> getAllEligibleIds() {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.keySet().stream()
            .filter(CreativeTabContentManager::isEligibleItem)
            .sorted(Comparator.comparing(ResourceLocation::toString))
            .collect(Collectors.toList());
    }

    public static synchronized void setLayout(List<ResourceLocation> newLayout) {
        ensureLoaded();
        Set<ResourceLocation> allowed = new LinkedHashSet<>(getAllEligibleIds());
        LAYOUT.clear();
        for (ResourceLocation id : newLayout) {
            if (id != null && allowed.contains(id)) {
                LAYOUT.add(id);
            }
        }
    }

    public static synchronized void resetToDefaultsAndSave() {
        LAYOUT.clear();
        LAYOUT.addAll(getDefaultLayoutTemplate());
        save();
    }

    public static synchronized void save() {
        String jsonText = buildJsonText();
        writeConfig(jsonText);
    }

    public static synchronized void saveWorldCopy(String worldName) {
        if (worldName == null || worldName.isBlank()) {
            return;
        }
        String jsonText = buildJsonText();
        Path worldPath = FMLPaths.GAMEDIR.get()
            .resolve("saves")
            .resolve(worldName)
            .resolve("data")
            .resolve("create_the_air_wars_tab_items.json");
        try {
            Files.createDirectories(worldPath.getParent());
            Files.writeString(worldPath, jsonText, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public static synchronized void saveAllWorldCopies() {
        String jsonText = buildJsonText();
        Path savesPath = FMLPaths.GAMEDIR.get().resolve("saves");
        if (!Files.isDirectory(savesPath)) {
            return;
        }
        try {
            Files.list(savesPath).filter(Files::isDirectory).forEach(worldDir -> {
                Path worldPath = worldDir.resolve("data").resolve("create_the_air_wars_tab_items.json");
                try {
                    Files.createDirectories(worldPath.getParent());
                    Files.writeString(worldPath, jsonText, StandardCharsets.UTF_8);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    public static Path getConfigPath() {
        return CONFIG_PATH;
    }

    private static String buildJsonText() {
        JsonObject root = new JsonObject();
        root.addProperty("layoutVersion", LAYOUT_VERSION);
        root.addProperty("columns", DEFAULT_COLUMNS);
        JsonArray slots = new JsonArray();
        for (ResourceLocation id : LAYOUT) {
            if (id == null) {
                slots.add((String) null);
            } else {
                slots.add(id.toString());
            }
        }
        root.add("slots", slots);
        return GSON.toJson(root);
    }

    private static void writeConfig(String jsonText) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, jsonText, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;

        if (!Files.exists(CONFIG_PATH)) {
            LAYOUT.addAll(getDefaultLayoutTemplate());
            save();
            return;
        }

        try {
            String json = Files.readString(CONFIG_PATH, StandardCharsets.UTF_8);
            JsonElement root = GSON.fromJson(json, JsonElement.class);
            if (root != null && root.isJsonObject()) {
                JsonObject obj = root.getAsJsonObject();
                int version = obj.has("layoutVersion") ? obj.get("layoutVersion").getAsInt() : 0;
                if (version < LAYOUT_VERSION) {
                    LAYOUT.clear();
                    LAYOUT.addAll(getDefaultLayoutTemplate());
                    save();
                    return;
                }
                JsonArray slots = obj.getAsJsonArray("slots");
                if (slots != null) {
                    for (JsonElement element : slots) {
                        if (element == null || element.isJsonNull()) {
                            LAYOUT.add(null);
                            continue;
                        }
                        ResourceLocation id = ResourceLocation.tryParse(element.getAsString());
                        if (id != null) {
                            LAYOUT.add(SPACER_ID.equals(id) ? null : id);
                        } else {
                            LAYOUT.add(null);
                        }
                    }
                }
            } else if (root != null && root.isJsonArray()) {
                // Backward compatibility with old format (plain item id array).
                JsonArray old = root.getAsJsonArray();
                for (JsonElement element : old) {
                    if (element != null && element.isJsonPrimitive()) {
                        ResourceLocation id = ResourceLocation.tryParse(element.getAsString());
                        if (id != null) {
                            LAYOUT.add(id);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        if (LAYOUT.isEmpty()) {
            LAYOUT.addAll(getDefaultLayoutTemplate());
            save();
        } else {
            setLayout(new ArrayList<>(LAYOUT));
            save();
        }
    }

    public static synchronized void populateCreativeTab(List<ItemStack> displayItems, Set<ItemStack> searchItems) {
        ensureLoaded();
        for (ResourceLocation id : LAYOUT) {
            if (id == null || SPACER_ID.equals(id)) {
                continue;
            }
            if (isRemovedItem(id)) {
                continue;
            }

            Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id);
            if (item == null || item == Items.AIR) {
                continue;
            }

            ItemStack stack = new ItemStack(item);
            displayItems.add(stack);
            searchItems.add(stack.copy());
        }
    }

    private static boolean isEligibleItem(ResourceLocation id) {
        if (SPACER_ID.equals(id)) {
            return false;
        }
        if (id.getPath().endsWith("_visual")) {
            return false;
        }
        if (isRemovedItem(id)) {
            return false;
        }
        return isEligibleNamespace(id);
    }

    private static boolean isRemovedItem(ResourceLocation id) {
        String path = id.getPath();
        return "m_24".equals(path)
            || "chick".equals(path)
            || "sonar".equals(path)
            || "binoculars".equals(path)
            || "drone_controller".equals(path)
            || "fpv_drone".equals(path)
            || "aim9xactive".equals(path)
            || "rim_7active".equals(path)
            || "ninek_119mactv".equals(path)
            || "c_25actv".equals(path)
            || "gdffgdgdg".equals(path)
            || "thtrue".equals(path)
            || "fab_3000trueblock".equals(path)
            || "test".equals(path)
            || "c_9emptyrocketbox".equals(path)
            || "c_9_rocketbox".equals(path)
            || "efssdfsdf".equals(path)
            || "dsfsdsf".equals(path)
            || "gtdfdgf".equals(path)
            || "shell".equals(path);
    }

    private static boolean isEligibleNamespace(ResourceLocation id) {
        String namespace = id.getNamespace();
        return CreateTheAirWarsMod.MODID.equals(namespace) || AIRWORK_NAMESPACE.equals(namespace);
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() == CreateTheAirWarsModItems.SPACER.get()) {
            event.getToolTip().clear();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPickup(net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Pre event) {
        if (event.getItemEntity().getItem().getItem() == CreateTheAirWarsModItems.SPACER.get()) {
            event.getItemEntity().discard();
            event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onToss(ItemTossEvent event) {
        if (event.getEntity().getItem().getItem() == CreateTheAirWarsModItems.SPACER.get()) {
            event.getEntity().discard();
            event.setCanceled(true);
        }
    }

    public static boolean isSpacerStack(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == CreateTheAirWarsModItems.SPACER.get();
    }

    public static boolean isSpacerId(ResourceLocation id) {
        return SPACER_ID.equals(id);
    }

    public static List<ResourceLocation> getDefaultLayoutTemplate() {
        LinkedHashSet<ResourceLocation> defaults = new LinkedHashSet<>();
        for (String entry : DEFAULT_TEMPLATE) {
            ResourceLocation id = ResourceLocation.tryParse(entry);
            if (id != null && isEligibleItem(id)) {
                defaults.add(id);
            }
        }
        defaults.addAll(getAllEligibleIds());
        return new ArrayList<>(defaults);
    }
}
