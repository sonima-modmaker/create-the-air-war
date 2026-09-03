package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import hi.block.MinigunBlock;
import hi.block.entity.MinigunBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class MinigunBlockEntityRenderer implements BlockEntityRenderer<MinigunBlockEntity> {
    private static final PartialModel BARRELS = PartialModel.of(ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/minigun_barrels"));

    public MinigunBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void render(MinigunBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(MinigunBlock.FACING);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(switch (facing) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F;
        }));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        poseStack.translate(0.5D, 0.5D, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(be.getBarrelAngle(partialTick)));
        poseStack.translate(-0.5D, -0.5D, 0.0D);

        SuperByteBuffer partial = CachedBuffers.partial(BARRELS, state);
        partial.light(light);
        partial.renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
        poseStack.popPose();
    }
}
