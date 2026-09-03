package hi.client.creative;

import hi.CreateTheAirWarsMod;
import hi.creative.CreativeTabContentManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.lang.reflect.Field;

@net.neoforged.fml.common.EventBusSubscriber(modid = CreateTheAirWarsMod.MODID, value = Dist.CLIENT)
public final class CreativeTabSpacerClientHandler {
    private static final Field HOVERED_SLOT_FIELD = findHoveredSlotField();

    private CreativeTabSpacerClientHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCreativeTabClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() instanceof CreativeModeInventoryScreen creativeScreen && isSpacerHovered(creativeScreen)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCreativeTabScroll(ScreenEvent.MouseScrolled.Pre event) {
        if (event.getScreen() instanceof CreativeModeInventoryScreen creativeScreen && isSpacerHovered(creativeScreen)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCreativeTabKey(ScreenEvent.KeyPressed.Pre event) {
        if (!(event.getScreen() instanceof CreativeModeInventoryScreen creativeScreen) || !isSpacerHovered(creativeScreen)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        if (minecraft.options.keyPickItem.matches(event.getKeyCode(), event.getScanCode())) {
            event.setCanceled(true);
            return;
        }

        for (var hotbarKey : minecraft.options.keyHotbarSlots) {
            if (hotbarKey.matches(event.getKeyCode(), event.getScanCode())) {
                event.setCanceled(true);
                return;
            }
        }
    }

    private static boolean isSpacerHovered(CreativeModeInventoryScreen screen) {
        Slot hovered = getHoveredSlot(screen);
        return hovered != null && CreativeTabContentManager.isSpacerStack(hovered.getItem());
    }

    private static Slot getHoveredSlot(CreativeModeInventoryScreen screen) {
        if (HOVERED_SLOT_FIELD == null) {
            return null;
        }
        try {
            return (Slot) HOVERED_SLOT_FIELD.get(screen);
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    private static Field findHoveredSlotField() {
        try {
            Field field = AbstractContainerScreen.class.getDeclaredField("hoveredSlot");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
