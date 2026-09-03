package hi.client;

import hi.client.gui.RecipeDevScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class RecipeDevClientScreenOpener {
    private RecipeDevClientScreenOpener() {
    }

    public static void open() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        minecraft.setScreen(new RecipeDevScreen());
    }
}
