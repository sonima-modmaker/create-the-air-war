package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hi.block.TestBlock;
import hi.block.entity.TestBlockEntity;
import hi.init.CreateTheAirWarsModItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class TestBlockEntityRenderer implements BlockEntityRenderer<TestBlockEntity> {
	private final ItemRenderer itemRenderer;

	public TestBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.itemRenderer = context.getItemRenderer();
	}

	@Override
	public void render(TestBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
		if (blockEntity.getLevel() == null) {
			return;
		}
		ItemStack stack = blockEntity.getShownImage();
		ItemStack screenStack = stack.isEmpty() ? new ItemStack(CreateTheAirWarsModItems.DISPLAY.get()) : stack;
		float yRot = -blockEntity.getBlockState().getValue(TestBlock.FACING).toYRot();
		poseStack.pushPose();
		poseStack.translate(0.5, 0.5, 0.5);
		poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
		poseStack.pushPose();
		poseStack.translate(0, 0, 0.501);
		poseStack.scale(0.92f, 0.92f, 0.92f);
		itemRenderer.renderStatic(screenStack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, blockEntity.getLevel(), (int) blockEntity.getBlockPos().asLong());
		poseStack.popPose();
		if (!blockEntity.getCassette().isEmpty()) {
			poseStack.pushPose();
			poseStack.translate(0.33, -0.33, 0.505);
			poseStack.scale(0.32f, 0.32f, 0.32f);
			itemRenderer.renderStatic(blockEntity.getCassette(), ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, blockEntity.getLevel(), (int) (blockEntity.getBlockPos().asLong() + 31));
			poseStack.popPose();
		}
		if (!blockEntity.isSourceMode()) {
			poseStack.pushPose();
			poseStack.translate(-0.33, -0.33, 0.505);
			poseStack.scale(0.26f, 0.26f, 0.26f);
			itemRenderer.renderStatic(screenStack, ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack, buffer, blockEntity.getLevel(), (int) (blockEntity.getBlockPos().asLong() + 91));
			poseStack.popPose();
		}
		poseStack.popPose();
	}
}
