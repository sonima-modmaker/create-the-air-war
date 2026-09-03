package hi.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hi.client.camera.ThermalRenderHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public final class ThermalModelRenderHelper {
    private ThermalModelRenderHelper() {
    }

    public static boolean shouldRenderThermalModel(float intensity) {
        return ThermalRenderHooks.isThermalCameraFeedActive() && intensity > 0.001f;
    }

    public static void renderThermalBlockModel(BlockState state, com.mojang.blaze3d.vertex.PoseStack poseStack, MultiBufferSource buffer, float intensity) {
        if (!shouldRenderThermalModel(intensity)) {
            return;
        }

        float clamped = Mth.clamp(intensity, 0.0f, 1.0f);
        MultiBufferSource thermalBuffer = renderType -> new AlphaVertexConsumer(
            buffer.getBuffer(ThermalEntityRenderType.getThermalEntityTranslucent(InventoryMenu.BLOCK_ATLAS)),
            clamped
        );

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
            state,
            poseStack,
            thermalBuffer,
            LightTexture.FULL_BRIGHT,
            net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
            ModelData.EMPTY,
            RenderType.cutout()
        );
    }

    private static final class AlphaVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float intensity;

        private AlphaVertexConsumer(VertexConsumer delegate, float intensity) {
            this.delegate = delegate;
            this.intensity = intensity;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            int outAlpha = Mth.clamp(Math.round(alpha * intensity), 0, 255);
            delegate.setColor(255, 255, 255, outAlpha);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }
    }
}
