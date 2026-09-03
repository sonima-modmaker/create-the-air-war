package hi.client.gui;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import hi.block.entity.AntiAircraftLauncherBlockEntity;
import hi.network.AntiAircraftLauncherUpdatePacket;
import hi.world.inventory.AntiAircraftLauncherMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiAircraftLauncherScreen extends AbstractSimiContainerScreen<AntiAircraftLauncherMenu> {
    private final AntiAircraftLauncherBlockEntity blockEntity;
    private IconButton toggleModeButton;
    private IconButton plusButton;
    private IconButton removeButton;
    private IconButton saveButton;
    private SelectionScrollInput modScroll;
    private Label modLabel;
    private SelectionScrollInput entityScroll;
    private Label entityLabel;
    private IconButton addFilterButton;
    private IconButton cancelFilterButton;

    private boolean isEditingFilter = false;
    private boolean isBlacklist = true;
    private final List<String> currentTargets = new ArrayList<>();
    private final List<String> sortedMods = new ArrayList<>();
    private final Map<String, List<String>> modEntitiesMap = new HashMap<>();

    public AntiAircraftLauncherScreen(AntiAircraftLauncherMenu container, net.minecraft.world.entity.player.Inventory inventory, Component text) {
        super(container, inventory, text);
        if (container.getBlockEntity() instanceof AntiAircraftLauncherBlockEntity launcher) {
            this.blockEntity = launcher;
            this.isBlacklist = launcher.isBlacklist();
            this.currentTargets.addAll(launcher.getTargets());
        } else {
            this.blockEntity = null;
        }
        buildModAndEntitiesMap();
    }

    private void buildModAndEntitiesMap() {
        sortedMods.clear();
        modEntitiesMap.clear();

        sortedMods.add("sable");
        modEntitiesMap.put("sable", new ArrayList<>(List.of("ship")));

        sortedMods.add("players");
        List<String> playersList = new ArrayList<>();
        playersList.add("* (all players)");
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            mc.getConnection().getOnlinePlayers().forEach(info -> {
                String playerName = info.getProfile().getName();
                if (playerName != null && !playerName.isEmpty()) {
                    playersList.add(playerName);
                }
            });
        }
        modEntitiesMap.put("players", playersList);

        for (EntityType<?> type : net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation key = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (key == null) {
                continue;
            }
            modEntitiesMap.computeIfAbsent(key.getNamespace(), ignored -> new ArrayList<>()).add(key.getPath());
        }

        for (String mod : new ArrayList<>(modEntitiesMap.keySet())) {
            List<String> entities = modEntitiesMap.get(mod);
            Collections.sort(entities);
            if (!mod.equals("sable") && !mod.equals("players")) {
                sortedMods.add(mod);
                entities.add(0, "* (all entities)");
            }
        }

        Collections.sort(sortedMods, (a, b) -> {
            if (a.equals("sable")) return -1;
            if (b.equals("sable")) return 1;
            if (a.equals("players")) return -1;
            if (b.equals("players")) return 1;
            return a.compareTo(b);
        });
    }

    @Override
    protected void init() {
        setWindowSize(AllGuiTextures.SCHEDULE.getWidth(), AllGuiTextures.SCHEDULE.getHeight());
        super.init();
        clearWidgets();

        int w = AllGuiTextures.SCHEDULE.getWidth();
        int h = AllGuiTextures.SCHEDULE.getHeight();

        toggleModeButton = new IconButton(leftPos + 21, topPos + h - 30, isBlacklist ? AllIcons.I_BLACKLIST : AllIcons.I_WHITELIST);
        toggleModeButton.withCallback(() -> {
            isBlacklist = !isBlacklist;
            toggleModeButton.setIcon(isBlacklist ? AllIcons.I_BLACKLIST : AllIcons.I_WHITELIST);
        });
        addRenderableWidget(toggleModeButton);

        plusButton = new IconButton(leftPos + 46, topPos + h - 30, AllIcons.I_ADD);
        plusButton.withCallback(() -> {
            isEditingFilter = true;
            updateEditingPanelVisibility(true);
        });
        addRenderableWidget(plusButton);

        removeButton = new IconButton(leftPos + 71, topPos + h - 30, AllIcons.I_TRASH);
        removeButton.withCallback(() -> {
            if (!currentTargets.isEmpty()) {
                currentTargets.remove(currentTargets.size() - 1);
            }
        });
        addRenderableWidget(removeButton);

        saveButton = new IconButton(leftPos + w - 42, topPos + h - 30, AllIcons.I_CONFIRM);
        saveButton.withCallback(() -> {
            if (blockEntity != null) {
                PacketDistributor.sendToServer(new AntiAircraftLauncherUpdatePacket(blockEntity.getBlockPos(), isBlacklist, currentTargets));
            }
            onClose();
        });
        addRenderableWidget(saveButton);

        modLabel = new Label(leftPos + 36, topPos + 84, Component.literal("")).withShadow();
        modLabel.visible = false;
        addRenderableWidget(modLabel);

        modScroll = new SelectionScrollInput(leftPos + 33, topPos + 80, 120, 16);
        List<Component> modOptions = new ArrayList<>();
        for (String m : sortedMods) {
            modOptions.add(Component.literal(m));
        }
        modScroll.forOptions(modOptions);
        modScroll.titled(Component.literal("Choose mod"));
        modScroll.writingTo(modLabel);
        modScroll.calling(state -> {
            String selectedMod = sortedMods.get(state);
            List<String> entities = modEntitiesMap.get(selectedMod);
            List<Component> entityComponents = new ArrayList<>();
            for (String entity : entities) {
                entityComponents.add(Component.literal(entity));
            }
            entityScroll.forOptions(entityComponents);
            entityScroll.setState(0);
        });
        modScroll.visible = false;
        addRenderableWidget(modScroll);

        entityLabel = new Label(leftPos + 36, topPos + 124, Component.literal("")).withShadow();
        entityLabel.visible = false;
        addRenderableWidget(entityLabel);

        entityScroll = new SelectionScrollInput(leftPos + 33, topPos + 120, 120, 16);
        List<Component> entityOptions = new ArrayList<>();
        for (String entity : modEntitiesMap.get(sortedMods.get(0))) {
            entityOptions.add(Component.literal(entity));
        }
        entityScroll.forOptions(entityOptions);
        entityScroll.titled(Component.literal("Choose entity"));
        entityScroll.writingTo(entityLabel);
        entityScroll.visible = false;
        addRenderableWidget(entityScroll);

        addFilterButton = new IconButton(leftPos + w - 42, topPos + 80, AllIcons.I_CONFIRM);
        addFilterButton.withCallback(() -> {
            int modIdx = modScroll.getState();
            String mod = sortedMods.get(modIdx);

            int entIdx = entityScroll.getState();
            String entity = modEntitiesMap.get(mod).get(entIdx);

            if (mod.equals("sable")) {
                currentTargets.add("aeronautics");
            } else if (mod.equals("players")) {
                currentTargets.add(entity.startsWith("*") ? "players" : "players:" + entity);
            } else {
                currentTargets.add(entity.startsWith("*") ? mod : mod + ":" + entity);
            }

            isEditingFilter = false;
            updateEditingPanelVisibility(false);
        });
        addFilterButton.visible = false;
        addRenderableWidget(addFilterButton);

        cancelFilterButton = new IconButton(leftPos + w - 42, topPos + 110, AllIcons.I_DISABLE);
        cancelFilterButton.withCallback(() -> {
            isEditingFilter = false;
            updateEditingPanelVisibility(false);
        });
        cancelFilterButton.visible = false;
        addRenderableWidget(cancelFilterButton);
    }

    private void updateEditingPanelVisibility(boolean visible) {
        modScroll.visible = visible;
        modLabel.visible = visible;
        entityScroll.visible = visible;
        entityLabel.visible = visible;
        addFilterButton.visible = visible;
        cancelFilterButton.visible = visible;

        toggleModeButton.visible = !visible;
        plusButton.visible = !visible;
        removeButton.visible = !visible;
        saveButton.visible = !visible;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        AllGuiTextures.SCHEDULE.render(graphics, leftPos, topPos);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.literal("Air Defence Launcher"), 33, 21, 0xFFFFFF, false);

        if (blockEntity != null) {
            graphics.drawString(font, Component.literal("Rockets: " + blockEntity.getRockets() + "/2"), 33, 35, 0xFFCB74, false);
            graphics.drawString(font, Component.literal("Angle: " + (int) blockEntity.getDetectionAngle() + "°"), 118, 34, 0xFFCB74, false);
        }

        if (!isEditingFilter) {
            graphics.drawString(font, Component.literal(isBlacklist ? "Blacklist filters" : "Whitelist filters"), 33, 53, 0xFFFFFF, false);
            int yOffset = 67;
            for (int i = 0; i < Math.min(8, currentTargets.size()); i++) {
                graphics.drawString(font, Component.literal("- " + currentTargets.get(i)), 33, yOffset, 0xFFFFFF, false);
                yOffset += 13;
            }
            if (currentTargets.isEmpty()) {
                graphics.drawString(font, Component.literal("No filters. Mode applies to all targets."), 33, 67, 0xFFFFFF, false);
            }
        } else {
            graphics.drawString(font, Component.literal("Add filter"), 33, 53, 0xFFFFFF, false);
            graphics.drawString(font, Component.literal("Mod / Group"), 33, 69, 0xFFFFFF, false);
            graphics.drawString(font, Component.literal("Entity"), 33, 109, 0xFFFFFF, false);
        }
    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, b, c);
    }
}
