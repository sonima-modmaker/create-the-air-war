package hi.client.camera;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

public class FramebufferTexture extends AbstractTexture {
    private final RenderTarget renderTarget;

    public FramebufferTexture(RenderTarget renderTarget) {
        this.renderTarget = renderTarget;
    }

    @Override
    public void load(ResourceManager resourceManager) {
        // No-op
    }

    @Override
    public int getId() {
        return this.renderTarget.getColorTextureId();
    }
}
