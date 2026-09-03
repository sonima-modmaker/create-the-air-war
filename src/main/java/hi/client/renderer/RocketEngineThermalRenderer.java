package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import hi.block.entity.RocketEngineBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class RocketEngineThermalRenderer implements BlockEntityRenderer<RocketEngineBlockEntity> {
    public RocketEngineThermalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RocketEngineBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        float intensity = blockEntity.getThermalHighlightStrength(partialTick);
        ThermalModelRenderHelper.renderThermalBlockModel(blockEntity.getBlockState(), poseStack, buffer, intensity);
    }
}
