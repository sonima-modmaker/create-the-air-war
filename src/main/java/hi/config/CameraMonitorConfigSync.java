package hi.config;

import hi.CreateTheAirWarsMod;
import hi.network.CameraMonitorSettingsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(modid = CreateTheAirWarsMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class CameraMonitorConfigSync {
    private CameraMonitorConfigSync() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncToPlayer(serverPlayer);
        }
    }

    @EventBusSubscriber(modid = CreateTheAirWarsMod.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onConfigReloaded(ModConfigEvent.Reloading event) {
            if (event.getConfig().getSpec() != CameraMonitorServerConfig.SPEC || event.getConfig().getType() != ModConfig.Type.COMMON) {
                return;
            }
            syncToAll();
        }

        @SubscribeEvent
        public static void onConfigLoaded(ModConfigEvent.Loading event) {
            if (event.getConfig().getSpec() != CameraMonitorServerConfig.SPEC || event.getConfig().getType() != ModConfig.Type.COMMON) {
                return;
            }
            syncToAll();
        }
    }

    private static void syncToAll() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncToPlayer(player);
        }
    }

    public static void syncToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new CameraMonitorSettingsPacket(
            CameraMonitorServerConfig.getMonitorFeedFps(),
            CameraMonitorServerConfig.getMonitorFeedResolution(),
            CameraMonitorServerConfig.getMonitorFeedViewDistance()
        ));
    }
}
