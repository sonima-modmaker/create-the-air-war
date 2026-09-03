package hi.client.renderer;

import hi.entity.UxoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UxoRenderer extends EntityRenderer<UxoEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "textures/entity/fab_1500.png");

    public UxoRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(UxoEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(entity.getXRot()));
        poseStack.translate(0.0D, -0.4D, 0.0D); // embedded half-way into ground

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(UxoEntity entity) {
        return TEXTURE;
    }
}
