package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class VihrRocketItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static VihrRocketItemRenderer INSTANCE;

    private VihrRocketItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static VihrRocketItemRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new VihrRocketItemRenderer();
        }
        return INSTANCE;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        VihrRocketModelRenderer.applyItemTransform(poseStack, context);
        VihrRocketModelRenderer.render(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
