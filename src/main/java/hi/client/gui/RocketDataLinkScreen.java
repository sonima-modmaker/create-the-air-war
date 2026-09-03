package hi.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import hi.block.entity.RocketDataLinkBlockEntity;
import hi.block.entity.RocketEngineBlockEntity;

import hi.network.RocketDataLinkPacket;
// import hi.registry.ModBlocks;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class RocketDataLinkScreen extends AbstractSimiScreen {
    private static final AllGuiTextures BG = AllGuiTextures.SCHEDULE;
    
    private final RocketDataLinkBlockEntity blockEntity;
    private IconButton startAllButton;
    private IconButton stopAllButton;
    private IconButton confirmButton;
    private float scrollOffset = 0;
    private float scrollTarget = 0;

    public RocketDataLinkScreen(RocketDataLinkBlockEntity be) {
        this.blockEntity = be;
    }

    @Override
    protected void init() {
        setWindowSize(BG.getWidth(), BG.getHeight());
        super.init();
        clearWidgets();

        int x = guiLeft;
        int y = guiTop;

        // Start all engines button
        startAllButton = new IconButton(x + 21, y + 196, AllIcons.I_PLAY);
        startAllButton.withCallback(() -> 
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(new hi.network.RocketDataLinkPacket(blockEntity.getBlockPos(), hi.network.RocketDataLinkPacket.Action.START_ALL))
        );
        addRenderableWidget(startAllButton);

        // Stop all engines button
        stopAllButton = new IconButton(x + 43, y + 196, AllIcons.I_PAUSE);
        stopAllButton.withCallback(() -> 
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(new hi.network.RocketDataLinkPacket(blockEntity.getBlockPos(), hi.network.RocketDataLinkPacket.Action.STOP_ALL))
        );
        addRenderableWidget(stopAllButton);

        // Confirm button
        confirmButton = new IconButton(x + BG.getWidth() - 42, y + BG.getHeight() - 30, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onClose);
        addRenderableWidget(confirmButton);
    }

    @Override
    public void tick() {
        super.tick();
        
        // Smooth scroll
        scrollOffset = Mth.lerp(0.5f, scrollOffset, scrollTarget);
        
        // Update button states
        boolean anyRunning = false;
        for (BlockPos pos : blockEntity.getLinkedEngines()) {
            if (minecraft.level == null) continue;
            BlockEntity be = minecraft.level.getBlockEntity(pos);
            if (be instanceof RocketEngineBlockEntity engine && 
                engine.getEngineState() == RocketEngineBlockEntity.EngineState.RUNNING) {
                anyRunning = true;
                break;
            }
        }
        stopAllButton.active = anyRunning;
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        // Background
        BG.render(graphics, x, y);

        // Title
        Component title = Component.translatable("block.create_the_air_wars.rocket_data_link");
        graphics.drawString(font, title, x + BG.getWidth() / 2 - font.width(title) / 2, y + 4, 0x592424, false);

        // Engine list with scissor
        graphics.enableScissor(x + 16, y + 16, x + 240, y + 189);
        renderEngineList(graphics, x, y, partialTicks);
        graphics.disableScissor();

        // Scroll shadows
        int zLevel = 200;
        graphics.fillGradient(x + 16, y + 16, x + 240, y + 26, zLevel, 0x77000000, 0x00000000);
        graphics.fillGradient(x + 16, y + 179, x + 240, y + 189, zLevel, 0x00000000, 0x77000000);

        // Engine count
        Component countText = Component.translatable("create_the_air_wars.rocket_data_link.engine_count", 
            blockEntity.getLinkedEngines().size());
        graphics.drawString(font, countText, x + 70, y + 200, 0x404040, false);

        // 3D block preview
        PoseStack ms = graphics.pose();
        ms.pushPose();
        TransformStack.of(ms)
            .translate(x + BG.getWidth() + 4, y + BG.getHeight() + 4, 100)
            .scale(40)
            .rotateXDegrees(-22)
            .rotateYDegrees(63);
        GuiGameElement.of(blockEntity.getBlockState())
            .render(graphics);
        ms.popPose();
    }

    private void renderEngineList(GuiGraphics graphics, int guiX, int guiY, float partialTicks) {
        List<BlockPos> engines = blockEntity.getLinkedEngines();
        PoseStack ms = graphics.pose();
        
        int yOffset = 25;
        float scroll = Mth.lerp(partialTicks, scrollOffset, scrollTarget);

        // Left strip
        UIRenderHelper.drawStretched(graphics, guiX + 33, guiY + 16, 3, 173, 200,
            AllGuiTextures.SCHEDULE_STRIP_DARK);

        ms.pushPose();
        ms.translate(0, -scroll, 0);

        // Light strip at top
        UIRenderHelper.drawStretched(graphics, guiX + 33, guiY + 16, 3, 10, -100,
            AllGuiTextures.SCHEDULE_STRIP_LIGHT);

        if (engines.isEmpty()) {
            // No engines message
            Component noEngines = Component.translatable("create_the_air_wars.rocket_data_link.no_engines");
            graphics.drawString(font, noEngines, guiX + BG.getWidth() / 2 - font.width(noEngines) / 2, 
                guiY + 80, 0x7A7A7A);
            AllGuiTextures.SCHEDULE_STRIP_END.render(graphics, guiX + 29, guiY + yOffset);
        } else {
            for (int i = 0; i < engines.size(); i++) {
                BlockPos enginePos = engines.get(i);
                int cardHeight = renderEngineCard(graphics, guiX, guiY + yOffset, enginePos, i + 1);
                yOffset += cardHeight;

                if (i + 1 < engines.size()) {
                    AllGuiTextures.SCHEDULE_STRIP_DOTTED.render(graphics, guiX + 29, guiY + yOffset - 3);
                    yOffset += 10;
                }
            }
            
            // End strip
            yOffset += 9;
            AllGuiTextures.SCHEDULE_STRIP_END.render(graphics, guiX + 29, guiY + yOffset);
        }

        ms.popPose();
    }

    private int renderEngineCard(GuiGraphics graphics, int guiX, int y, BlockPos pos, int index) {
        int cardWidth = 195;
        int cardHeight = 42;
        int x = guiX + 25;

        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(x, y, 0);

        // Card background
        AllGuiTextures light = AllGuiTextures.SCHEDULE_CARD_LIGHT;
        AllGuiTextures medium = AllGuiTextures.SCHEDULE_CARD_MEDIUM;
        AllGuiTextures dark = AllGuiTextures.SCHEDULE_CARD_DARK;

        UIRenderHelper.drawStretched(graphics, 0, 1, cardWidth, cardHeight - 2, 0, light);
        UIRenderHelper.drawStretched(graphics, 1, 0, cardWidth - 2, cardHeight, 0, light);
        UIRenderHelper.drawStretched(graphics, 1, 1, cardWidth - 2, cardHeight - 2, 0, dark);
        UIRenderHelper.drawStretched(graphics, 2, 2, cardWidth - 4, cardHeight - 4, 0, medium);

        // Left strip
        UIRenderHelper.drawStretched(graphics, 8, 0, 3, cardHeight + 10, 0, AllGuiTextures.SCHEDULE_STRIP_LIGHT);
        AllGuiTextures.SCHEDULE_STRIP_TRAVEL.render(graphics, 4, 10);

        if (minecraft.level == null) {
            ms.popPose();
            return cardHeight;
        }
        
        BlockEntity be = minecraft.level.getBlockEntity(pos);
        
        if (!(be instanceof RocketEngineBlockEntity engine)) {
            // Missing engine - show placeholder icon
            ms.pushPose();
            ms.translate(26, 21, 100);
            ms.scale(12, -12, 12);
            GuiGameElement.of(new ItemStack(hi.init.CreateTheAirWarsModBlocks.ROCKET_ENGINE.get()))
                .rotateBlock(30, 45, 0)
                .render(graphics);
            ms.popPose();
            
            graphics.drawString(font, "Engine #" + index, 46, 8, 0x7A7A7A, false);
            Component missing = Component.translatable("create_the_air_wars.rocket_data_link.engine_missing");
            graphics.drawString(font, missing, 46, 20, 0xF68989, false);
            ms.popPose();
            return cardHeight;
        }

        // Engine icon (3D preview) - proper positioning
        ms.pushPose();
        ms.translate(26, 21, 100);
        ms.scale(12, -12, 12);
        GuiGameElement.of(engine.getBlockState())
            .rotateBlock(30, 45, 0)
            .render(graphics);
        ms.popPose();

        // Engine header - moved right to not overlap with icon
        int textX = 46;
        graphics.drawString(font, "Engine #" + index, textX, 6, 0xE8E8E8, false);

        // Status with indicator dot
        String statusKey = switch (engine.getEngineState()) {
            case OFF -> "off";
            case STARTING -> "starting";
            case RUNNING -> "running";
            case STOPPING -> "stopping";
        };
        int statusColor = switch (engine.getEngineState()) {
            case OFF -> 0xF68989;
            case STARTING, STOPPING -> 0xF2C16D;
            case RUNNING -> 0x8AE388;
        };
        
        // Status indicator dot
        int dotColor = 0xFF000000 | statusColor;
        graphics.fill(textX, 18, textX + 4, 22, dotColor);
        
        Component status = Component.translatable("create_the_air_wars.rocket_data_link.status." + statusKey);
        graphics.drawString(font, status, textX + 7, 17, statusColor, false);

        // Fuel bar - positioned on the right side of card
        int fuelPercent = engine.getFuelCapacity() > 0 ? 
            (engine.getFuelAmount() * 100) / engine.getFuelCapacity() : 0;
        int barWidth = 50;
        int barX = cardWidth - barWidth - 8;
        int barY = 6;
        int barHeight = 10;
        
        // Bar background with border
        graphics.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, 0xFF1A1A1A);
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF2D2D2D);
        
        // Bar fill with gradient effect
        int filledWidth = (barWidth * fuelPercent) / 100;
        int fuelColor = fuelPercent > 50 ? 0xFF7ACC7A : fuelPercent > 20 ? 0xFFD4A84A : 0xFFCC5A5A;
        int fuelColorLight = fuelPercent > 50 ? 0xFF9AE89A : fuelPercent > 20 ? 0xFFF2C16D : 0xFFF68989;
        if (filledWidth > 0) {
            graphics.fill(barX, barY, barX + filledWidth, barY + barHeight / 2, fuelColorLight);
            graphics.fill(barX, barY + barHeight / 2, barX + filledWidth, barY + barHeight, fuelColor);
        }
        
        // Fuel percentage inside bar or next to it
        String fuelText = fuelPercent + "%";
        graphics.drawString(font, fuelText, barX + barWidth / 2 - font.width(fuelText) / 2, barY + 1, 0xFFFFFF, false);

        // Second row - thrust info
        int infoY = 28;
        if (engine.isThrottling()) {
            double thrust = Math.abs(engine.getAppliedThrust());
            graphics.drawString(font, String.format("%.0f N", thrust), textX, infoY, 0xF2C16D, false);
            
            int throttle = (int)(engine.getThrustPower() * 100);
            graphics.drawString(font, "Throttle: " + throttle + "%", barX - 10, infoY, 0x8AE388, false);
        } else if (engine.getEngineState() == RocketEngineBlockEntity.EngineState.RUNNING) {
            graphics.drawString(font, "Idle", textX, infoY, 0x6A6A6A, false);
        }

        ms.popPose();
        return cardHeight;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<BlockPos> engines = blockEntity.getLinkedEngines();
        int totalHeight = engines.size() * 52 + 50;
        int visibleHeight = 173;
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        
        scrollTarget = Mth.clamp(scrollTarget - (float)(scrollY * 20), 0, maxScroll);
        return true;
    }
}
