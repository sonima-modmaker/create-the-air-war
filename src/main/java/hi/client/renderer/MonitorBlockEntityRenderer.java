package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import hi.block.MonitorBlock;
import hi.block.entity.CameraBlockEntity;
import hi.block.entity.MonitorBlockEntity;
import hi.client.camera.CameraMonitorClientHandler;
import hi.client.camera.CameraFeedTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MonitorBlockEntityRenderer implements BlockEntityRenderer<MonitorBlockEntity> {
    private static final float SCREEN_HALF_SIZE = 6.0f / 16.0f;
    private static final float SCREEN_Z_OFFSET = -0.5025f;

    public MonitorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void render(MonitorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        ResourceLocation texture = CameraFeedTextureManager.getBlankFeedLocation();
        if (blockEntity.hasLinkedCamera()) {
            BlockPos linkedCameraPos = blockEntity.getLinkedCameraPos();
            if (blockEntity.getLevel().getBlockEntity(linkedCameraPos) instanceof CameraBlockEntity) {
                texture = CameraFeedTextureManager.requestFeed(linkedCameraPos.asLong());
            }
        }

        Direction direction = blockEntity.getBlockState().getValue(MonitorBlock.FACING);
        float yaw = direction.toYRot();

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - yaw));
        poseStack.translate(0.0, 0.0, SCREEN_Z_OFFSET);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        addQuad(vertexConsumer, poseStack, -SCREEN_HALF_SIZE, -SCREEN_HALF_SIZE, SCREEN_HALF_SIZE, SCREEN_HALF_SIZE, LightTexture.FULL_BRIGHT);

        if (CameraMonitorClientHandler.isAdjustingMonitor(blockEntity.getBlockPos()) && blockEntity.hasLinkedVihr()) {
            renderLauncherOverlay(poseStack, buffer, blockEntity.getTotalVihrRocketCount(), blockEntity.getTotalLinkedLauncherCapacity());
        }

        poseStack.popPose();
    }

    private static void renderLauncherOverlay(PoseStack poseStack, MultiBufferSource buffer, int rocketCount, int maxRocketCount) {
        Font font = Minecraft.getInstance().font;
        poseStack.pushPose();
        poseStack.translate(-SCREEN_HALF_SIZE + 0.035F, SCREEN_HALF_SIZE - 0.065F, -0.002F);
        poseStack.scale(0.01F, -0.01F, 0.01F);
        String text = "\u041f\u0422\u0423\u0420 " + rocketCount + "/" + maxRocketCount;
        font.drawInBatch(text, 0.0F, 0.0F, 0xFFF2E7C7, false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    private static void addQuad(VertexConsumer consumer, PoseStack poseStack, float x0, float y0, float x1, float y1, int light) {
        Matrix4f pose = poseStack.last().pose();
        Vector3f normal = poseStack.last().normal().transform(new Vector3f(0, 0, -1));
        vertex(consumer, pose, x0, y1, 1.0f, 1.0f, light, normal);
        vertex(consumer, pose, x1, y1, 0.0f, 1.0f, light, normal);
        vertex(consumer, pose, x1, y0, 0.0f, 0.0f, light, normal);
        vertex(consumer, pose, x0, y0, 1.0f, 0.0f, light, normal);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float u, float v, int light, Vector3f normal) {
        consumer.addVertex(pose, x, y, 0.0f)
            .setColor(255, 255, 255, 255)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(normal.x(), normal.y(), normal.z());
    }
}
