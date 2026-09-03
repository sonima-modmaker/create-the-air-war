package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hi.entity.C75RocketEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class C75RocketRenderer extends EntityRenderer<C75RocketEntity> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
        "create_the_air_wars", "exact_models/c-75.json"
    );
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        "create_the_air_wars", "textures/block/c-75.png"
    );
    private static final ExactJsonModelRenderer ROCKET_MODEL = new ExactJsonModelRenderer(MODEL, TEXTURE, true);

    public C75RocketRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(C75RocketEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getVisualYaw(partialTicks) + 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getVisualPitch(partialTicks)));
        ROCKET_MODEL.render(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(C75RocketEntity entity) {
        return TEXTURE;
    }
}
