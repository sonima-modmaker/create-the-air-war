package hi.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import hi.client.CtawShaders;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThermalEntityRenderType extends RenderType {
    private static final Map<ResourceLocation, RenderType> CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> TRANSLUCENT_CACHE = new ConcurrentHashMap<>();

    public ThermalEntityRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    private static RenderType createThermal(String name, ResourceLocation texture) {
        return RenderType.create(
            name,
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(CtawShaders::getThermalEntityShader))
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                .setCullState(RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(true)
        );
    }

    private static RenderType createThermalTranslucent(String name, ResourceLocation texture) {
        return RenderType.create(
            name,
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            true,
            true,
            RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(CtawShaders::getThermalEntityShader))
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(true)
        );
    }

    public static RenderType getThermalEntity(ResourceLocation texture) {
        return CACHE.computeIfAbsent(texture, tex -> createThermal(
            "ctaw_thermal_entity_" + tex.getNamespace() + "_" + tex.getPath().replace('/', '_').replace('.', '_'),
            tex
        ));
    }

    public static RenderType getThermalEntityTranslucent(ResourceLocation texture) {
        return TRANSLUCENT_CACHE.computeIfAbsent(texture, tex -> createThermalTranslucent(
            "ctaw_thermal_entity_translucent_" + tex.getNamespace() + "_" + tex.getPath().replace('/', '_').replace('.', '_'),
            tex
        ));
    }

    public static final RenderType THERMAL_ENTITY = getThermalEntity(net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS);
}
