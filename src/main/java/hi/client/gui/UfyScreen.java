package hi.client.gui;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import java.util.HashMap;

import hi.world.inventory.UfyMenu;


import hi.CreateTheAirWarsMod;

import com.mojang.blaze3d.systems.RenderSystem;

public class UfyScreen extends AbstractContainerScreen<UfyMenu> {
	private final static HashMap<String, Object> guistate = UfyMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	EditBox input;
	EditBox output;
	Button button_send;

	public UfyScreen(UfyMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 256;
		this.imageHeight = 166;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		input.render(guiGraphics, mouseX, mouseY, partialTicks);
		output.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		guiGraphics.blit(net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:textures/screens/image_2.png"), this.leftPos + -23, this.topPos + 2, 0, 0, 160, 160, 160, 160);

		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		if (input.isFocused())
			return input.keyPressed(key, b, c);
		if (output.isFocused())
			return output.keyPressed(key, b, c);
		return super.keyPressed(key, b, c);
	}

	@Override
	public void containerTick() {
		super.containerTick();
		////input.tick();
		////output.tick();
	}

	@Override
	public void resize(Minecraft minecraft, int width, int height) {
		String inputValue = input.getValue();
		String outputValue = output.getValue();
		super.resize(minecraft, width, height);
		input.setValue(inputValue);
		output.setValue(outputValue);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
	}

	@Override
	public void init() {
		super.init();
		input = new EditBox(this.font, this.leftPos + 114, this.topPos + 7, 118, 18, Component.translatable("gui.create_the_air_wars.ufy.input"));
		input.setMaxLength(32767);
		guistate.put("text:input", input);
		this.addWidget(this.input);
		output = new EditBox(this.font, this.leftPos + 114, this.topPos + 139, 118, 18, Component.translatable("gui.create_the_air_wars.ufy.output"));
		output.setMaxLength(32767);
		guistate.put("text:output", output);
		this.addWidget(this.output);
		button_send = Button.builder(Component.translatable("gui.create_the_air_wars.ufy.button_send"), e -> {
			if (true) {
				// net.neoforged.neoforge.network.PacketDistributor.sendToServer(new UfyButtonMessage(0, x, y, z));
							}
		}).bounds(this.leftPos + 147, this.topPos + 38, 46, 20).build();
		guistate.put("button:button_send", button_send);
		this.addRenderableWidget(button_send);
	}
}
