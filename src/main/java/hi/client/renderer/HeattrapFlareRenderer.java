package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hi.CreateTheAirWarsMod;
import hi.entity.HeattrapFlareEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class HeattrapFlareRenderer extends EntityRenderer<HeattrapFlareEntity> {
    private static final ResourceLocation[] FRAMES = new ResourceLocation[] {
        ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "textures/entity/heattrap_flare_1.png"),
        ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "textures/entity/heattrap_flare_2.png"),
        ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "textures/entity/heattrap_flare_3.png"),
        ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "textures/entity/heattrap_flare_4.png"),
        ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "textures/entity/heattrap_flare_5.png"),
        ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "textures/entity/heattrap_flare_6.png"),
        ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "textures/entity/heattrap_flare_7.png"),
        ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "textures/entity/heattrap_flare_8.png")
    };

    public HeattrapFlareRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(HeattrapFlareEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float alpha = entity.getRenderAlpha(partialTicks);
        if (alpha <= 0.01f) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0, 0.1, 0.0);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(entity.isLanded() ? 0.9f : 0.72f, entity.isLanded() ? 0.9f : 0.72f, 1.0f);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        int light = 0xF000F0;

        addVertex(vertexConsumer, pose, matrix, -0.5f, -0.5f, 0.0f, 1.0f, light, alpha);
        addVertex(vertexConsumer, pose, matrix, 0.5f, -0.5f, 1.0f, 1.0f, light, alpha);
        addVertex(vertexConsumer, pose, matrix, 0.5f, 0.5f, 1.0f, 0.0f, light, alpha);
        addVertex(vertexConsumer, pose, matrix, -0.5f, 0.5f, 0.0f, 0.0f, light, alpha);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void addVertex(VertexConsumer consumer, PoseStack.Pose pose, Matrix4f matrix, float x, float y, float u, float v, int light, float alpha) {
        consumer.addVertex(matrix, x, y, 0.0f)
            .setColor(255, 255, 255, Math.max(0, Math.min(255, (int) (alpha * 255.0f))))
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(HeattrapFlareEntity entity) {
        return FRAMES[(entity.tickCount / 4) % FRAMES.length];
    }
}
