package hi.client;

import hi.block.entity.RocketDataLinkBlockEntity;
import hi.client.gui.RocketDataLinkScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class RocketDataLinkClientScreenOpener {
    private RocketDataLinkClientScreenOpener() {
    }

    public static void open(RocketDataLinkBlockEntity dataLink) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        minecraft.setScreen(new RocketDataLinkScreen(dataLink));
    }
}
