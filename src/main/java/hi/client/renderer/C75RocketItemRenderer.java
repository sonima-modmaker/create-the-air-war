package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class C75RocketItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
        "create_the_air_wars", "exact_models/c-75.json"
    );
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        "create_the_air_wars", "textures/block/c-75.png"
    );
    private static final ExactJsonModelRenderer ROCKET_MODEL = new ExactJsonModelRenderer(MODEL, TEXTURE, true);
    private static C75RocketItemRenderer INSTANCE;

    private C75RocketItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static C75RocketItemRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new C75RocketItemRenderer();
        }
        return INSTANCE;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        ROCKET_MODEL.applyItemTransform(poseStack, context);
        ROCKET_MODEL.render(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
