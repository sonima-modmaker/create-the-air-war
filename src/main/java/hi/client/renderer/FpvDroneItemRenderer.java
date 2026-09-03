package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FpvDroneItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
        "create_the_air_wars", "exact_models/fpv_drone.json"
    );
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        "create_the_air_wars", "textures/item/fpv_drone.png"
    );
    private static final ExactJsonModelRenderer DRONE_MODEL = new ExactJsonModelRenderer(MODEL, TEXTURE, true);
    private static FpvDroneItemRenderer INSTANCE;

    private FpvDroneItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static FpvDroneItemRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FpvDroneItemRenderer();
        }
        return INSTANCE;
    }

    public static void renderModel(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, ItemDisplayContext context) {
        DRONE_MODEL.applyItemTransform(poseStack, context);
        DRONE_MODEL.render(poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        renderModel(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, context);
        poseStack.popPose();
    }
}
