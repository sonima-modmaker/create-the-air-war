package hi.client;

import hi.entity.FpvDroneEntity;
import hi.item.DroneControllerItem;
import hi.network.FpvDroneControlPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public final class DroneControllerClientHandler {
    private static double pendingMouseDx;
    private static double pendingMouseDy;
    private static float smoothedMouseYaw;
    private static float smoothedMousePitch;
    private static long controlStartGameTime = -1L;
    private static boolean fpvPostEffectActive;
    private static final ResourceLocation FPV_TV_EFFECT = ResourceLocation.withDefaultNamespace("shaders/post/tv.json");

    private DroneControllerClientHandler() {
    }

    public static void registerGameEvents() {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(DroneControllerClientHandler::onClientTick);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(DroneControllerClientHandler::onMovementInputUpdate);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(DroneControllerClientHandler::onInteractionKeyMapping);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(DroneControllerClientHandler::onRenderGui);
    }

    private static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        ItemStack controller = getHeldController(minecraft);
        if (!DroneControllerItem.isController(controller) || !DroneControllerItem.isControlling(controller)) {
            restoreCamera(minecraft);
            disableFpvPostEffect(minecraft);
            controlStartGameTime = -1L;
            pendingMouseDx = 0.0D;
            pendingMouseDy = 0.0D;
            smoothedMouseYaw = 0.0F;
            smoothedMousePitch = 0.0F;
            return;
        }
        UUID droneId = DroneControllerItem.getLinkedDroneId(controller);
        Entity entity = droneId != null ? findDrone(minecraft, droneId) : null;
        if (!(entity instanceof FpvDroneEntity drone)) {
            if (droneId != null) {
                PacketDistributor.sendToServer(new FpvDroneControlPacket(droneId, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
            }
            DroneControllerItem.clearDrone(controller);
            restoreCamera(minecraft);
            disableFpvPostEffect(minecraft);
            controlStartGameTime = -1L;
            return;
        }
        if (controlStartGameTime < 0L) {
            controlStartGameTime = minecraft.level.getGameTime();
        }
        if (minecraft.screen != null) {
            minecraft.setScreen(null);
        }
        if (minecraft.getCameraEntity() != drone) {
            minecraft.setCameraEntity(drone);
        }
        enableFpvPostEffect(minecraft);
        blockVanillaKeyMappings(minecraft.options);
        sendControls(minecraft, drone);
    }

    private static void sendControls(Minecraft minecraft, FpvDroneEntity drone) {
        long window = minecraft.getWindow().getWindow();
        double sensitivity = minecraft.options.sensitivity().get();
        boolean hasMouseInput = pendingMouseDx != 0.0D || pendingMouseDy != 0.0D;
        float rawMouseYaw = (float) (pendingMouseDx * sensitivity * 0.070F);
        float rawMousePitch = (float) (pendingMouseDy * sensitivity * 0.070F);
        if (minecraft.options.invertYMouse().get()) {
            rawMousePitch = -rawMousePitch;
        }
        pendingMouseDx = 0.0D;
        pendingMouseDy = 0.0D;
        smoothedMouseYaw = hasMouseInput ? Mth.lerp(0.22F, smoothedMouseYaw, rawMouseYaw) : 0.0F;
        smoothedMousePitch = hasMouseInput ? Mth.lerp(0.22F, smoothedMousePitch, rawMousePitch) : 0.0F;
        if (Math.abs(smoothedMouseYaw) < 0.001F) {
            smoothedMouseYaw = 0.0F;
        }
        if (Math.abs(smoothedMousePitch) < 0.001F) {
            smoothedMousePitch = 0.0F;
        }

        boolean shift = isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        boolean ctrl = isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) || isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
        float yaw = (isKeyDown(window, GLFW.GLFW_KEY_A) ? -1.35F : 0.0F) + (isKeyDown(window, GLFW.GLFW_KEY_D) ? 1.35F : 0.0F);
        float throttle = (ctrl ? 0.020F : 0.0F) + (shift ? -0.020F : 0.0F);

        if (smoothedMouseYaw != 0.0F || smoothedMousePitch != 0.0F || yaw != 0.0F || throttle != 0.0F) {
            PacketDistributor.sendToServer(new FpvDroneControlPacket(drone.getUUID(), smoothedMouseYaw, smoothedMousePitch, yaw, 0.0F, throttle));
        }
    }

    public static boolean captureMouseTurn(double dx, double dy) {
        if (!isControlling()) {
            return false;
        }
        pendingMouseDx += dx;
        pendingMouseDy += dy;
        return true;
    }

    private static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        if (!isControlling()) {
            return;
        }
        event.getInput().leftImpulse = 0.0F;
        event.getInput().forwardImpulse = 0.0F;
        event.getInput().up = false;
        event.getInput().down = false;
        event.getInput().left = false;
        event.getInput().right = false;
        event.getInput().jumping = false;
        event.getInput().shiftKeyDown = false;
    }

    private static void onInteractionKeyMapping(InputEvent.InteractionKeyMappingTriggered event) {
        if (!isControlling()) {
            return;
        }
        if (event.isUseItem()) {
            return;
        }
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isControlling() || minecraft.player == null) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int w = minecraft.getWindow().getGuiScaledWidth();
        int h = minecraft.getWindow().getGuiScaledHeight();
        long time = minecraft.level != null ? minecraft.level.getGameTime() : 0L;
        FpvDroneEntity drone = getActiveDrone(minecraft);
        float throttle = drone != null ? drone.getThrottle() : 0.0F;
        long elapsedTicks = controlStartGameTime >= 0L ? Math.max(0L, time - controlStartGameTime) : 0L;
        int minutes = (int) (elapsedTicks / 1200L);
        int seconds = (int) ((elapsedTicks / 20L) % 60L);
        float voltage = 5.35F - throttle * 0.42F - Math.min(0.75F, elapsedTicks / 12000.0F);

        graphics.fill(0, 0, w, h, 0x15000000);
        for (int y = (int) (time % 4L); y < h; y += 4) {
            graphics.fill(0, y, w, y + 1, 0x26000000);
        }
        int glitchY = Math.floorMod((int) (time * 7L), Math.max(1, h));
        int glitchHeight = 4 + (int) (Math.abs(Math.sin(time * 0.37D)) * 10.0D);
        int glitchOffset = (int) (Math.sin(time * 0.91D) * 18.0D);
        graphics.fill(Math.max(0, glitchOffset), glitchY, Math.min(w, w + glitchOffset), Math.min(h, glitchY + glitchHeight), 0x22D9D9D9);
        for (int i = 0; i < 34; i++) {
            int x = Math.floorMod((int) (time * 31L + i * 97L), Math.max(1, w));
            int y = Math.floorMod((int) (time * 17L + i * 53L), Math.max(1, h));
            int alpha = 22 + Math.floorMod((int) (time + i * 11L), 34);
            graphics.fill(x, y, Math.min(w, x + 2), Math.min(h, y + 1), (alpha << 24) | 0xDADADA);
        }
        int cx = w / 2;
        int cy = h / 2;
        int color = 0xFFE6E6E6;
        int shadow = 0x66000000;
        drawShadowText(graphics, minecraft, "4 1", 18, 18, color, shadow);
        drawShadowText(graphics, minecraft, "A I R", cx - 34, 18, color, shadow);
        drawShadowText(graphics, minecraft, String.format(java.util.Locale.ROOT, "%.2fV", voltage), w - 100, 18, color, shadow);
        drawOsdLadder(graphics, cx - 260, cy - 70, true, color);
        drawOsdLadder(graphics, cx + 260, cy - 70, false, color);
        graphics.hLine(cx - 18, cx - 5, cy, color);
        graphics.hLine(cx + 5, cx + 18, cy, color);
        graphics.vLine(cx, cy - 18, cy - 5, color);
        graphics.vLine(cx, cy + 5, cy + 18, color);
        int barW = Math.min(680, w - 48);
        int barX = (w - barW) / 2;
        int barY = h - 70;
        graphics.fill(barX, barY, barX + barW, barY + 48, 0x22000000);
        drawBorder(graphics, barX, barY, barX + barW, barY + 48, 0xFFE6E6E6);
        drawShadowText(graphics, minecraft, "FOXBEER", barX + 16, barY + 15, color, shadow);
        drawShadowText(graphics, minecraft, String.format(java.util.Locale.ROOT, "%04.1fV", voltage), cx - 28, barY + 15, color, shadow);
        drawShadowText(graphics, minecraft, String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds), barX + barW - 104, barY + 15, color, shadow);
        int throttleY = cy + 80 - Math.round(throttle * 160.0F);
        graphics.hLine(cx + 280, cx + 310, throttleY, 0xFFFFF0F0);
        drawShadowText(graphics, minecraft, String.format(java.util.Locale.ROOT, "%02d%%", Math.round(throttle * 100.0F)), cx + 318, throttleY - 4, color, shadow);
    }

    public static boolean isControllingFpv() {
        return isControlling();
    }

    private static boolean isControlling() {
        Minecraft minecraft = Minecraft.getInstance();
        return DroneControllerItem.isController(getHeldController(minecraft)) && DroneControllerItem.isControlling(getHeldController(minecraft));
    }

    private static FpvDroneEntity getActiveDrone(Minecraft minecraft) {
        ItemStack controller = getHeldController(minecraft);
        UUID droneId = DroneControllerItem.getLinkedDroneId(controller);
        Entity entity = droneId != null ? findDrone(minecraft, droneId) : null;
        return entity instanceof FpvDroneEntity drone ? drone : null;
    }

    private static ItemStack getHeldController(Minecraft minecraft) {
        if (minecraft.player == null) {
            return ItemStack.EMPTY;
        }
        ItemStack main = minecraft.player.getMainHandItem();
        if (DroneControllerItem.isController(main)) {
            return main;
        }
        ItemStack off = minecraft.player.getOffhandItem();
        return DroneControllerItem.isController(off) ? off : ItemStack.EMPTY;
    }

    private static Entity findDrone(Minecraft minecraft, UUID droneId) {
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (droneId.equals(entity.getUUID())) {
                return entity;
            }
        }
        return null;
    }

    private static void restoreCamera(Minecraft minecraft) {
        if (minecraft.player != null && minecraft.getCameraEntity() != minecraft.player) {
            minecraft.setCameraEntity(minecraft.player);
        }
    }

    private static void enableFpvPostEffect(Minecraft minecraft) {
        if (fpvPostEffectActive) {
            return;
        }
        minecraft.gameRenderer.loadEffect(FPV_TV_EFFECT);
        fpvPostEffectActive = true;
    }

    private static void disableFpvPostEffect(Minecraft minecraft) {
        if (!fpvPostEffectActive) {
            return;
        }
        minecraft.gameRenderer.shutdownEffect();
        fpvPostEffectActive = false;
    }

    private static void drawOsdLadder(GuiGraphics graphics, int x, int y, boolean left, int color) {
        for (int i = 0; i < 8; i++) {
            int yy = y + i * 22;
            int shortLen = i % 2 == 0 ? 34 : 20;
            if (left) {
                graphics.hLine(x, x + shortLen, yy, color);
                if (i == 4) {
                    graphics.drawString(Minecraft.getInstance().font, ">", x + 46, yy - 4, color);
                }
            } else {
                graphics.hLine(x - shortLen, x, yy, color);
                if (i == 4) {
                    graphics.drawString(Minecraft.getInstance().font, "<", x - 54, yy - 4, color);
                }
            }
        }
    }

    private static void drawBorder(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        graphics.hLine(x0, x1, y0, color);
        graphics.hLine(x0, x1, y1, color);
        graphics.vLine(x0, y0, y1, color);
        graphics.vLine(x1, y0, y1, color);
    }

    private static void drawShadowText(GuiGraphics graphics, Minecraft minecraft, String text, int x, int y, int color, int shadow) {
        graphics.drawString(minecraft.font, text, x + 2, y + 2, shadow);
        graphics.drawString(minecraft.font, text, x, y, color);
    }

    private static boolean isKeyDown(long window, int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    private static void blockVanillaKeyMappings(Options options) {
        options.keyUp.setDown(false);
        options.keyDown.setDown(false);
        options.keyLeft.setDown(false);
        options.keyRight.setDown(false);
        options.keyJump.setDown(false);
        options.keyShift.setDown(false);
        options.keySprint.setDown(false);
        options.keyAttack.setDown(false);
        options.keyPickItem.setDown(false);
        options.keyDrop.setDown(false);
        options.keySwapOffhand.setDown(false);
    }
}
