package hi.client;

import hi.CreateTheAirWarsMod;
import hi.client.gui.CreativeTabEditorScreen;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@net.neoforged.fml.common.EventBusSubscriber(modid = CreateTheAirWarsMod.MODID, value = Dist.CLIENT, bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
public class CreativeTabEditorKeybinds {
    public static final KeyMapping OPEN_EDITOR = new KeyMapping(
        "key.create_the_air_wars.open_tab_editor",
        GLFW.GLFW_KEY_UNKNOWN,
        "key.categories.create_the_air_wars"
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        // Intentionally left unregistered: the tab editor should stay hidden from the R hotkey.
    }

    @net.neoforged.fml.common.EventBusSubscriber(modid = CreateTheAirWarsMod.MODID, value = Dist.CLIENT, bus = net.neoforged.fml.common.EventBusSubscriber.Bus.GAME)
    public static class ForgeHandlers {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Pre event) {
            return;
        }
    }
}
