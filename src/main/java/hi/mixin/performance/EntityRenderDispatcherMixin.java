package hi.mixin.performance;

import com.mojang.blaze3d.vertex.PoseStack;
import hi.CreateTheAirWarsMod;
import hi.client.performance.ClientPerformanceLimiter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Shadow
    private boolean shouldRenderShadow;

    @Unique
    private boolean createTheAirWars$shadowStatePatched;

    @Unique
    private boolean createTheAirWars$previousShadowState;

    @Inject(
        method = {"shouldRender", "a(Lbsr;Lgie;DDD)Z"},
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private <E extends Entity> void createTheAirWars$allowEntitiesInCameraFeed(
        E entity,
        net.minecraft.client.renderer.culling.Frustum frustum,
        double x,
        double y,
        double z,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (entity != null && entity.getPersistentData().getBoolean(hi.client.radar.ClientMissileTracker.RADAR_VIRTUAL_TAG)) {
            cir.setReturnValue(false);
            return;
        }
        if (!hi.client.camera.CameraFeedRenderer.isRenderingFeed()) {
            return;
        }
        if (entity == null || entity.isRemoved() || entity == Minecraft.getInstance().getCameraEntity()) {
            cir.setReturnValue(false);
            return;
        }
        cir.setReturnValue(true);
    }

    @ModifyVariable(
        method = {"render", "a(Lbsr;DDDFFLfbi;Lgez;I)V"},
        at = @At("HEAD"),
        argsOnly = true,
        require = 0
    )
    private <E extends Entity> MultiBufferSource createTheAirWars$wrapBuffer(
        MultiBufferSource original,
        E entity,
        double x,
        double y,
        double z,
        float rotationYaw,
        float partialTicks,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight
    ) {
        if (createTheAirWars$shouldUseThermalEntityPath(entity)) {
            net.minecraft.resources.ResourceLocation texture;
            try {
                texture = ((EntityRenderDispatcher)(Object)this).getRenderer(entity).getTextureLocation(entity);
            } catch (Throwable t) {
                texture = net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;
            }
            if (texture == null) {
                texture = net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;
            }
            net.minecraft.resources.ResourceLocation thermalTexture = texture;
            return type -> {
                return original.getBuffer(hi.client.renderer.ThermalEntityRenderType.getThermalEntity(thermalTexture));
            };
        }
        return original;
    }

    @Unique
    private static boolean createTheAirWars$shouldUseThermalEntityPath(Entity entity) {
        if (!hi.client.camera.CameraFeedRenderer.isRenderingFeed() || !hi.client.camera.CameraMonitorClientHandler.isThermalModeEnabled()) {
            return false;
        }
        if (entity instanceof Display) {
            return false;
        }
        if (entity instanceof LivingEntity || entity instanceof Projectile) {
            return true;
        }
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return entityId != null && CreateTheAirWarsMod.MODID.equals(entityId.getNamespace());
    }

    @Inject(
        method = {"render", "a(Lbsr;DDDFFLfbi;Lgez;I)V"},
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private <E extends Entity> void createTheAirWars$beforeRender(
        E entity,
        double x,
        double y,
        double z,
        float rotationYaw,
        float partialTicks,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        CallbackInfo ci
    ) {
        if (entity != null && entity.getPersistentData().getBoolean(hi.client.radar.ClientMissileTracker.RADAR_VIRTUAL_TAG)) {
            ci.cancel();
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (ClientPerformanceLimiter.shouldDisableEntityShadows(minecraft, entity)) {
            createTheAirWars$shadowStatePatched = true;
            createTheAirWars$previousShadowState = this.shouldRenderShadow;
            this.shouldRenderShadow = false;
        } else {
            createTheAirWars$shadowStatePatched = false;
        }
    }

    @Inject(
        method = {"render", "a(Lbsr;DDDFFLfbi;Lgez;I)V"},
        at = @At("RETURN"),
        require = 0
    )
    private <E extends Entity> void createTheAirWars$afterRender(
        E entity,
        double x,
        double y,
        double z,
        float rotationYaw,
        float partialTicks,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        CallbackInfo ci
    ) {
        if (createTheAirWars$shadowStatePatched) {
            this.shouldUseShadowsRestore();
        }
    }

    @Unique
    private void shouldUseShadowsRestore() {
        this.shouldRenderShadow = createTheAirWars$previousShadowState;
        createTheAirWars$shadowStatePatched = false;
    }
}
