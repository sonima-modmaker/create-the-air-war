package hi.client.gui;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import java.util.HashMap;

import hi.world.inventory.DfgdfgMenu;
import hi.network.DebugRecipeSaveMessage;
import hi.CreateTheAirWarsMod;

import com.mojang.blaze3d.systems.RenderSystem;

public class DfgdfgScreen extends AbstractContainerScreen<DfgdfgMenu> {
	private final static HashMap<String, Object> guistate = DfgdfgMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	Button button_ozm72;
	Button button_size;
	Button button_save;
	private int gridSize = 3;

	public DfgdfgScreen(DfgdfgMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 256;
		this.imageHeight = 166;
	}

	private static final ResourceLocation texture = net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:textures/screens/dfgdfg.png");

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, Component.translatable("gui.create_the_air_wars.dfgdfg.label_debug_items_menu"), 87, 5, -12829636, false);
		guiGraphics.drawString(this.font, Component.empty(), 20 + 5 * 18 + 2, 20 + 2 * 18 + 4, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		Object savedSize = guistate.get("gridSize");
		if (savedSize instanceof Number) {
			gridSize = Math.max(1, Math.min(5, ((Number) savedSize).intValue()));
		}
		button_ozm72 = Button.builder(Component.translatable("gui.create_the_air_wars.dfgdfg.button_ozm72"), e -> {
		}).bounds(this.leftPos + 18, this.topPos + 34, 56, 20).build();
		guistate.put("button:button_ozm72", button_ozm72);
		this.addRenderableWidget(button_ozm72);
		button_size = Button.builder(Component.empty(), e -> {
			gridSize = gridSize == 5 ? 1 : gridSize + 1;
			guistate.put("gridSize", gridSize);
			button_size.setMessage(Component.empty());
		}).bounds(this.leftPos + 20, this.topPos + 2, 90, 16).build();
		guistate.put("button:button_size", button_size);
		this.addRenderableWidget(button_size);
		button_save = Button.builder(Component.empty(), e -> {
			if (this.minecraft != null) {
				net.neoforged.neoforge.network.PacketDistributor.sendToServer(new DebugRecipeSaveMessage(gridSize));
			}
		}).bounds(this.leftPos + 154, this.topPos + 50, 90, 20).build();
		guistate.put("button:button_save", button_save);
		this.addRenderableWidget(button_save);
	}
}
