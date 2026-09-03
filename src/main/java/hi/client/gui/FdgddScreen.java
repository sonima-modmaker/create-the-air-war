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
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;

import hi.world.inventory.FdgddMenu;


import hi.CreateTheAirWarsMod;

import com.mojang.blaze3d.systems.RenderSystem;

public class FdgddScreen extends AbstractContainerScreen<FdgddMenu> {
	private final static HashMap<String, Object> guistate = FdgddMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	EditBox targetX;
	EditBox targetY;
	EditBox targetZ;
	Button button_save;
	private int centerX;
	private int centerY;
	private int centerZ;

	public FdgddScreen(FdgddMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	private static final ResourceLocation texture = net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:textures/screens/fdgdd.png");

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		targetX.render(guiGraphics, mouseX, mouseY, partialTicks);
		targetY.render(guiGraphics, mouseX, mouseY, partialTicks);
		targetZ.render(guiGraphics, mouseX, mouseY, partialTicks);
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
		if (targetX.isFocused())
			return targetX.keyPressed(key, b, c);
		if (targetY.isFocused())
			return targetY.keyPressed(key, b, c);
		if (targetZ.isFocused())
			return targetZ.keyPressed(key, b, c);
		return super.keyPressed(key, b, c);
	}

	@Override
	public void containerTick() {
		super.containerTick();
		// //targetX.tick();
		// //targetZ.tick();
	}

	@Override
	public void resize(Minecraft minecraft, int width, int height) {
		String targetXValue = targetX.getValue();
		String targetYValue = targetY.getValue();
		String targetZValue = targetZ.getValue();
		super.resize(minecraft, width, height);
		targetX.setValue(targetXValue);
		targetY.setValue(targetYValue);
		targetZ.setValue(targetZValue);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, Component.translatable("gui.create_the_air_wars.fdgdd.label_target_x"), 8, 24, -12829636, false);
		guiGraphics.drawString(this.font, Component.literal("Y"), 8, 48, -12829636, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.create_the_air_wars.fdgdd.label_target_z"), 8, 72, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		centerX = (int) Math.floor(entity.getX());
		centerY = (int) Math.floor(entity.getY());
		centerZ = (int) Math.floor(entity.getZ());

		targetX = new EditBox(this.font, this.leftPos + 30, this.topPos + 22, 70, 16, Component.translatable("gui.create_the_air_wars.fdgdd.target_x"));
		targetX.setMaxLength(20);
		guistate.put("text:target_x", targetX);
		this.addWidget(this.targetX);

		targetY = new EditBox(this.font, this.leftPos + 30, this.topPos + 46, 70, 16, Component.literal("Y"));
		targetY.setMaxLength(20);
		guistate.put("text:target_y", targetY);
		this.addWidget(this.targetY);

		targetZ = new EditBox(this.font, this.leftPos + 30, this.topPos + 70, 70, 16, Component.translatable("gui.create_the_air_wars.fdgdd.target_z"));
		targetZ.setMaxLength(20);
		guistate.put("text:target_z", targetZ);
		this.addWidget(this.targetZ);

		ItemStack stack = menu.hand == 0 ? entity.getMainHandItem() : entity.getOffhandItem();
		if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) {
			net.minecraft.nbt.CompoundTag t = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag();
			if (t.contains("targetX")) targetX.setValue(Integer.toString(t.getInt("targetX")));
			if (t.contains("targetY")) targetY.setValue(Integer.toString(t.getInt("targetY")));
			if (t.contains("targetZ")) targetZ.setValue(Integer.toString(t.getInt("targetZ")));
		}

		button_save = Button.builder(Component.translatable("gui.create_the_air_wars.fdgdd.button_save"), e -> {
			int tx = centerX;
			int ty = centerY;
			int tz = centerZ;
			try { tx = Integer.parseInt(targetX.getValue().trim()); } catch (Exception ex) {}
			try { ty = Integer.parseInt(targetY.getValue().trim()); } catch (Exception ex) {}
			try { tz = Integer.parseInt(targetZ.getValue().trim()); } catch (Exception ex) {}
			net.neoforged.neoforge.network.PacketDistributor.sendToServer(new hi.network.FdgddButtonMessage(0, x, y, z, menu.hand, tx, ty, tz));
		}).bounds(this.leftPos + 110, this.topPos + 42, 54, 20).build();
		guistate.put("button:button_save", button_save);
		this.addRenderableWidget(button_save);
	}
}
