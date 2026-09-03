package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hi.entity.VihrRocketEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VihrRocketEntityRenderer extends EntityRenderer<VihrRocketEntity> {
    public VihrRocketEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(VihrRocketEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float yaw = entity.getVisualYaw(partialTicks);
        float pitch = entity.getVisualPitch(partialTicks);
        float roll = entity.getVisualRollDegrees(partialTicks);
        float wingAngle = entity.getWingAngleDegrees(partialTicks);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw + 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
        VihrRocketModelRenderer.render(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, wingAngle);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(VihrRocketEntity entity) {
        return VihrRocketModelRenderer.TEXTURE;
    }
}
