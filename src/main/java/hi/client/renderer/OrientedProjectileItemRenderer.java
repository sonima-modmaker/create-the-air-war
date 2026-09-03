package hi.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hi.entity.TomahawkbultEntity;
import hi.entity.VihrRocketEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class OrientedProjectileItemRenderer<T extends AbstractArrow & ItemSupplier> extends EntityRenderer<T> {
    private static final float DEFAULT_YAW_SMOOTHING = 0.35f;
    private static final float DEFAULT_PITCH_SMOOTHING = 0.28f;
    private static final float GUIDED_YAW_SMOOTHING = 0.18f;
    private static final float GUIDED_PITCH_SMOOTHING = 0.14f;
    private static final float ROLL_SMOOTHING = 0.22f;
    private static final float DEFAULT_MAX_YAW_STEP = 18.0f;
    private static final float DEFAULT_MAX_PITCH_STEP = 12.0f;
    private static final float GUIDED_MAX_YAW_STEP = 8.5f;
    private static final float GUIDED_MAX_PITCH_STEP = 6.5f;
    private static final float MAX_BANK_ROLL = 45.0f;
    private static final double MIN_ORIENTATION_SPEED_SQR = 1.0E-6;
    private static final long STATE_RETENTION_TICKS = 40L;

    private final ItemRenderer itemRenderer;
    private final Map<Integer, Float> smoothedYawByEntity = new HashMap<>();
    private final Map<Integer, Float> smoothedPitchByEntity = new HashMap<>();
    private final Map<Integer, Float> smoothedRollByEntity = new HashMap<>();
    private final Map<Integer, Long> lastSeenTickByEntity = new HashMap<>();
    private long lastCleanupTick = Long.MIN_VALUE;

    public OrientedProjectileItemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) {
            return;
        }

        Vec3 motion = entity.getDeltaMovement();
        if (motion.lengthSqr() < MIN_ORIENTATION_SPEED_SQR) {
            motion = new Vec3(entity.getX() - entity.xo, entity.getY() - entity.yo, entity.getZ() - entity.zo);
        }

        float yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot());
        if (motion.lengthSqr() >= MIN_ORIENTATION_SPEED_SQR) {
            double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            yaw = (float) Math.toDegrees(Math.atan2(motion.x, motion.z));
            pitch = (float) Math.toDegrees(Math.atan2(motion.y, Math.max(horizontal, 1.0E-4)));
        }

        int entityId = entity.getId();
        long gameTime = entity.level().getGameTime();
        lastSeenTickByEntity.put(entityId, gameTime);
        boolean guidedProjectile = entity instanceof TomahawkbultEntity || entity instanceof VihrRocketEntity;
        float yawSmoothing = guidedProjectile ? GUIDED_YAW_SMOOTHING : DEFAULT_YAW_SMOOTHING;
        float pitchSmoothing = guidedProjectile ? GUIDED_PITCH_SMOOTHING : DEFAULT_PITCH_SMOOTHING;
        float maxYawStep = guidedProjectile ? GUIDED_MAX_YAW_STEP : DEFAULT_MAX_YAW_STEP;
        float maxPitchStep = guidedProjectile ? GUIDED_MAX_PITCH_STEP : DEFAULT_MAX_PITCH_STEP;

        Float previousYaw = smoothedYawByEntity.get(entityId);
        Float previousPitch = smoothedPitchByEntity.get(entityId);

        float smoothedYaw = previousYaw != null
            ? Mth.rotLerp(yawSmoothing, previousYaw, yaw)
            : yaw;
        float smoothedPitch = previousPitch != null
            ? Mth.lerp(pitchSmoothing, previousPitch, pitch)
            : pitch;

        if (previousYaw != null) {
            float yawDelta = Mth.wrapDegrees(smoothedYaw - previousYaw);
            smoothedYaw = previousYaw + Mth.clamp(yawDelta, -maxYawStep, maxYawStep);
        }
        if (previousPitch != null) {
            float pitchDelta = smoothedPitch - previousPitch;
            smoothedPitch = previousPitch + Mth.clamp(pitchDelta, -maxPitchStep, maxPitchStep);
        }

        float smoothedRoll = 0.0f;
        if (guidedProjectile && previousYaw != null) {
            float yawDelta = Mth.wrapDegrees(smoothedYaw - previousYaw);
            float targetRoll = Mth.clamp(-yawDelta * 4.5f, -MAX_BANK_ROLL, MAX_BANK_ROLL);
            float previousRoll = smoothedRollByEntity.getOrDefault(entityId, 0.0f);
            smoothedRoll = Mth.lerp(ROLL_SMOOTHING, previousRoll, targetRoll);
        }
        smoothedRollByEntity.put(entityId, smoothedRoll);

        smoothedYawByEntity.put(entityId, smoothedYaw);
        smoothedPitchByEntity.put(entityId, smoothedPitch);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(smoothedYaw + 180.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(smoothedPitch));
        if (guidedProjectile) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(smoothedRoll));
        }
        if (entity instanceof VihrRocketEntity) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((entity.tickCount + partialTicks) * 32.0F));
            poseStack.translate(
                Math.sin((entity.tickCount + partialTicks) * 0.2F) * 0.01D,
                Math.cos((entity.tickCount + partialTicks) * 0.17F) * 0.01D,
                0.0D
            );
        }
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.NONE, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();

        if (entity.isRemoved() || !entity.isAlive()) {
            removeEntityState(entityId);
        } else if (gameTime - lastCleanupTick >= STATE_RETENTION_TICKS) {
            pruneStaleState(gameTime);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void pruneStaleState(long gameTime) {
        lastCleanupTick = gameTime;
        Iterator<Map.Entry<Integer, Long>> iterator = lastSeenTickByEntity.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Long> entry = iterator.next();
            if (gameTime - entry.getValue() > STATE_RETENTION_TICKS) {
                int entityId = entry.getKey();
                iterator.remove();
                smoothedYawByEntity.remove(entityId);
                smoothedPitchByEntity.remove(entityId);
                smoothedRollByEntity.remove(entityId);
            }
        }
    }

    private void removeEntityState(int entityId) {
        lastSeenTickByEntity.remove(entityId);
        smoothedYawByEntity.remove(entityId);
        smoothedPitchByEntity.remove(entityId);
        smoothedRollByEntity.remove(entityId);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return null;
    }
}
