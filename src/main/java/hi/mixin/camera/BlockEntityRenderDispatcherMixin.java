package hi.mixin.camera;

import com.mojang.blaze3d.vertex.PoseStack;
import hi.client.renderer.ThermalModelRenderHelper;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Unique
    private static final String CREATE_THE_AIR_WARS$PORTABLE_ENGINE_BE = "dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlockEntity";

    @Unique
    private static final Map<Class<?>, Field> CREATE_THE_AIR_WARS$VISUAL_STRENGTH_FIELDS = new ConcurrentHashMap<>();

    @Inject(method = "render", at = @At("TAIL"))
    private void createTheAirWars$renderThermalBlockHighlights(
        BlockEntity blockEntity,
        float partialTicks,
        PoseStack poseStack,
        MultiBufferSource buffer,
        CallbackInfo ci
    ) {
        if (blockEntity == null || blockEntity.getLevel() == null || !hi.client.camera.ThermalRenderHooks.isThermalCameraFeedActive()) {
            return;
        }

        if (createTheAirWars$isSimulatedPortableEngine(blockEntity)) {
            ThermalModelRenderHelper.renderThermalBlockModel(
                blockEntity.getBlockState(),
                poseStack,
                buffer,
                createTheAirWars$getPortableEngineThermalStrength(blockEntity, partialTicks)
            );
        }
    }

    @Unique
    private static boolean createTheAirWars$isSimulatedPortableEngine(BlockEntity blockEntity) {
        if (!CREATE_THE_AIR_WARS$PORTABLE_ENGINE_BE.equals(blockEntity.getClass().getName())) {
            return false;
        }

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock());
        return "simulated".equals(blockId.getNamespace()) && blockId.getPath().endsWith("portable_engine");
    }

    @Unique
    private static float createTheAirWars$getPortableEngineThermalStrength(BlockEntity blockEntity, float partialTicks) {
        try {
            Field field = CREATE_THE_AIR_WARS$VISUAL_STRENGTH_FIELDS.computeIfAbsent(blockEntity.getClass(), clazz -> {
                try {
                    Field reflected = clazz.getDeclaredField("visualStrength");
                    reflected.setAccessible(true);
                    return reflected;
                } catch (ReflectiveOperationException e) {
                    return null;
                }
            });

            if (field == null) {
                return 0.0f;
            }

            Object value = field.get(blockEntity);
            if (value instanceof LerpedFloat lerpedFloat) {
                return lerpedFloat.getValue(partialTicks);
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return 0.0f;
    }
}
