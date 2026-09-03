package hi.client.camera;

import hi.CreateTheAirWarsMod;
import hi.block.entity.MonitorBlockEntity;
import hi.network.CameraAdjustPacket;
import hi.network.CameraLockPacket;
import hi.network.VihrLaunchPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

@net.neoforged.fml.common.EventBusSubscriber(modid = CreateTheAirWarsMod.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public final class CameraMonitorClientHandler {
    private static final float ROTATION_STEP = 3.0F;
    private static final float DEFAULT_FOV = 70.0F;
    private static final float MIN_FOV = 12.0F;
    private static final float MAX_FOV = 90.0F;
    private static final float ZOOM_STEP = 4.0F;
    private static final Map<BlockPos, Float> CAMERA_FOVS = new HashMap<>();
    private static boolean registered;
    private static BlockPos activeMonitorPos;
    private static BlockPos activeCameraPos;
    private static boolean wasLockDown;
    private static boolean wasLaunchDown;
    private static boolean wasReloadDown;
    private static boolean wasThermalToggleDown;
    private static boolean thermalModeEnabled;

    private CameraMonitorClientHandler() {
    }

    public static void registerGameEvents() {
        if (registered) {
            return;
        }
        registered = true;
        NeoForge.EVENT_BUS.addListener(CameraMonitorClientHandler::onClientTick);
        NeoForge.EVENT_BUS.addListener(CameraMonitorClientHandler::onMouseScroll);
        NeoForge.EVENT_BUS.addListener(CameraMonitorClientHandler::onMovementInputUpdate);
        NeoForge.EVENT_BUS.addListener(CameraMonitorClientHandler::onInteractionKeyMapping);
    }

    public static float getFov(BlockPos cameraPos) {
        return CAMERA_FOVS.getOrDefault(cameraPos, DEFAULT_FOV);
    }

    public static void toggleAdjustmentMode(Level level, BlockPos monitorPos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || level == null) {
            return;
        }
        if (monitorPos.equals(activeMonitorPos)) {
            clearAdjustmentMode();
            minecraft.player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_monitor.adjustment_off").withStyle(ChatFormatting.YELLOW), true);
            return;
        }

        BlockEntity be = level.getBlockEntity(monitorPos);
        if (!(be instanceof MonitorBlockEntity monitor) || !monitor.hasLinkedCamera()) {
            minecraft.player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_monitor.no_link").withStyle(ChatFormatting.RED), true);
            return;
        }

        activeMonitorPos = monitorPos.immutable();
        activeCameraPos = monitor.getLinkedCameraPos().immutable();
        wasLaunchDown = false;
        wasReloadDown = GLFW.glfwGetMouseButton(minecraft.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        wasThermalToggleDown = false;
        minecraft.player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_monitor.adjustment_on").withStyle(ChatFormatting.GREEN), true);
    }

    public static boolean isThermalModeEnabled() {
        return thermalModeEnabled;
    }

    public static BlockPos getActiveCameraPos() {
        return activeCameraPos;
    }

    public static BlockPos getActiveMonitorPos() {
        return activeMonitorPos;
    }

    public static boolean isActiveCamera(BlockPos cameraPos) {
        return activeCameraPos != null && activeCameraPos.equals(cameraPos);
    }

    public static boolean hasActiveMonitor() {
        return activeMonitorPos != null && activeCameraPos != null;
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || activeCameraPos == null) {
            return;
        }
        if (!isActiveMonitorStillValid(minecraft)) {
            clearAdjustmentMode();
            return;
        }

        // Auto-disconnect if player is more than 5 blocks away from the monitor
        if (minecraft.player != null && activeMonitorPos != null) {
            double distSq = minecraft.player.distanceToSqr(
                activeMonitorPos.getX() + 0.5D,
                activeMonitorPos.getY() + 0.5D,
                activeMonitorPos.getZ() + 0.5D
            );
            if (distSq > 25.0D) {
                clearAdjustmentMode();
                minecraft.player.displayClientMessage(
                    Component.literal("Out of range / Слишком далеко от монитора")
                        .withStyle(ChatFormatting.RED),
                    true
                );
                return;
            }
        }
        if (minecraft.screen != null) {
            minecraft.setScreen(null);
        }

        blockVanillaKeyMappings(minecraft.options);
        pollCameraControls(minecraft);
    }

    private static void pollCameraControls(Minecraft minecraft) {
        long window = minecraft.getWindow().getWindow();
        float yawDelta = 0.0F;
        float pitchDelta = 0.0F;

        if (isKeyDown(window, GLFW.GLFW_KEY_LEFT) || isKeyDown(window, GLFW.GLFW_KEY_A)) {
            yawDelta -= ROTATION_STEP;
        }
        if (isKeyDown(window, GLFW.GLFW_KEY_RIGHT) || isKeyDown(window, GLFW.GLFW_KEY_D)) {
            yawDelta += ROTATION_STEP;
        }
        if (isKeyDown(window, GLFW.GLFW_KEY_UP) || isKeyDown(window, GLFW.GLFW_KEY_W)) {
            pitchDelta -= ROTATION_STEP;
        }
        if (isKeyDown(window, GLFW.GLFW_KEY_DOWN) || isKeyDown(window, GLFW.GLFW_KEY_S)) {
            pitchDelta += ROTATION_STEP;
        }

        if (yawDelta != 0.0F || pitchDelta != 0.0F) {
            PacketDistributor.sendToServer(new CameraAdjustPacket(activeCameraPos, yawDelta, pitchDelta));
        }

        boolean lockDown = isKeyDown(window, GLFW.GLFW_KEY_L);
        if (lockDown && !wasLockDown) {
            PacketDistributor.sendToServer(new CameraLockPacket(activeCameraPos));
        }
        wasLockDown = lockDown;

        boolean launchDown = isKeyDown(window, GLFW.GLFW_KEY_SPACE);
        if (launchDown && !wasLaunchDown && activeMonitorPos != null) {
            PacketDistributor.sendToServer(new VihrLaunchPacket(activeMonitorPos));
        }
        wasLaunchDown = launchDown;

        wasReloadDown = isKeyDown(window, GLFW.GLFW_KEY_R);

        boolean thermalToggleDown = isKeyDown(window, GLFW.GLFW_KEY_N);
        if (thermalToggleDown && !wasThermalToggleDown) {
            thermalModeEnabled = !thermalModeEnabled;
        }
        wasThermalToggleDown = thermalToggleDown;
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
        options.keyUse.setDown(false);
        options.keyPickItem.setDown(false);
        options.keyDrop.setDown(false);
        options.keySwapOffhand.setDown(false);
        options.keyInventory.setDown(false);
    }

    private static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        if (activeCameraPos == null) {
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
        if (activeCameraPos == null) {
            return;
        }
        if (event.isUseItem() && isLookingAtActiveMonitor()) {
            return;
        }
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    private static boolean isLookingAtActiveMonitor() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
            return hit.getBlockPos().equals(activeMonitorPos);
        }
        return false;
    }

    private static void clearAdjustmentMode() {
        if (activeCameraPos != null) {
            CAMERA_FOVS.remove(activeCameraPos);
        }
        activeMonitorPos = null;
        activeCameraPos = null;
        wasLockDown = false;
        wasLaunchDown = false;
        wasReloadDown = false;
        wasThermalToggleDown = false;
    }

    public static boolean isAdjustingMonitor(BlockPos monitorPos) {
        return monitorPos != null && monitorPos.equals(activeMonitorPos);
    }

    private static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.level == null || activeCameraPos == null) {
            return;
        }
        if (!isActiveMonitorStillValid(minecraft)) {
            activeMonitorPos = null;
            activeCameraPos = null;
            return;
        }

        float currentFov = getFov(activeCameraPos);
        float nextFov = Mth.clamp(currentFov - (float) event.getScrollDeltaY() * ZOOM_STEP, MIN_FOV, MAX_FOV);
        CAMERA_FOVS.put(activeCameraPos.immutable(), nextFov);
        if (minecraft.player != null) {
            int zoomPercent = Math.round(DEFAULT_FOV / nextFov * 100.0F);
            minecraft.player.displayClientMessage(Component.translatable("message.create_the_air_wars.camera_monitor.zoom", zoomPercent).withStyle(ChatFormatting.AQUA), true);
        }
        event.setCanceled(true);
    }

    private static boolean isActiveMonitorStillValid(Minecraft minecraft) {
        if (activeMonitorPos == null || minecraft.level == null) {
            return false;
        }
        BlockEntity be = minecraft.level.getBlockEntity(activeMonitorPos);
        if (!(be instanceof MonitorBlockEntity monitor) || !monitor.hasLinkedCamera()) {
            return false;
        }
        activeCameraPos = monitor.getLinkedCameraPos().immutable();
        return true;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        if (CameraFeedRenderer.isRenderingFeed()) {
            return;
        }
        CameraFeedTextureManager.renderRequestedFeeds();
    }
}
