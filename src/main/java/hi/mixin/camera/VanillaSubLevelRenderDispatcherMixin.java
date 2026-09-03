package hi.mixin.camera;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ryanhcode.sable.sublevel.render.dispatcher.VanillaSubLevelRenderDispatcher;
import hi.client.camera.ThermalRenderHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VanillaSubLevelRenderDispatcher.class)
public class VanillaSubLevelRenderDispatcherMixin {
    @Unique
    private ShaderInstance createTheAirWars$previousSectionShader;

    @ModifyArg(
        method = "renderSectionLayer",
        at = @At(
            value = "INVOKE",
            target = "Ldev/ryanhcode/sable/sublevel/render/dispatcher/VanillaSubLevelRenderDispatcher;setupDynamicEffects(Lnet/minecraft/client/renderer/ShaderInstance;ZZ)V",
            ordinal = 0
        ),
        index = 0
    )
    private ShaderInstance createTheAirWars$useThermalShaderForDynamicEffects(ShaderInstance original) {
        return ThermalRenderHooks.getThermalBlockShaderOrFallback(original);
    }

    @ModifyArg(
        method = "renderSectionLayer",
        at = @At(
            value = "INVOKE",
            target = "Ldev/ryanhcode/sable/sublevel/render/vanilla/VanillaChunkedSubLevelRenderData;renderChunkedSubLevel(Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/ShaderInstance;Lorg/joml/Matrix4f;DDD)V"
        ),
        index = 1
    )
    private ShaderInstance createTheAirWars$useThermalShaderForChunkedSubLevels(ShaderInstance original) {
        return ThermalRenderHooks.getThermalBlockShaderOrFallback(original);
    }

    @Inject(
        method = "renderSectionLayer",
        at = @At(
            value = "INVOKE",
            target = "Ldev/ryanhcode/sable/sublevel/render/dispatcher/VanillaSubLevelRenderDispatcher;setupDynamicEffects(Lnet/minecraft/client/renderer/ShaderInstance;ZZ)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        )
    )
    private void createTheAirWars$bindThermalShaderForChunkedSubLevels(
        Iterable<?> subLevels,
        net.minecraft.client.renderer.RenderType layer,
        ShaderInstance shader,
        double cameraX,
        double cameraY,
        double cameraZ,
        org.joml.Matrix4f modelView,
        org.joml.Matrix4f projection,
        float partialTicks,
        CallbackInfo ci
    ) {
        ShaderInstance thermal = ThermalRenderHooks.getThermalBlockShaderOrFallback(shader);
        if (thermal != shader) {
            this.createTheAirWars$previousSectionShader = RenderSystem.getShader();
            thermal.setDefaultUniforms(VertexFormat.Mode.QUADS, modelView, projection, Minecraft.getInstance().getWindow());
            thermal.apply();
        } else {
            this.createTheAirWars$previousSectionShader = null;
        }
    }

    @Inject(
        method = "renderSectionLayer",
        at = @At("RETURN")
    )
    private void createTheAirWars$restoreShaderAfterChunkedSubLevels(
        Iterable<?> subLevels,
        net.minecraft.client.renderer.RenderType layer,
        ShaderInstance shader,
        double cameraX,
        double cameraY,
        double cameraZ,
        org.joml.Matrix4f modelView,
        org.joml.Matrix4f projection,
        float partialTicks,
        CallbackInfo ci
    ) {
        if (this.createTheAirWars$previousSectionShader != null) {
            this.createTheAirWars$previousSectionShader.apply();
            this.createTheAirWars$previousSectionShader = null;
        }
    }

    @Redirect(
        method = "renderAfterSections",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;getShader()Lnet/minecraft/client/renderer/ShaderInstance;"
        )
    )
    private ShaderInstance createTheAirWars$useThermalShaderForSingleBlockSubLevels() {
        return ThermalRenderHooks.getThermalBlockShaderOrFallback(RenderSystem.getShader());
    }
}
