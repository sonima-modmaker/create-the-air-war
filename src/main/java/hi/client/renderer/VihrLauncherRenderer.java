package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import hi.block.VihrLauncherBlock;
import hi.block.entity.VihrLauncherBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class VihrLauncherRenderer implements BlockEntityRenderer<VihrLauncherBlockEntity> {
    public static final PartialModel[] VIHR_ROCKETS = new PartialModel[] {
        PartialModel.of(ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/vihr_rocket_1")),
        PartialModel.of(ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/vihr_rocket_2")),
        PartialModel.of(ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/vihr_rocket_3")),
        PartialModel.of(ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/vihr_rocket_4")),
        PartialModel.of(ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/vihr_rocket_5")),
        PartialModel.of(ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/vihr_rocket_6"))
    };
    public static final PartialModel[] VIHR_PANELS = new PartialModel[] {
        PartialModel.of(ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/vihr_panel_1")),
        PartialModel.of(ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/vihr_panel_2")),
        PartialModel.of(ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/vihr_panel_3")),
        PartialModel.of(ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/vihr_panel_4")),
        PartialModel.of(ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/vihr_panel_5")),
        PartialModel.of(ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/vihr_panel_6"))
    };
    private static final double[][] PANEL_ORIGINS = new double[][] {
        {12.0D / 16.0D, 10.0D / 16.0D, -2.0D / 16.0D},
        {3.0D / 16.0D, 10.0D / 16.0D, -2.0D / 16.0D},
        {14.0D / 16.0D, 5.0D / 16.0D, -2.0D / 16.0D},
        {1.0D / 16.0D, 5.0D / 16.0D, -2.0D / 16.0D},
        {5.0D / 16.0D, 5.0D / 16.0D, -2.0D / 16.0D},
        {9.0D / 16.0D, 5.0D / 16.0D, -2.0D / 16.0D}
    };

    public VihrLauncherRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void render(VihrLauncherBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(VihrLauncherBlock.FACING);
        Direction horizontalFacing = state.getValue(VihrLauncherBlock.HORIZONTAL_FACING);
        float yaw = be.getRenderYaw(partialTick);

        float mountYawRotation = 0.0F;
        if (facing == Direction.UP || facing == Direction.DOWN) {
            mountYawRotation = 180.0F - horizontalFacing.toYRot();
        } else {
            switch (facing) {
                case SOUTH -> mountYawRotation = 180.0F;
                case WEST -> mountYawRotation = 270.0F;
                case EAST -> mountYawRotation = 90.0F;
                default -> mountYawRotation = 0.0F;
            }
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(mountYawRotation));
        CameraBlockEntityRenderer.rotateFromFloorMount(poseStack, facing);
        
        // Dynamic yaw rotation relative to the base direction
        float relativeYaw = yaw - horizontalFacing.toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-relativeYaw));
        
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        poseStack.translate(-0.5D, -0.5D, -0.5D);

        for (int i = 0; i < VIHR_ROCKETS.length; i++) {
            renderPartial(VIHR_ROCKETS[i], state, poseStack, buffer, light);
            renderPanel(VIHR_PANELS[i], state, poseStack, buffer, light, PANEL_ORIGINS[i], be.getPanelAngleDegrees(i, partialTick));
        }

        poseStack.popPose();
    }

    private static void renderPartial(PartialModel model, BlockState state, PoseStack poseStack, MultiBufferSource buffer, int light) {
        SuperByteBuffer partial = CachedBuffers.partial(model, state);
        partial.light(light);
        partial.renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
    }

    private static void renderPanel(PartialModel model, BlockState state, PoseStack poseStack, MultiBufferSource buffer, int light, double[] origin, float angleDegrees) {
        poseStack.pushPose();
        poseStack.translate(origin[0], origin[1], origin[2]);
        poseStack.mulPose(Axis.XP.rotationDegrees(angleDegrees));
        poseStack.translate(-origin[0], -origin[1], -origin[2]);
        renderPartial(model, state, poseStack, buffer, light);
        poseStack.popPose();
    }
}
