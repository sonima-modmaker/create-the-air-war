package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hi.block.X25mlBlock;
import hi.block.entity.X25mlBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;

public class X25mlBlockEntityRenderer implements BlockEntityRenderer<X25mlBlockEntity> {
    public X25mlBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(X25mlBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // X25ML now renders through the normal baked block model path.
    }
}
