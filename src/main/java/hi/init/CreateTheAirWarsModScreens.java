/*
 * MCreator note: This file will be REGENERATED on each build.
 */
package hi.init;

import hi.CreateTheAirWarsMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;

@net.neoforged.fml.common.EventBusSubscriber(modid = CreateTheAirWarsMod.MODID, bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CreateTheAirWarsModScreens {
    public static void registerParticleProviders(net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event) {
    }

    @SubscribeEvent
    public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(hi.init.CreateTheAirWarsModMenus.ANTI_AIRCRAFT_LAUNCHER.get(), hi.client.gui.AntiAircraftLauncherScreen::new);
    }
}
