package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import hi.block.entity.GyroStabilizerBlockEntity;
import hi.block.GyroStabilizerBlock;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class GyroStabilizerRenderer extends KineticBlockEntityRenderer<GyroStabilizerBlockEntity> {
    private static final PartialModel FLYWHEEL_ACTIVE = PartialModel.of(
        ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/gyro_stabilizer_flywheel_active"));
    private static final PartialModel FLYWHEEL_DEACTIVE = PartialModel.of(
        ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/gyro_stabilizer_flywheel_deactive"));

    public GyroStabilizerRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(GyroStabilizerBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (be.getLevel() == null) {
            return;
        }

        renderLowerShaft(be, poseStack, buffer, light);
        renderFlywheel(be, partialTicks, poseStack, buffer, light);
    }

    private void renderLowerShaft(GyroStabilizerBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light) {
        SuperByteBuffer shaftBuffer = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), Direction.DOWN);
        kineticRotationTransform(shaftBuffer, be, getRotationAxisOf(be), getAngleForBe(be, be.getBlockPos(), getRotationAxisOf(be)), light);
        shaftBuffer.renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }

    private void renderFlywheel(GyroStabilizerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light) {
        BlockState blockState = be.getBlockState();
        PartialModel partial = blockState.getValue(GyroStabilizerBlock.POWERED) ? FLYWHEEL_ACTIVE : FLYWHEEL_DEACTIVE;
        SuperByteBuffer flywheelBuffer = CachedBuffers.partial(partial, blockState);
        float angle = AngleHelper.rad(be.getFlywheelAngle(partialTicks));
        VertexConsumer vertexConsumer = buffer.getBuffer(getRenderType(be, blockState));

        kineticRotationTransform(flywheelBuffer, be, getRotationAxisOf(be), angle, light);
        flywheelBuffer.renderInto(ms, vertexConsumer);
    }
}
