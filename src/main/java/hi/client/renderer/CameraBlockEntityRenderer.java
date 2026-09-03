package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import hi.block.CameraBlock;
import hi.block.entity.CameraBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CameraBlockEntityRenderer implements BlockEntityRenderer<CameraBlockEntity> {
    public static final PartialModel CAMERA_PLATFORM = PartialModel.of(
        ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/camera_platform"));
    public static final PartialModel CAMERA_BASE = PartialModel.of(
        ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/camera_base"));
    public static final PartialModel CAMERA_HEAD = PartialModel.of(
        ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/camera_head"));

    public CameraBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void render(CameraBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(CameraBlock.FACING);
        Direction horizontalFacing = state.getValue(CameraBlock.HORIZONTAL_FACING);
        float pitch = be.getRenderPitch(partialTick);
        float yaw = be.getRenderYaw(partialTick);
        if (pitch == -90.0F) {
            pitch = -89.99F;
        } else if (pitch == 90.0F) {
            pitch = 89.99F;
        }

        float mountYawRotation = facing == Direction.UP ? 180.0F - horizontalFacing.toYRot() : 0.0F;
        Vec3 worldDirection = Vec3.directionFromRotation(pitch, yaw);
        Vec3 mountDirection = facing == Direction.UP ? rotateY(worldDirection, mountYawRotation) : worldDirection;
        Vec3 mountLocalDirection = toMountLocalDirection(facing, mountDirection);
        float baseYaw = directionYaw(mountLocalDirection);
        float headPitch = directionPitch(mountLocalDirection);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        if (facing == Direction.UP) {
            poseStack.mulPose(Axis.YP.rotationDegrees(mountYawRotation));
        }
        rotateFromFloorMount(poseStack, facing);
        poseStack.translate(-0.5, -0.5, -0.5);

        SuperByteBuffer platform = CachedBuffers.partial(CAMERA_PLATFORM, state);
        platform.light(light);
        platform.renderInto(poseStack, buffer.getBuffer(RenderType.solid()));

        poseStack.pushPose();
        rotateAroundCenter(poseStack, Axis.YP, 180.0F - baseYaw);
        SuperByteBuffer base = CachedBuffers.partial(CAMERA_BASE, state);
        base.light(light);
        base.renderInto(poseStack, buffer.getBuffer(RenderType.solid()));

        poseStack.pushPose();
        rotateAroundCenter(poseStack, Axis.XP, -headPitch);
        SuperByteBuffer head = CachedBuffers.partial(CAMERA_HEAD, state);
        head.light(light);
        head.renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
        poseStack.popPose();

        poseStack.popPose();
        poseStack.popPose();
    }

    public static void rotateFromFloorMount(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case EAST -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            case WEST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            default -> {
            }
        }
    }

    private static void rotateAroundCenter(PoseStack poseStack, Axis axis, float degrees) {
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(axis.rotationDegrees(degrees));
        poseStack.translate(-0.5, -0.5, -0.5);
    }

    private static Vec3 toMountLocalDirection(Direction facing, Vec3 direction) {
        return switch (facing) {
            case DOWN -> new Vec3(direction.x, -direction.y, -direction.z);
            case NORTH -> new Vec3(direction.x, -direction.z, direction.y);
            case SOUTH -> new Vec3(direction.x, direction.z, -direction.y);
            case EAST -> new Vec3(-direction.y, direction.x, direction.z);
            case WEST -> new Vec3(direction.y, -direction.x, direction.z);
            default -> direction;
        };
    }

    private static float directionYaw(Vec3 direction) {
        Vec3 flat = new Vec3(direction.x, 0.0D, direction.z);
        if (flat.lengthSqr() < 1.0E-6D) {
            return 0.0F;
        }
        flat = flat.normalize();
        return (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(-flat.x, flat.z)));
    }

    private static float directionPitch(Vec3 direction) {
        Vec3 normalized = direction.lengthSqr() > 1.0E-6D ? direction.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        return (float) Mth.clamp(Mth.wrapDegrees(-Math.toDegrees(Math.asin(normalized.y))), -89.9D, 89.9D);
    }

    private static Vec3 rotateY(Vec3 vector, float degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(
            vector.x * cos - vector.z * sin,
            vector.y,
            vector.x * sin + vector.z * cos
        );
    }
}
