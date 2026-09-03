package hi.client.gui;

import hi.creative.CreativeTabContentManager;
import hi.init.CreateTheAirWarsModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CreativeTabEditorScreen extends Screen {
    private static final int LAYOUT_COLUMNS = 9;
    private static final int LAYOUT_ROWS = 15;
    private static final int LAYOUT_SLOT_COUNT = LAYOUT_COLUMNS * LAYOUT_ROWS;
    private static final int LAYOUT_VISIBLE_ROWS = 8;
    private static final int PALETTE_COLUMNS = 5;
    private static final int PALETTE_VISIBLE_ROWS = 8;
    private static final int SLOT_SIZE = 18;
    private static final int PANEL_PADDING = 12;
    private static final int PANEL_GAP = 14;
    private static final int PANEL_TOP = 42;
    private static final int PANEL_HEIGHT = 188;
    private static final int LEFT_PANEL_WIDTH = 108;
    private static final int RIGHT_PANEL_WIDTH = 186;
    private static final ResourceLocation SPACER_BRUSH_ID = ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "spacer");

    private final @Nullable Screen parent;
    private final List<ResourceLocation> allItems = new ArrayList<>();
    private final List<ResourceLocation> filteredItems = new ArrayList<>();
    private final List<ResourceLocation> layout = new ArrayList<>();
    private EditBox searchBox;
    private Button spacerButton;
    private Button saveButton;
    private Button resetButton;
    private Button doneButton;
    private int paletteScrollRow;
    private int layoutScrollRow;
    private boolean draggingPaint;
    private int dragButton = -1;
    private int lastPaintedIndex = -1;
    private @Nullable ResourceLocation brushId;
    private boolean dirty;
    private String statusLine = "Pick an item on the left, then paint slots on the right.";

    public CreativeTabEditorScreen(@Nullable Screen parent) {
        super(Component.translatable("screen.create_the_air_wars.tab_editor.title"));
        this.parent = parent;
        this.brushId = null;
        reloadData();
    }

    private void reloadData() {
        this.allItems.clear();
        this.allItems.addAll(CreativeTabContentManager.getAllEligibleIds());
        this.layout.clear();
        this.layout.addAll(CreativeTabContentManager.getLayout());
        while (this.layout.size() < LAYOUT_SLOT_COUNT) {
            this.layout.add(null);
        }
        if (this.layout.size() > LAYOUT_SLOT_COUNT) {
            this.layout.subList(LAYOUT_SLOT_COUNT, this.layout.size()).clear();
        }
        rebuildFilteredItems("");
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int leftPanelX = centerX - RIGHT_PANEL_WIDTH / 2 - PANEL_GAP - LEFT_PANEL_WIDTH;
        int top = PANEL_TOP;
        int buttonY = top + PANEL_HEIGHT + 10;

        this.searchBox = new EditBox(this.font, leftPanelX, top - 22, LEFT_PANEL_WIDTH, 18, Component.literal("Search"));
        this.searchBox.setHint(Component.literal("Search"));
        this.searchBox.setMaxLength(60);
        this.searchBox.setResponder(this::rebuildFilteredItems);
        this.addRenderableWidget(this.searchBox);
        setInitialFocus(this.searchBox);

        this.spacerButton = this.addRenderableWidget(
            Button.builder(Component.literal("Spacer"), button -> {
                    this.brushId = SPACER_BRUSH_ID;
                    this.statusLine = "Spacer brush selected.";
                })
                .pos(leftPanelX, buttonY)
                .size(LEFT_PANEL_WIDTH, 20)
                .build()
        );

        int actionsX = centerX - RIGHT_PANEL_WIDTH / 2;
        this.saveButton = this.addRenderableWidget(
            Button.builder(Component.translatable("screen.create_the_air_wars.tab_editor.save"), button -> saveLayout())
                .pos(actionsX, buttonY)
                .size(58, 20)
                .build()
        );
        this.resetButton = this.addRenderableWidget(
            Button.builder(Component.translatable("screen.create_the_air_wars.tab_editor.reset"), button -> resetLayout())
                .pos(actionsX + 64, buttonY)
                .size(58, 20)
                .build()
        );
        this.doneButton = this.addRenderableWidget(
            Button.builder(Component.literal("Done"), button -> onClose())
                .pos(actionsX + 128, buttonY)
                .size(58, 20)
                .build()
        );
    }

    private void rebuildFilteredItems(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        this.filteredItems.clear();
        this.filteredItems.addAll(this.allItems.stream()
            .filter(id -> matchesQuery(id, normalized))
            .sorted(Comparator.comparing(this::getDisplayName, String.CASE_INSENSITIVE_ORDER))
            .toList());

        int maxRow = Math.max(0, (this.filteredItems.size() - 1) / PALETTE_COLUMNS - (PALETTE_VISIBLE_ROWS - 1));
        this.paletteScrollRow = Math.min(this.paletteScrollRow, maxRow);
    }

    private boolean matchesQuery(ResourceLocation id, String query) {
        if (query.isEmpty()) {
            return true;
        }
        String displayName = getDisplayName(id).toLowerCase(Locale.ROOT);
        return id.toString().toLowerCase(Locale.ROOT).contains(query) || displayName.contains(query);
    }

    private String getDisplayName(ResourceLocation id) {
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null || item == Items.AIR) {
            return id.toString();
        }
        return item.getDefaultInstance().getHoverName().getString();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int leftPanelX = centerX - RIGHT_PANEL_WIDTH / 2 - PANEL_GAP - LEFT_PANEL_WIDTH;
        int rightPanelX = centerX - RIGHT_PANEL_WIDTH / 2;
        int top = PANEL_TOP;

        graphics.drawCenteredString(this.font, this.title, centerX, 12, 0xFFFFFF);
        graphics.drawString(this.font, "Palette", leftPanelX, top - 34, 0xE6D8B5, false);
        graphics.drawString(this.font, "Layout", rightPanelX, top - 34, 0xE6D8B5, false);
        graphics.drawString(this.font, "Brush", leftPanelX, top + PANEL_HEIGHT + 38, 0xCFCFCF, false);

        renderPanel(graphics, leftPanelX - PANEL_PADDING, top - PANEL_PADDING, LEFT_PANEL_WIDTH + PANEL_PADDING * 2, PANEL_HEIGHT + PANEL_PADDING * 2 + 52);
        renderPanel(graphics, rightPanelX - PANEL_PADDING, top - PANEL_PADDING, RIGHT_PANEL_WIDTH + PANEL_PADDING * 2, PANEL_HEIGHT + PANEL_PADDING * 2);

        renderPalette(graphics, leftPanelX, top, mouseX, mouseY);
        renderLayout(graphics, rightPanelX, top, mouseX, mouseY);
        renderBrushPreview(graphics, leftPanelX, top + PANEL_HEIGHT + 48);

        graphics.drawString(this.font, Component.literal(statusLine).withStyle(dirty ? ChatFormatting.GOLD : ChatFormatting.GRAY), leftPanelX, top + PANEL_HEIGHT + 74, 0xD6D6D6, false);
        graphics.drawString(this.font, "LMB place/paint  RMB spacer  MMB pick  Shift+LMB cut", leftPanelX, top + PANEL_HEIGHT + 88, 0x9FA8B3, false);

        renderHoveredTooltip(graphics, mouseX, mouseY, leftPanelX, rightPanelX, top);
    }

    private void renderPanel(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, 0xCC10151C);
        graphics.fill(x, y, x + w, y + 1, 0xFF4D657A);
        graphics.fill(x, y + h - 1, x + w, y + h, 0xFF2C3947);
        graphics.fill(x, y, x + 1, y + h, 0xFF2C3947);
        graphics.fill(x + w - 1, y, x + w, y + h, 0xFF2C3947);
    }

    private void renderPalette(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        int startIndex = this.paletteScrollRow * PALETTE_COLUMNS;
        int visibleSlots = PALETTE_COLUMNS * PALETTE_VISIBLE_ROWS;
        for (int i = 0; i < visibleSlots; i++) {
            int itemIndex = startIndex + i;
            int slotX = x + (i % PALETTE_COLUMNS) * SLOT_SIZE;
            int slotY = y + (i / PALETTE_COLUMNS) * SLOT_SIZE;
            renderSlotBackground(graphics, slotX, slotY, false);

            if (itemIndex >= this.filteredItems.size()) {
                continue;
            }

            ResourceLocation id = this.filteredItems.get(itemIndex);
            ItemStack stack = stackFor(id);
            graphics.renderItem(stack, slotX + 1, slotY + 1);
            if (id.equals(this.brushId)) {
                renderSelection(graphics, slotX, slotY, 0xFF74D2B6);
            }
        }

        renderScrollMarker(graphics, x + LEFT_PANEL_WIDTH + 4, y, PALETTE_VISIBLE_ROWS, getPaletteTotalRows(), this.paletteScrollRow);
    }

    private void renderLayout(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        int startRow = this.layoutScrollRow;
        for (int row = 0; row < LAYOUT_VISIBLE_ROWS; row++) {
            for (int col = 0; col < LAYOUT_COLUMNS; col++) {
                int layoutIndex = (startRow + row) * LAYOUT_COLUMNS + col;
                int slotX = x + col * SLOT_SIZE;
                int slotY = y + row * SLOT_SIZE;
                renderSlotBackground(graphics, slotX, slotY, true);
                if (layoutIndex >= this.layout.size()) {
                    continue;
                }

                ResourceLocation id = this.layout.get(layoutIndex);
                if (id == null) {
                    graphics.fill(slotX + 4, slotY + 4, slotX + 14, slotY + 14, 0x223A4654);
                    continue;
                }

                ItemStack stack = isSpacer(id) ? spacerStack() : stackFor(id);
                graphics.renderItem(stack, slotX + 1, slotY + 1);
                if (isSpacer(id)) {
                    graphics.fill(slotX + 2, slotY + 14, slotX + 16, slotY + 15, 0x80576644);
                }
            }
        }

        renderScrollMarker(graphics, x + RIGHT_PANEL_WIDTH + 4, y, LAYOUT_VISIBLE_ROWS, LAYOUT_ROWS, this.layoutScrollRow);
    }

    private void renderBrushPreview(GuiGraphics graphics, int x, int y) {
        renderSlotBackground(graphics, x, y, false);
        ItemStack brushStack = currentBrushStack();
        if (!brushStack.isEmpty()) {
            graphics.renderItem(brushStack, x + 1, y + 1);
        } else {
            graphics.fill(x + 4, y + 4, x + 14, y + 14, 0x223A4654);
        }
        String label = this.brushId == null ? "Empty" : isSpacer(this.brushId) ? "Spacer" : currentBrushStack().getHoverName().getString();
        graphics.drawString(this.font, label, x + SLOT_SIZE + 8, y + 5, 0xFFFFFF, false);
    }

    private void renderSlotBackground(GuiGraphics graphics, int x, int y, boolean highlight) {
        int fill = highlight ? 0xFF1A2330 : 0xFF141B24;
        graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, fill);
        graphics.fill(x, y, x + SLOT_SIZE, y + 1, 0xFF77889C);
        graphics.fill(x, y, x + 1, y + SLOT_SIZE, 0xFF77889C);
        graphics.fill(x + SLOT_SIZE - 1, y, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF3A4654);
        graphics.fill(x, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, 0xFF3A4654);
    }

    private void renderSelection(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x, y, x + SLOT_SIZE, y + 2, color);
        graphics.fill(x, y + SLOT_SIZE - 2, x + SLOT_SIZE, y + SLOT_SIZE, color);
        graphics.fill(x, y, x + 2, y + SLOT_SIZE, color);
        graphics.fill(x + SLOT_SIZE - 2, y, x + SLOT_SIZE, y + SLOT_SIZE, color);
    }

    private void renderScrollMarker(GuiGraphics graphics, int x, int y, int visibleRows, int totalRows, int scrollRow) {
        int trackHeight = visibleRows * SLOT_SIZE;
        graphics.fill(x, y, x + 4, y + trackHeight, 0xFF1A2028);
        if (totalRows <= visibleRows) {
            graphics.fill(x, y, x + 4, y + trackHeight, 0xFF6D8BA8);
            return;
        }

        int thumbHeight = Math.max(12, trackHeight * visibleRows / totalRows);
        int maxScroll = totalRows - visibleRows;
        int travel = trackHeight - thumbHeight;
        int thumbY = y + (int) Math.round((scrollRow / (double) maxScroll) * travel);
        graphics.fill(x, thumbY, x + 4, thumbY + thumbHeight, 0xFF6D8BA8);
    }

    private void renderHoveredTooltip(GuiGraphics graphics, int mouseX, int mouseY, int leftPanelX, int rightPanelX, int top) {
        ResourceLocation paletteId = getPaletteSlotAt(mouseX, mouseY, leftPanelX, top);
        if (paletteId != null) {
            graphics.renderTooltip(this.font, stackFor(paletteId), mouseX, mouseY);
            return;
        }

        int layoutIndex = getLayoutIndexAt(mouseX, mouseY, rightPanelX, top);
        if (layoutIndex >= 0 && layoutIndex < this.layout.size()) {
            ResourceLocation id = this.layout.get(layoutIndex);
            ItemStack stack = id == null ? ItemStack.EMPTY : isSpacer(id) ? spacerStack() : stackFor(id);
            List<Component> tooltip = new ArrayList<>();
            if (id == null) {
                tooltip.add(Component.literal("Empty slot"));
                tooltip.add(Component.literal("Nothing will be shown here").withStyle(ChatFormatting.GRAY));
            } else if (isSpacer(id)) {
                tooltip.add(spacerStack().getHoverName());
                tooltip.add(Component.literal("Gap / separator slot").withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(stack.getHoverName());
                tooltip.add(Component.literal(id.toString()).withStyle(ChatFormatting.DARK_GRAY));
            }
            graphics.renderTooltip(this.font, tooltip, stack.getTooltipImage(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int centerX = this.width / 2;
        int leftPanelX = centerX - RIGHT_PANEL_WIDTH / 2 - PANEL_GAP - LEFT_PANEL_WIDTH;
        int rightPanelX = centerX - RIGHT_PANEL_WIDTH / 2;
        int top = PANEL_TOP;

        ResourceLocation paletteId = getPaletteSlotAt(mouseX, mouseY, leftPanelX, top);
        if (paletteId != null) {
            this.brushId = paletteId;
            this.statusLine = "Brush: " + getDisplayName(paletteId);
            return true;
        }

        int layoutIndex = getLayoutIndexAt(mouseX, mouseY, rightPanelX, top);
        if (layoutIndex >= 0 && layoutIndex < this.layout.size()) {
            handleLayoutClick(layoutIndex, button);
            this.draggingPaint = button == 0 || button == 1;
            this.dragButton = button;
            this.lastPaintedIndex = layoutIndex;
            return true;
        }

        return false;
    }

    private void handleLayoutClick(int layoutIndex, int button) {
        ResourceLocation current = this.layout.get(layoutIndex);
        if (button == 2) {
            if (current != null) {
                this.brushId = current;
                this.statusLine = isSpacer(current) ? "Brush: Spacer" : "Brush: " + getDisplayName(current);
            } else {
                this.statusLine = "Empty slot picked. Brush unchanged.";
            }
            return;
        }

        if (button == 1) {
            setLayoutSlot(layoutIndex, SPACER_BRUSH_ID);
            return;
        }

        if (hasShiftDown() && current != null && !isSpacer(current)) {
            this.brushId = current;
            setLayoutSlot(layoutIndex, null);
            this.statusLine = "Cut " + getDisplayName(current) + " to brush.";
            return;
        }

        setLayoutSlot(layoutIndex, this.brushId);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        if (!this.draggingPaint || button != this.dragButton) {
            return false;
        }

        int centerX = this.width / 2;
        int rightPanelX = centerX - RIGHT_PANEL_WIDTH / 2;
        int top = PANEL_TOP;
        int layoutIndex = getLayoutIndexAt(mouseX, mouseY, rightPanelX, top);
        if (layoutIndex >= 0 && layoutIndex < this.layout.size() && layoutIndex != this.lastPaintedIndex) {
            if (button == 1) {
                setLayoutSlot(layoutIndex, SPACER_BRUSH_ID);
            } else if (button == 0) {
                setLayoutSlot(layoutIndex, this.brushId);
            }
            this.lastPaintedIndex = layoutIndex;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingPaint = false;
        this.dragButton = -1;
        this.lastPaintedIndex = -1;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
        int centerX = this.width / 2;
        int leftPanelX = centerX - RIGHT_PANEL_WIDTH / 2 - PANEL_GAP - LEFT_PANEL_WIDTH;
        int rightPanelX = centerX - RIGHT_PANEL_WIDTH / 2;
        int top = PANEL_TOP;

        if (isInside(mouseX, mouseY, leftPanelX, top, LEFT_PANEL_WIDTH, PALETTE_VISIBLE_ROWS * SLOT_SIZE)) {
            int maxRow = Math.max(0, getPaletteTotalRows() - PALETTE_VISIBLE_ROWS);
            this.paletteScrollRow = MthClamp.clamp(this.paletteScrollRow - Integer.signum((int) Math.round(scrollDeltaY)), 0, maxRow);
            return true;
        }

        if (isInside(mouseX, mouseY, rightPanelX, top, RIGHT_PANEL_WIDTH, LAYOUT_VISIBLE_ROWS * SLOT_SIZE)) {
            int maxRow = Math.max(0, LAYOUT_ROWS - LAYOUT_VISIBLE_ROWS);
            this.layoutScrollRow = MthClamp.clamp(this.layoutScrollRow - Integer.signum((int) Math.round(scrollDeltaY)), 0, maxRow);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
    }

    private void saveLayout() {
        CreativeTabContentManager.setLayout(this.layout);
        CreativeTabContentManager.save();
        CreativeTabContentManager.saveAllWorldCopies();
        CreativeTabContentManager.reloadFromDisk();
        this.dirty = false;
        this.statusLine = "Saved. Reopen the creative tab if it is already open.";
        refreshParentScreen();
    }

    private void resetLayout() {
        this.layout.clear();
        this.layout.addAll(CreativeTabContentManager.getDefaultLayoutTemplate());
        while (this.layout.size() < LAYOUT_SLOT_COUNT) {
            this.layout.add(null);
        }
        if (this.layout.size() > LAYOUT_SLOT_COUNT) {
            this.layout.subList(LAYOUT_SLOT_COUNT, this.layout.size()).clear();
        }
        this.dirty = true;
        this.statusLine = "Default layout loaded into the editor.";
    }

    private void refreshParentScreen() {
        Minecraft mc = Minecraft.getInstance();
        if (this.parent != null && mc != null) {
            this.parent.resize(mc, this.width, this.height);
        }
    }

    private void setLayoutSlot(int layoutIndex, @Nullable ResourceLocation id) {
        if (layoutIndex < 0 || layoutIndex >= this.layout.size()) {
            return;
        }
        this.layout.set(layoutIndex, id);
        this.dirty = true;
        if (id == null) {
            this.statusLine = "Cleared slot.";
        } else if (isSpacer(id)) {
            this.statusLine = "Placed spacer.";
        } else {
            this.statusLine = "Placed " + getDisplayName(id) + ".";
        }
    }

    private int getPaletteTotalRows() {
        return Math.max(1, (int) Math.ceil(this.filteredItems.size() / (double) PALETTE_COLUMNS));
    }

    private @Nullable ResourceLocation getPaletteSlotAt(double mouseX, double mouseY, int x, int y) {
        if (!isInside(mouseX, mouseY, x, y, LEFT_PANEL_WIDTH, PALETTE_VISIBLE_ROWS * SLOT_SIZE)) {
            return null;
        }
        int col = (int) ((mouseX - x) / SLOT_SIZE);
        int row = (int) ((mouseY - y) / SLOT_SIZE);
        if (col < 0 || col >= PALETTE_COLUMNS || row < 0 || row >= PALETTE_VISIBLE_ROWS) {
            return null;
        }
        int index = this.paletteScrollRow * PALETTE_COLUMNS + row * PALETTE_COLUMNS + col;
        if (index < 0 || index >= this.filteredItems.size()) {
            return null;
        }
        return this.filteredItems.get(index);
    }

    private int getLayoutIndexAt(double mouseX, double mouseY, int x, int y) {
        if (!isInside(mouseX, mouseY, x, y, RIGHT_PANEL_WIDTH, LAYOUT_VISIBLE_ROWS * SLOT_SIZE)) {
            return -1;
        }
        int col = (int) ((mouseX - x) / SLOT_SIZE);
        int row = (int) ((mouseY - y) / SLOT_SIZE);
        if (col < 0 || col >= LAYOUT_COLUMNS || row < 0 || row >= LAYOUT_VISIBLE_ROWS) {
            return -1;
        }
        return (this.layoutScrollRow + row) * LAYOUT_COLUMNS + col;
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private ItemStack currentBrushStack() {
        if (this.brushId == null) {
            return ItemStack.EMPTY;
        }
        return isSpacer(this.brushId) ? spacerStack() : stackFor(this.brushId);
    }

    private ItemStack stackFor(ResourceLocation id) {
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }

    private ItemStack spacerStack() {
        return new ItemStack(CreateTheAirWarsModItems.SPACER.get());
    }

    private boolean isSpacer(@Nullable ResourceLocation id) {
        return id != null && CreativeTabContentManager.isSpacerId(id);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        } else {
            super.onClose();
        }
    }

    private static final class MthClamp {
        private static int clamp(int value, int min, int max) {
            return Math.max(min, Math.min(max, value));
        }
    }
}
