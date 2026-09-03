package hi.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import hi.CreateTheAirWarsMod;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@EventBusSubscriber(modid = CreateTheAirWarsMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CtawShaders {
    private static ShaderInstance cameraPostShader;
    private static ShaderInstance thermalEntityShader;

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
            new ShaderInstance(
                event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "camera_post").toString(),
                DefaultVertexFormat.POSITION_TEX
            ),
            shader -> cameraPostShader = shader
        );
        event.registerShader(
            new ShaderInstance(
                event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "thermal_entity").toString(),
                DefaultVertexFormat.NEW_ENTITY
            ),
            shader -> thermalEntityShader = shader
        );
    }

    public static ShaderInstance getCameraPostShader() {
        return cameraPostShader;
    }

    public static ShaderInstance getThermalEntityShader() {
        return thermalEntityShader;
    }

    public static ShaderInstance getThermalBlockShader() {
        return null;
    }
}
