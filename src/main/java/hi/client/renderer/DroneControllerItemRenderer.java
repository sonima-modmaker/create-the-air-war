package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class DroneControllerItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
        "create_the_air_wars", "exact_models/drone_controller.json"
    );
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        "create_the_air_wars", "textures/item/drone_controller.png"
    );
    private static final ExactJsonModelRenderer CONTROLLER_MODEL = new ExactJsonModelRenderer(MODEL, TEXTURE, true, Set.of("display"));
    private static DroneControllerItemRenderer INSTANCE;

    private DroneControllerItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static DroneControllerItemRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DroneControllerItemRenderer();
        }
        return INSTANCE;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        CONTROLLER_MODEL.applyItemTransform(poseStack, context);
        CONTROLLER_MODEL.render(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
