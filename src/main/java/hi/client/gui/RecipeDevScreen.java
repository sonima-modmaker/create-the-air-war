package hi.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hi.network.RecipeDevSavePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RecipeDevScreen extends Screen {
    private static final int PANEL_WIDTH = 640;
    private static final int PANEL_HEIGHT = 408;
    private static final int GRID_LIMIT = 9;
    private static final int CELL_SIZE = 18;
    private static final int GRID_START_X = 18;
    private static final int GRID_START_Y = 104;
    private static final int PALETTE_COLUMNS = 12;
    private static final int PALETTE_VISIBLE_ROWS = 2;
    private static final int SELECTED_SECTION_Y = 266;
    private static final int SELECTED_INPUT_Y = 278;
    private static final int PALETTE_SECTION_Y = 298;
    private static final int PALETTE_SEARCH_Y = 310;
    private static final int PALETTE_SCOPE_Y = 334;
    private static final int PALETTE_HINT_Y = 338;
    private static final int PALETTE_GRID_Y = 356;
    private static final int SYMBOL_PREVIEW_LIMIT = 7;
    private static final char[] SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final String[] gridValues = new String[GRID_LIMIT * GRID_LIMIT];
    private final List<ResourceLocation> allItems = new ArrayList<>();
    private final List<ResourceLocation> filteredItems = new ArrayList<>();

    private EditBox fileNameBox;
    private EditBox outputItemBox;
    private EditBox outputCountBox;
    private EditBox selectedIngredientBox;
    private EditBox paletteSearchBox;
    private Button typeButton;
    private Button rowsMinusButton;
    private Button rowsPlusButton;
    private Button colsMinusButton;
    private Button colsPlusButton;
    private Button mirrorButton;
    private Button heatButton;
    private Button clearCellButton;
    private Button clearGridButton;
    private Button saveButton;
    private Button closeButton;
    private Button paletteTargetButton;
    private Button paletteScopeButton;

    private RecipeMode mode = RecipeMode.SHAPED;
    private HeatRequirement heatRequirement = HeatRequirement.NONE;
    private PaletteTarget paletteTarget = PaletteTarget.CELL;
    private PaletteScope paletteScope = PaletteScope.COMMON;
    private boolean acceptMirrored = true;
    private int rows = 3;
    private int cols = 3;
    private int selectedCell = 0;
    private int paletteScrollRow = 0;
    private String lastAutoFileName = "new_recipe";
    private String status = "Click a cell, then click an item below. RMB item = output. Basin fluids: fluid:modid:name@250, fluidtag:c:milk@250.";

    public RecipeDevScreen() {
        super(Component.literal("CreateTheAirWars Recipe Dev UI"));
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        populateAllItems();

        int left = left();
        int top = top();
        fileNameBox = createBox(left + 18, top + 34, 132, "recipe file");
        fileNameBox.setValue("new_recipe");
        outputItemBox = createBox(left + 156, top + 34, 210, "output item");
        outputItemBox.setValue("create_the_air_wars:");
        outputCountBox = createBox(left + 372, top + 34, 52, "count");
        outputCountBox.setValue("1");
        outputItemBox.setResponder(this::onOutputItemChanged);
        selectedIngredientBox = createBox(left + 18, top + SELECTED_INPUT_Y, 210, "selected ingredient or #tag");
        selectedIngredientBox.setResponder(this::onSelectedIngredientChanged);
        paletteSearchBox = createBox(left + 18, top + PALETTE_SEARCH_Y, 176, "search items");
        paletteSearchBox.setResponder(this::onPaletteSearchChanged);

        typeButton = Button.builder(Component.empty(), button -> {
            mode = mode.next();
            clampGridToMode();
            refreshButtonState();
        }).bounds(left + 18, top + 58, 138, 20).build();
        rowsMinusButton = Button.builder(Component.literal("-"), button -> {
            rows = Math.max(mode.minRows, rows - 1);
            clampGridToMode();
            refreshButtonState();
        }).bounds(left + 198, top + 58, 20, 20).build();
        rowsPlusButton = Button.builder(Component.literal("+"), button -> {
            rows = Math.min(mode.maxRows, rows + 1);
            clampGridToMode();
            refreshButtonState();
        }).bounds(left + 258, top + 58, 20, 20).build();
        colsMinusButton = Button.builder(Component.literal("-"), button -> {
            cols = Math.max(mode.minCols, cols - 1);
            clampGridToMode();
            refreshButtonState();
        }).bounds(left + 330, top + 58, 20, 20).build();
        colsPlusButton = Button.builder(Component.literal("+"), button -> {
            cols = Math.min(mode.maxCols, cols + 1);
            clampGridToMode();
            refreshButtonState();
        }).bounds(left + 390, top + 58, 20, 20).build();
        mirrorButton = Button.builder(Component.empty(), button -> {
            acceptMirrored = !acceptMirrored;
            refreshButtonState();
        }).bounds(left + 18, top + 82, 170, 20).build();
        heatButton = Button.builder(Component.empty(), button -> {
            heatRequirement = heatRequirement.next();
            refreshButtonState();
        }).bounds(left + 196, top + 82, 170, 20).build();
        clearCellButton = Button.builder(Component.literal("Clear Cell"), button -> {
            gridValues[selectedCell] = "";
            syncSelectedIngredientField();
            updateStatus("Selected cell cleared");
        }).bounds(left + 236, top + SELECTED_INPUT_Y, 82, 20).build();
        clearGridButton = Button.builder(Component.literal("Clear Grid"), button -> {
            for (int i = 0; i < gridValues.length; i++) {
                gridValues[i] = "";
            }
            syncSelectedIngredientField();
            updateStatus("All ingredients cleared");
        }).bounds(left + 324, top + SELECTED_INPUT_Y, 82, 20).build();
        saveButton = Button.builder(Component.literal("Save JSON"), button -> saveRecipe()).bounds(left + 434, top + PALETTE_SEARCH_Y, 92, 20).build();
        closeButton = Button.builder(Component.literal("Close"), button -> onClose()).bounds(left + PANEL_WIDTH - 86, top + 16, 68, 20).build();
        paletteTargetButton = Button.builder(Component.empty(), button -> {
            paletteTarget = paletteTarget.next();
            refreshButtonState();
        }).bounds(left + 200, top + PALETTE_SEARCH_Y, 106, 20).build();
        paletteScopeButton = Button.builder(Component.empty(), button -> {
            paletteScope = paletteScope.next();
            rebuildFilteredItems(paletteSearchBox.getValue());
            refreshButtonState();
        }).bounds(left + 18, top + PALETTE_SCOPE_Y, 106, 20).build();

        addRenderableWidget(fileNameBox);
        addRenderableWidget(outputItemBox);
        addRenderableWidget(outputCountBox);
        addRenderableWidget(selectedIngredientBox);
        addRenderableWidget(paletteSearchBox);
        addRenderableWidget(typeButton);
        addRenderableWidget(rowsMinusButton);
        addRenderableWidget(rowsPlusButton);
        addRenderableWidget(colsMinusButton);
        addRenderableWidget(colsPlusButton);
        addRenderableWidget(mirrorButton);
        addRenderableWidget(heatButton);
        addRenderableWidget(clearCellButton);
        addRenderableWidget(clearGridButton);
        addRenderableWidget(saveButton);
        addRenderableWidget(closeButton);
        addRenderableWidget(paletteTargetButton);
        addRenderableWidget(paletteScopeButton);

        clampGridToMode();
        rebuildFilteredItems("");
        refreshButtonState();
        syncSelectedIngredientField();
        onOutputItemChanged(outputItemBox.getValue());
    }

    private EditBox createBox(int x, int y, int width, String hint) {
        EditBox box = new EditBox(this.font, x, y, width, 18, Component.literal(hint));
        box.setMaxLength(256);
        box.setBordered(true);
        return box;
    }

    private int left() {
        return (this.width - PANEL_WIDTH) / 2;
    }

    private int top() {
        return (this.height - PANEL_HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.fill(0, 0, this.width, this.height, 0xFF0A0F15);

        int left = left();
        int top = top();
        super.render(graphics, mouseX, mouseY, partialTicks);

        drawPanel(graphics, left + 8, top + 8, left + PANEL_WIDTH - 8, top + 96, 0xFF2E3743, 0xFF131920);
        drawPanel(graphics, left + 8, top + 100, left + 296, top + 254, 0xFF2E3743, 0xFF131920);
        drawPanel(graphics, left + 304, top + 100, left + PANEL_WIDTH - 8, top + 254, 0xFF2E3743, 0xFF131920);
        drawPanel(graphics, left + 8, top + 260, left + PANEL_WIDTH - 8, top + 348, 0xFF2E3743, 0xFF131920);
        drawPanel(graphics, left + 8, top + 352, left + PANEL_WIDTH - 8, top + PANEL_HEIGHT - 8, 0xFF2E3743, 0xFF131920);

        graphics.drawString(this.font, this.title, left + 18, top + 18, 0xE6EEF8, false);
        graphics.drawString(this.font, Component.literal("File"), left + 18, top + 24, 0x9FB3C8, false);
        graphics.drawString(this.font, Component.literal("Result Id"), left + 156, top + 24, 0x9FB3C8, false);
        graphics.drawString(this.font, Component.literal("Result Count / mB"), left + 372, top + 24, 0x9FB3C8, false);
        graphics.drawString(this.font, Component.literal("Mode"), left + 18, top + 48, 0x9FB3C8, false);
        graphics.drawString(this.font, Component.literal("Rows: " + rows), left + 224, top + 62, 0xE6EEF8, false);
        graphics.drawString(this.font, Component.literal("Cols: " + cols), left + 356, top + 62, 0xE6EEF8, false);
        graphics.drawString(this.font, Component.literal("Grid"), left + 18, top + 92, 0x9FB3C8, false);
        graphics.drawString(this.font, Component.literal("Selected cell"), left + 18, top + SELECTED_SECTION_Y, 0x9FB3C8, false);
        graphics.drawString(this.font, Component.literal("Palette"), left + 18, top + PALETTE_SECTION_Y, 0x9FB3C8, false);
        graphics.drawString(this.font, Component.literal("LMB palette = " + paletteTarget.label + " | RMB/Shift = result"), left + 132, top + PALETTE_HINT_Y, 0x9FB3C8, false);

        drawGrid(graphics, left + GRID_START_X, top + GRID_START_Y, mouseX, mouseY);
        renderOutputPreview(graphics, left + 452, top + 120);
        renderPalette(graphics, left + 18, top + PALETTE_GRID_Y, mouseX, mouseY);

        graphics.drawString(this.font, Component.literal(status), left + 18, top + PANEL_HEIGHT - 16, 0xC9D8E6, false);

        renderHoveredTooltip(graphics, mouseX, mouseY);
    }

    private void drawPanel(GuiGraphics graphics, int x1, int y1, int x2, int y2, int borderColor, int fillColor) {
        graphics.fill(x1, y1, x2, y2, borderColor);
        graphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0x00000000);
    }

    private void drawGrid(GuiGraphics graphics, int originX, int originY, int mouseX, int mouseY) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = row * GRID_LIMIT + col;
                int x = originX + col * (CELL_SIZE + 2);
                int y = originY + row * (CELL_SIZE + 2);
                boolean selected = index == selectedCell;
                boolean hovered = mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE;

                int border = selected ? 0xFFF0D77A : hovered ? 0xFF7FA4CC : 0xFF46566B;
                int fill = selected ? 0xFF2B3440 : 0xFF11171F;
                graphics.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, border);
                graphics.fill(x + 1, y + 1, x + CELL_SIZE - 1, y + CELL_SIZE - 1, fill);

                String ingredient = normalizedIngredient(gridValues[index]);
                ItemStack stack = stackForIngredient(ingredient);
                if (!stack.isEmpty()) {
                    graphics.renderItem(stack, x + 1, y + 1);
                } else if (!ingredient.isBlank()) {
                    graphics.drawString(this.font, shortenIngredient(ingredient), x + 2, y + 5, 0xE6EEF8, false);
                }
            }
        }
    }

    private void renderOutputPreview(GuiGraphics graphics, int x, int y) {
        graphics.drawString(this.font, Component.literal("Result"), x, y, 0x9FB3C8, false);
        renderSlotBackground(graphics, x, y + 12);
        if (paletteTarget == PaletteTarget.OUTPUT) {
            renderSelection(graphics, x, y + 12, 0xFFF0D77A);
        }
        ItemStack stack = stackForOutputPreview();
        if (!stack.isEmpty()) {
            graphics.renderItem(stack, x + 1, y + 13);
            graphics.renderItemDecorations(this.font, stack, x + 1, y + 13);
        }
        String outputId = outputItemBox == null ? "" : outputItemBox.getValue().trim();
        if (!outputId.isBlank()) {
            graphics.drawString(this.font, Component.literal(shortenPaletteLabel(outputId)), x + 24, y + 17, 0xE6EEF8, false);
        }
    }

    private void renderPalette(GuiGraphics graphics, int originX, int originY, int mouseX, int mouseY) {
        int startIndex = paletteScrollRow * PALETTE_COLUMNS;
        int visibleSlots = PALETTE_COLUMNS * PALETTE_VISIBLE_ROWS;
        for (int i = 0; i < visibleSlots; i++) {
            int itemIndex = startIndex + i;
            int slotX = originX + (i % PALETTE_COLUMNS) * CELL_SIZE;
            int slotY = originY + (i / PALETTE_COLUMNS) * CELL_SIZE;
            renderSlotBackground(graphics, slotX, slotY);

            if (itemIndex >= filteredItems.size()) {
                continue;
            }

            ResourceLocation id = filteredItems.get(itemIndex);
            ItemStack stack = stackFor(id);
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, slotX + 1, slotY + 1);
            }
            if (isSelectedPaletteItem(id)) {
                renderSelection(graphics, slotX, slotY, 0xFF74D2B6);
            }
        }

        renderScrollMarker(graphics, originX + PALETTE_COLUMNS * CELL_SIZE + 6, originY, PALETTE_VISIBLE_ROWS, getPaletteTotalRows(), paletteScrollRow);
    }

    private void renderSlotBackground(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, 0xFF11171F);
        graphics.fill(x, y, x + CELL_SIZE, y + 1, 0xFF77889C);
        graphics.fill(x, y, x + 1, y + CELL_SIZE, 0xFF77889C);
        graphics.fill(x + CELL_SIZE - 1, y, x + CELL_SIZE, y + CELL_SIZE, 0xFF3A4654);
        graphics.fill(x, y + CELL_SIZE - 1, x + CELL_SIZE, y + CELL_SIZE, 0xFF3A4654);
    }

    private void renderSelection(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x, y, x + CELL_SIZE, y + 2, color);
        graphics.fill(x, y + CELL_SIZE - 2, x + CELL_SIZE, y + CELL_SIZE, color);
        graphics.fill(x, y, x + 2, y + CELL_SIZE, color);
        graphics.fill(x + CELL_SIZE - 2, y, x + CELL_SIZE, y + CELL_SIZE, color);
    }

    private void renderScrollMarker(GuiGraphics graphics, int x, int y, int visibleRows, int totalRows, int scrollRow) {
        int trackHeight = visibleRows * CELL_SIZE;
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

    private void renderHoveredTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        ResourceLocation paletteId = getPaletteSlotAt(mouseX, mouseY);
        if (paletteId != null) {
            ItemStack stack = stackFor(paletteId);
            if (!stack.isEmpty()) {
                graphics.renderTooltip(this.font, stack, mouseX, mouseY);
            } else {
                graphics.renderTooltip(this.font, Component.literal(paletteId.toString()), mouseX, mouseY);
            }
            return;
        }

        int gridIndex = getGridIndexAt(mouseX, mouseY);
        if (gridIndex >= 0) {
            String ingredient = normalizedIngredient(gridValues[gridIndex]);
            if (!ingredient.isBlank()) {
                ItemStack stack = stackForIngredient(ingredient);
                if (!stack.isEmpty()) {
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(stack.getHoverName());
                    tooltip.add(Component.literal(ingredient).withStyle(ChatFormatting.DARK_GRAY));
                    graphics.renderTooltip(this.font, tooltip, stack.getTooltipImage(), mouseX, mouseY);
                } else {
                    graphics.renderTooltip(this.font, Component.literal(ingredient), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        ResourceLocation paletteId = getPaletteSlotAt(mouseX, mouseY);
        if (paletteId != null) {
            boolean forceOutput = button == 1 || hasShiftDown();
            applyPaletteItem(paletteId, forceOutput);
            return true;
        }

        if (isMouseOverOutputSlot(mouseX, mouseY)) {
            paletteTarget = PaletteTarget.OUTPUT;
            refreshButtonState();
            updateStatus("Result slot selected");
            return true;
        }

        int gridIndex = getGridIndexAt(mouseX, mouseY);
        if (gridIndex >= 0) {
            selectedCell = gridIndex;
            paletteTarget = PaletteTarget.CELL;
            refreshButtonState();
            syncSelectedIngredientField();
            updateStatus("Selected cell " + (selectedCell % GRID_LIMIT + 1) + "," + (selectedCell / GRID_LIMIT + 1));
            return true;
        }

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
        if (isMouseOverPalette(mouseX, mouseY)) {
            int maxRow = Math.max(0, getPaletteTotalRows() - PALETTE_VISIBLE_ROWS);
            if (scrollDeltaY > 0) {
                paletteScrollRow = Math.max(0, paletteScrollRow - 1);
            } else if (scrollDeltaY < 0) {
                paletteScrollRow = Math.min(maxRow, paletteScrollRow + 1);
            }
            return true;
        }
        super.mouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    private void onSelectedIngredientChanged(String value) {
        if (selectedCell < 0 || selectedCell >= gridValues.length) {
            return;
        }
        gridValues[selectedCell] = value.trim();
    }

    private void onPaletteSearchChanged(String query) {
        rebuildFilteredItems(query);
    }

    private void onOutputItemChanged(String value) {
        if (fileNameBox == null) {
            return;
        }
        String currentFileName = fileNameBox.getValue().trim();
        String autoFileName = deriveRecipeFileName(value);
        if (currentFileName.isBlank() || currentFileName.equals("new_recipe") || currentFileName.equals(lastAutoFileName)) {
            fileNameBox.setValue(autoFileName);
        }
        lastAutoFileName = autoFileName;
    }

    private void syncSelectedIngredientField() {
        if (selectedCell < 0 || selectedCell >= gridValues.length || selectedIngredientBox == null) {
            return;
        }
        selectedIngredientBox.setValue(gridValues[selectedCell] == null ? "" : gridValues[selectedCell]);
    }

    private void refreshButtonState() {
        typeButton.setMessage(Component.literal("Mode: " + mode.shortLabel));
        mirrorButton.visible = mode == RecipeMode.MECHANICAL;
        mirrorButton.active = mode == RecipeMode.MECHANICAL;
        mirrorButton.setMessage(Component.literal("Mirrored: " + (acceptMirrored ? "on" : "off")));
        heatButton.visible = mode == RecipeMode.MIXING || mode == RecipeMode.COMPACTING;
        heatButton.active = mode == RecipeMode.MIXING || mode == RecipeMode.COMPACTING;
        heatButton.setMessage(Component.literal("Heat: " + heatRequirement.label));
        rowsMinusButton.active = rows > mode.minRows;
        rowsPlusButton.active = rows < mode.maxRows;
        colsMinusButton.active = cols > mode.minCols;
        colsPlusButton.active = cols < mode.maxCols;
        paletteTargetButton.setMessage(Component.literal("Palette -> " + paletteTarget.label));
        paletteScopeButton.setMessage(Component.literal("Items: " + paletteScope.label));
    }

    private void clampGridToMode() {
        rows = Math.max(mode.minRows, Math.min(mode.maxRows, rows));
        cols = Math.max(mode.minCols, Math.min(mode.maxCols, cols));
        if (mode == RecipeMode.PRESSING || mode == RecipeMode.CRUSHING) {
            rows = 1;
            cols = 1;
        }
        if (selectedCell / GRID_LIMIT >= rows || selectedCell % GRID_LIMIT >= cols) {
            selectedCell = 0;
        }
    }

    private void saveRecipe() {
        try {
            RecipeJsonData data = buildRecipeJson();
            PacketDistributor.sendToServer(new RecipeDevSavePacket(data.folder(), data.fileName(), data.json()));
            updateStatus("Saved draft to debug_recipes/" + data.folder() + "/" + data.fileName() + ".json");
        } catch (IllegalArgumentException exception) {
            updateStatus(ChatFormatting.RED + exception.getMessage());
        }
    }

    private RecipeJsonData buildRecipeJson() {
        String fileName = fileNameBox.getValue().trim();
        if (fileName.isBlank()) {
            throw new IllegalArgumentException("Recipe file name is required");
        }

        String outputItem = outputItemBox.getValue().trim();
        if (outputItem.isBlank()) {
            throw new IllegalArgumentException("Output item id is required");
        }

        int outputCount;
        try {
            outputCount = Math.max(1, Integer.parseInt(outputCountBox.getValue().trim()));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Output count must be a number");
        }

        JsonObject root = new JsonObject();
        root.addProperty("type", mode.typeId);

        switch (mode) {
            case SHAPED -> buildShaped(root, outputItem, outputCount);
            case SHAPELESS -> buildShapeless(root, outputItem, outputCount);
            case PRESSING -> buildPressing(root, outputItem, outputCount);
            case CRUSHING -> buildCrushing(root, outputItem, outputCount);
            case MIXING -> buildMixing(root, outputItem, outputCount);
            case COMPACTING -> buildCompacting(root, outputItem, outputCount);
            case MECHANICAL -> buildMechanical(root, outputItem, outputCount);
        }

        return new RecipeJsonData(mode.folder, fileName, GSON.toJson(root));
    }

    private void buildShaped(JsonObject root, String outputItem, int outputCount) {
        root.addProperty("category", "misc");
        List<String> pattern = buildPatternLines();
        root.add("pattern", toJsonArray(pattern));
        root.add("key", buildKey(pattern));
        root.add("result", buildResult(outputItem, outputCount));
    }

    private void buildMechanical(JsonObject root, String outputItem, int outputCount) {
        root.addProperty("accept_mirrored", acceptMirrored);
        root.addProperty("category", "misc");
        List<String> pattern = buildPatternLines();
        root.add("pattern", toJsonArray(pattern));
        root.add("key", buildKey(pattern));
        root.add("result", buildResult(outputItem, outputCount));
    }

    private void buildShapeless(JsonObject root, String outputItem, int outputCount) {
        JsonArray ingredients = buildIngredientList(false);
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Add at least one ingredient");
        }
        root.addProperty("category", "misc");
        root.add("ingredients", ingredients);
        root.add("result", buildResult(outputItem, outputCount));
    }

    private void buildPressing(JsonObject root, String outputItem, int outputCount) {
        JsonArray ingredients = buildIngredientList(true);
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Pressing needs one ingredient");
        }
        JsonArray results = new JsonArray();
        results.add(buildProcessingResult(outputItem, outputCount));
        root.add("ingredients", ingredients);
        root.add("results", results);
    }

    private void buildCrushing(JsonObject root, String outputItem, int outputCount) {
        JsonArray ingredients = buildIngredientList(true);
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Crushing needs one ingredient");
        }
        root.addProperty("processingTime", 250);
        JsonArray results = new JsonArray();
        results.add(buildCrushingResult(outputItem, outputCount));
        root.add("ingredients", ingredients);
        root.add("results", results);
    }

    private void buildMixing(JsonObject root, String outputItem, int outputCount) {
        JsonArray ingredients = buildIngredientList(false, true);
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Mixing needs at least one ingredient");
        }
        if (heatRequirement != HeatRequirement.NONE) {
            root.addProperty("heat_requirement", heatRequirement.jsonValue);
        }
        root.add("ingredients", ingredients);
        root.add("results", buildProcessingResults(outputItem, outputCount, true));
    }

    private void buildCompacting(JsonObject root, String outputItem, int outputCount) {
        JsonArray ingredients = buildIngredientList(false, true);
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Compacting needs at least one ingredient");
        }
        if (heatRequirement != HeatRequirement.NONE) {
            root.addProperty("heat_requirement", heatRequirement.jsonValue);
        }
        root.add("ingredients", ingredients);
        root.add("results", buildProcessingResults(outputItem, outputCount, true));
    }

    private List<String> buildPatternLines() {
        boolean hasIngredient = false;
        List<String> lines = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < cols; col++) {
                String ingredient = normalizedIngredient(gridValues[row * GRID_LIMIT + col]);
                if (!ingredient.isBlank()) {
                    hasIngredient = true;
                }
                line.append(ingredient.isBlank() ? ' ' : '\u0001');
            }
            lines.add(line.toString());
        }
        if (!hasIngredient) {
            throw new IllegalArgumentException("Add at least one ingredient");
        }
        return remapPattern();
    }

    private List<String> remapPattern() {
        Map<String, Character> symbols = new LinkedHashMap<>();
        int symbolIndex = 0;
        List<String> result = new ArrayList<>();

        for (int row = 0; row < rows; row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < cols; col++) {
                String ingredient = normalizedIngredient(gridValues[row * GRID_LIMIT + col]);
                if (ingredient.isBlank()) {
                    line.append(' ');
                    continue;
                }
                Character symbol = symbols.get(ingredient);
                if (symbol == null) {
                    if (symbolIndex >= SYMBOLS.length) {
                        throw new IllegalArgumentException("Too many unique ingredients for one recipe");
                    }
                    symbol = SYMBOLS[symbolIndex++];
                    symbols.put(ingredient, symbol);
                }
                line.append(symbol);
            }
            result.add(line.toString());
        }
        return result;
    }

    private JsonObject buildKey(List<String> pattern) {
        Set<Character> used = new LinkedHashSet<>();
        for (String line : pattern) {
            for (int i = 0; i < line.length(); i++) {
                char symbol = line.charAt(i);
                if (symbol != ' ') {
                    used.add(symbol);
                }
            }
        }

        Map<String, Character> symbolMap = new LinkedHashMap<>();
        int symbolIndex = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                String ingredient = normalizedIngredient(gridValues[row * GRID_LIMIT + col]);
                if (ingredient.isBlank() || symbolMap.containsKey(ingredient)) {
                    continue;
                }
                if (symbolIndex >= SYMBOLS.length) {
                    throw new IllegalArgumentException("Too many unique ingredients for one recipe");
                }
                symbolMap.put(ingredient, SYMBOLS[symbolIndex++]);
            }
        }

        JsonObject key = new JsonObject();
        for (Map.Entry<String, Character> entry : symbolMap.entrySet()) {
            if (used.contains(entry.getValue())) {
                key.add(String.valueOf(entry.getValue()), ingredientToJson(entry.getKey(), false));
            }
        }
        return key;
    }

    private JsonArray buildIngredientList(boolean singleOnly) {
        return buildIngredientList(singleOnly, false);
    }

    private JsonArray buildIngredientList(boolean singleOnly, boolean allowFluids) {
        JsonArray ingredients = new JsonArray();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                String ingredient = normalizedIngredient(gridValues[row * GRID_LIMIT + col]);
                if (ingredient.isBlank()) {
                    continue;
                }
                ingredients.add(ingredientToJson(ingredient, allowFluids));
                if (singleOnly) {
                    return ingredients;
                }
            }
        }
        return ingredients;
    }

    private JsonObject ingredientToJson(String ingredient, boolean allowFluids) {
        JsonObject json = new JsonObject();
        if (allowFluids && ingredient.startsWith("fluidtag:")) {
            ParsedFluidIngredient fluid = parseFluidIngredient(ingredient, true);
            json.addProperty("type", "neoforge:tag");
            json.addProperty("amount", fluid.amount());
            json.addProperty("tag", fluid.id());
            return json;
        }
        if (allowFluids && ingredient.startsWith("fluid:")) {
            ParsedFluidIngredient fluid = parseFluidIngredient(ingredient, false);
            json.addProperty("type", "neoforge:single");
            json.addProperty("amount", fluid.amount());
            json.addProperty("fluid", fluid.id());
            return json;
        }
        if (!allowFluids && (ingredient.startsWith("fluid:") || ingredient.startsWith("fluidtag:"))) {
            throw new IllegalArgumentException("Fluids are only supported in Create Mixing / Compacting");
        }
        if (ingredient.startsWith("#")) {
            json.addProperty("tag", ingredient.substring(1));
        } else {
            json.addProperty("item", ingredient);
        }
        return json;
    }

    private JsonObject buildResult(String outputItem, int outputCount) {
        if (outputItem.startsWith("fluid:") || outputItem.startsWith("fluidtag:")) {
            throw new IllegalArgumentException("Fluid output is only supported in Create Mixing / Compacting");
        }
        JsonObject result = new JsonObject();
        result.addProperty("id", outputItem);
        result.addProperty("count", outputCount);
        return result;
    }

    private JsonObject buildProcessingResult(String outputItem, int outputCount) {
        if (outputItem.startsWith("fluid:") || outputItem.startsWith("fluidtag:")) {
            throw new IllegalArgumentException("Fluid output must use basin recipe handling");
        }
        JsonObject result = new JsonObject();
        result.addProperty("id", outputItem);
        if (outputCount > 1) {
            result.addProperty("count", outputCount);
        }
        return result;
    }

    private JsonObject buildCrushingResult(String outputItem, int outputCount) {
        if (outputItem.startsWith("fluid:") || outputItem.startsWith("fluidtag:")) {
            throw new IllegalArgumentException("Crushing output must be an item");
        }
        JsonObject result = new JsonObject();
        result.addProperty("item", outputItem);
        if (outputCount > 1) {
            result.addProperty("count", outputCount);
        }
        return result;
    }

    private JsonArray buildProcessingResults(String outputItem, int outputCount, boolean allowFluidOutput) {
        JsonArray results = new JsonArray();
        if (allowFluidOutput && outputItem.startsWith("fluid:")) {
            String fluidId = outputItem.substring("fluid:".length()).trim();
            int atIndex = fluidId.lastIndexOf('@');
            if (atIndex > 0) {
                fluidId = fluidId.substring(0, atIndex).trim();
            }
            JsonObject fluidResult = new JsonObject();
            fluidResult.addProperty("id", fluidId);
            fluidResult.addProperty("amount", outputCount);
            results.add(fluidResult);
            return results;
        }
        if (allowFluidOutput && outputItem.startsWith("fluidtag:")) {
            throw new IllegalArgumentException("Fluid result must be one exact fluid, not a fluid tag");
        }
        results.add(buildProcessingResult(outputItem, outputCount));
        return results;
    }

    private JsonArray toJsonArray(List<String> lines) {
        JsonArray array = new JsonArray();
        for (String line : lines) {
            array.add(line);
        }
        return array;
    }

    private String normalizedIngredient(String input) {
        return input == null ? "" : input.trim();
    }

    private void updateStatus(String text) {
        this.status = text;
    }

    private void populateAllItems() {
        if (!allItems.isEmpty()) {
            return;
        }
        List<ResourceLocation> ids = new ArrayList<>();
        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item != null && item != Items.AIR) {
                ids.add(id);
            }
        }
        ids.sort(Comparator
            .comparingInt(this::namespacePriority)
            .thenComparing(this::getDisplayName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(ResourceLocation::toString));
        allItems.addAll(ids);
    }

    private int namespacePriority(ResourceLocation id) {
        return switch (id.getNamespace()) {
            case "create_the_air_wars" -> 0;
            case "create" -> 1;
            case "minecraft" -> 2;
            default -> 3;
        };
    }

    private void rebuildFilteredItems(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        filteredItems.clear();
        for (ResourceLocation id : allItems) {
            if (!paletteScope.allows(id)) {
                continue;
            }
            if (matchesQuery(id, normalized)) {
                filteredItems.add(id);
            }
        }
        int maxRow = Math.max(0, getPaletteTotalRows() - PALETTE_VISIBLE_ROWS);
        paletteScrollRow = Math.min(paletteScrollRow, maxRow);
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
        return new ItemStack(item).getHoverName().getString();
    }

    private int getPaletteTotalRows() {
        return Math.max(1, (int) Math.ceil(filteredItems.size() / (double) PALETTE_COLUMNS));
    }

    private ResourceLocation getPaletteSlotAt(double mouseX, double mouseY) {
        int originX = left() + 18;
        int originY = top() + PALETTE_GRID_Y;
        if (mouseX < originX || mouseY < originY) {
            return null;
        }

        int localX = (int) mouseX - originX;
        int localY = (int) mouseY - originY;
        int col = localX / CELL_SIZE;
        int row = localY / CELL_SIZE;
        if (col < 0 || col >= PALETTE_COLUMNS || row < 0 || row >= PALETTE_VISIBLE_ROWS) {
            return null;
        }

        int index = paletteScrollRow * PALETTE_COLUMNS + row * PALETTE_COLUMNS + col;
        if (index < 0 || index >= filteredItems.size()) {
            return null;
        }
        return filteredItems.get(index);
    }

    private boolean isMouseOverOutputSlot(double mouseX, double mouseY) {
        int x = left() + 452;
        int y = top() + 132;
        return mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE;
    }

    private boolean isMouseOverPalette(double mouseX, double mouseY) {
        int originX = left() + 18;
        int originY = top() + PALETTE_GRID_Y;
        int width = PALETTE_COLUMNS * CELL_SIZE;
        int height = PALETTE_VISIBLE_ROWS * CELL_SIZE;
        return mouseX >= originX && mouseX < originX + width && mouseY >= originY && mouseY < originY + height;
    }

    private int getGridIndexAt(double mouseX, double mouseY) {
        int originX = left() + GRID_START_X;
        int originY = top() + GRID_START_Y;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = originX + col * (CELL_SIZE + 2);
                int y = originY + row * (CELL_SIZE + 2);
                if (mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE) {
                    return row * GRID_LIMIT + col;
                }
            }
        }
        return -1;
    }

    private void applyPaletteItem(ResourceLocation id, boolean forceOutput) {
        if (forceOutput || paletteTarget == PaletteTarget.OUTPUT) {
            outputItemBox.setValue(id.toString());
            updateStatus("Output set to " + getDisplayName(id));
            return;
        }

        selectedIngredientBox.setValue(id.toString());
        gridValues[selectedCell] = id.toString();
        updateStatus("Cell set to " + getDisplayName(id));
    }

    private boolean isSelectedPaletteItem(ResourceLocation id) {
        String selectedIngredient = normalizedIngredient(selectedCell >= 0 && selectedCell < gridValues.length ? gridValues[selectedCell] : "");
        if (paletteTarget == PaletteTarget.CELL && id.toString().equals(selectedIngredient)) {
            return true;
        }
        return id.toString().equals(outputItemBox.getValue().trim());
    }

    private ItemStack stackFor(ResourceLocation id) {
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }

    private ItemStack stackForIngredient(String ingredient) {
        String value = normalizedIngredient(ingredient);
        if (value.isBlank() || value.startsWith("#")) {
            return ItemStack.EMPTY;
        }
        if (value.startsWith("fluid:") || value.startsWith("fluidtag:")) {
            return ItemStack.EMPTY;
        }
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return ItemStack.EMPTY;
        }
        return stackFor(id);
    }

    private ItemStack stackForOutputPreview() {
        ItemStack stack = stackForIngredient(outputItemBox == null ? "" : outputItemBox.getValue());
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack preview = stack.copy();
        preview.setCount(parseOutputCount());
        return preview;
    }

    private int parseOutputCount() {
        if (outputCountBox == null) {
            return 1;
        }
        try {
            return Math.max(1, Integer.parseInt(outputCountBox.getValue().trim()));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private String deriveRecipeFileName(String outputItem) {
        String rawValue = outputItem == null ? "" : outputItem.trim();
        if (rawValue.startsWith("fluid:")) {
            rawValue = rawValue.substring("fluid:".length());
        } else if (rawValue.startsWith("fluidtag:")) {
            rawValue = rawValue.substring("fluidtag:".length());
        }
        int atIndex = rawValue.lastIndexOf('@');
        if (atIndex > 0) {
            rawValue = rawValue.substring(0, atIndex);
        }
        ResourceLocation id = ResourceLocation.tryParse(rawValue);
        String value = id != null ? id.getPath() : rawValue;
        int colon = value.indexOf(':');
        if (colon >= 0 && colon + 1 < value.length()) {
            value = value.substring(colon + 1);
        }
        value = value.replaceAll("[^a-zA-Z0-9_.-]", "_");
        return value.isBlank() ? "new_recipe" : value;
    }

    private String shortenIngredient(String ingredient) {
        String value = normalizedIngredient(ingredient);
        if (value.isBlank()) {
            return "";
        }
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        int colon = value.indexOf(':');
        if (colon >= 0 && colon + 1 < value.length()) {
            value = value.substring(colon + 1);
        }
        int slash = value.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < value.length()) {
            value = value.substring(slash + 1);
        }
        return value.length() > SYMBOL_PREVIEW_LIMIT ? value.substring(0, SYMBOL_PREVIEW_LIMIT) : value;
    }

    private String shortenPaletteLabel(String value) {
        String shortened = shortenIngredient(value);
        return shortened.isBlank() ? value : shortened;
    }

    private ParsedFluidIngredient parseFluidIngredient(String ingredient, boolean isTag) {
        String prefix = isTag ? "fluidtag:" : "fluid:";
        String rawValue = ingredient.substring(prefix.length()).trim();
        int atIndex = rawValue.lastIndexOf('@');
        if (atIndex <= 0 || atIndex >= rawValue.length() - 1) {
            throw new IllegalArgumentException("Fluid ingredient format: " + prefix + "modid:name@250");
        }
        String id = rawValue.substring(0, atIndex).trim();
        String amountText = rawValue.substring(atIndex + 1).trim();
        int amount;
        try {
            amount = Math.max(1, Integer.parseInt(amountText));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Fluid amount must be a number");
        }
        if (id.isBlank()) {
            throw new IllegalArgumentException("Fluid id is required");
        }
        return new ParsedFluidIngredient(id, amount);
    }

    private record RecipeJsonData(String folder, String fileName, String json) {
    }

    private record ParsedFluidIngredient(String id, int amount) {
    }

    private enum RecipeMode {
        SHAPED("Shaped Crafting", "Shaped", "minecraft:crafting_shaped", "crafting_shaped", 1, 3, 1, 3),
        SHAPELESS("Shapeless Crafting", "Shapeless", "minecraft:crafting_shapeless", "crafting_shapeless", 1, 3, 1, 3),
        PRESSING("Create Pressing", "Pressing", "create:pressing", "create_pressing", 1, 1, 1, 1),
        CRUSHING("Create Crushing", "Crushing", "create:crushing", "create_crushing", 1, 1, 1, 1),
        MIXING("Create Mixing", "Mixing", "create:mixing", "create_mixing", 1, 3, 1, 3),
        COMPACTING("Create Compacting", "Compacting", "create:compacting", "create_compacting", 1, 3, 1, 3),
        MECHANICAL("Create Mechanical Crafting", "Mechanical", "create:mechanical_crafting", "create_mechanical_crafting", 1, 9, 1, 9);

        private final String label;
        private final String shortLabel;
        private final String typeId;
        private final String folder;
        private final int minRows;
        private final int maxRows;
        private final int minCols;
        private final int maxCols;

        RecipeMode(String label, String shortLabel, String typeId, String folder, int minRows, int maxRows, int minCols, int maxCols) {
            this.label = label;
            this.shortLabel = shortLabel;
            this.typeId = typeId;
            this.folder = folder;
            this.minRows = minRows;
            this.maxRows = maxRows;
            this.minCols = minCols;
            this.maxCols = maxCols;
        }

        private RecipeMode next() {
            RecipeMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private enum HeatRequirement {
        NONE("none", ""),
        HEATED("heated", "heated"),
        SUPERHEATED("superheated", "superheated");

        private final String label;
        private final String jsonValue;

        HeatRequirement(String label, String jsonValue) {
            this.label = label;
            this.jsonValue = jsonValue;
        }

        private HeatRequirement next() {
            HeatRequirement[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private enum PaletteTarget {
        CELL("cell"),
        OUTPUT("result");

        private final String label;

        PaletteTarget(String label) {
            this.label = label;
        }

        private PaletteTarget next() {
            PaletteTarget[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private enum PaletteScope {
        COMMON("Common") {
            @Override
            boolean allows(ResourceLocation id) {
                return id.getNamespace().equals("create_the_air_wars")
                    || id.getNamespace().equals("create")
                    || id.getNamespace().equals("minecraft");
            }
        },
        CTAW("CTAW") {
            @Override
            boolean allows(ResourceLocation id) {
                return id.getNamespace().equals("create_the_air_wars");
            }
        },
        ALL("All") {
            @Override
            boolean allows(ResourceLocation id) {
                return true;
            }
        };

        private final String label;

        PaletteScope(String label) {
            this.label = label;
        }

        abstract boolean allows(ResourceLocation id);

        private PaletteScope next() {
            PaletteScope[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }
}
