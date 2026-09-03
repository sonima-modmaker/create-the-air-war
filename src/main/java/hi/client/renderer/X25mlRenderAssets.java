package hi.client.renderer;

import hi.CreateTheAirWarsMod;
import net.minecraft.resources.ResourceLocation;

public final class X25mlRenderAssets {
    public static final ExactJsonModelRenderer BLOCK_MODEL = new ExactJsonModelRenderer(
        ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "models/custom/x25ml.json"),
        ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "textures/block/x25ml.png")
    );

    public static final ExactJsonModelRenderer ENTITY_MODEL = new ExactJsonModelRenderer(
        ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "models/custom/x25ml_entity.json"),
        ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "textures/block/x25ml.png")
    );

    private X25mlRenderAssets() {
    }
}
