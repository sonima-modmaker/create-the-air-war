package hi.client;

import hi.CreateTheAirWarsMod;
import hi.client.sound.Aim9xSeekerSoundManager;
import hi.client.sound.UnifiedFlightSoundManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = CreateTheAirWarsMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        hi.client.radar.ClientMissileTracker.clientTick();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            RocketDataLinkOutlineHandler.clientTick();
            UnifiedFlightSoundManager.clientTick();
            Aim9xSeekerSoundManager.clientTick();
            return;
        }

        switch ((int) (minecraft.level.getGameTime() & 3L)) {
            case 0 -> UnifiedFlightSoundManager.clientTick();
            case 1 -> UnifiedFlightSoundManager.clientTick();
            case 2 -> Aim9xSeekerSoundManager.clientTick();
            default -> RocketDataLinkOutlineHandler.clientTick();
        }
    }
}
