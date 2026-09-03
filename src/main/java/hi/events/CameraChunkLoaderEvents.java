package hi.events;

import hi.CreateTheAirWarsMod;
import hi.util.CameraChunkTracker;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import hi.block.entity.MonitorBlockEntity;

@EventBusSubscriber(modid = CreateTheAirWarsMod.MODID)
public class CameraChunkLoaderEvents {
    @SubscribeEvent
    public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Five ticks are enough to refresh tickets and also guarantee that
            // breaking a monitor releases its camera chunks within 0.25 s.
            if (player.tickCount % 5 == 0) {
                CameraChunkTracker.serverTick(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CameraChunkTracker.playerLoggedOut(player);
        }
    }

    @SubscribeEvent
    public static void onChunkSent(net.neoforged.neoforge.event.level.ChunkWatchEvent.Sent event) {
        CameraChunkTracker.chunkSent(event.getPlayer(), event.getPos());
    }
}
