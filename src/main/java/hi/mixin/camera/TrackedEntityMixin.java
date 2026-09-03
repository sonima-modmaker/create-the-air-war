package hi.mixin.camera;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public class TrackedEntityMixin {
    private static java.lang.reflect.Field entityField;
    private static java.lang.reflect.Field seenPlayersField;
    private static java.lang.reflect.Field serverEntityField;
    private static java.lang.reflect.Method addPairingMethod;

    static {
        try {
            Class<?> clazz = Class.forName("net.minecraft.server.level.ChunkMap$TrackedEntity");
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                if (field.getType() == net.minecraft.world.entity.Entity.class) {
                    field.setAccessible(true);
                    entityField = field;
                } else if (field.getType() == java.util.Set.class) {
                    field.setAccessible(true);
                    seenPlayersField = field;
                } else if (field.getType() == net.minecraft.server.level.ServerEntity.class) {
                    field.setAccessible(true);
                    serverEntityField = field;
                }
            }
            
            for (java.lang.reflect.Method m : net.minecraft.server.level.ServerEntity.class.getDeclaredMethods()) {
                if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == net.minecraft.server.level.ServerPlayer.class) {
                    String name = m.getName();
                    if (name.equals("addPairing") || name.equals("m_8541_")) {
                        m.setAccessible(true);
                        addPairingMethod = m;
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            System.err.println("[CTAW] Failed to initialize TrackedEntityMixin reflection fields: " + t);
        }
    }

    @Inject(method = "updatePlayer", at = @At("HEAD"), cancellable = true, require = 0)
    public void ctaw$updatePlayerDev(ServerPlayer player, CallbackInfo ci) {
        ctaw$updatePlayerCommon(player, ci);
    }

    @Inject(method = "m_140485_", at = @At("HEAD"), cancellable = true, require = 0)
    public void ctaw$updatePlayerProd(ServerPlayer player, CallbackInfo ci) {
        ctaw$updatePlayerCommon(player, ci);
    }

    private void ctaw$updatePlayerCommon(ServerPlayer player, CallbackInfo ci) {
        try {
            if (entityField != null && seenPlayersField != null && serverEntityField != null && addPairingMethod != null) {
                net.minecraft.world.entity.Entity entity = (net.minecraft.world.entity.Entity) entityField.get(this);
                if (player != entity) {
                    net.minecraft.world.level.ChunkPos entityChunk = entity.chunkPosition();
                    if (hi.util.CameraChunkTracker.isTracking(player, entityChunk.x, entityChunk.z)) {
                        Set seenPlayers = (Set) seenPlayersField.get(this);
                        net.minecraft.server.level.ServerEntity serverEntity = (net.minecraft.server.level.ServerEntity) serverEntityField.get(this);
                        if (player.connection != null && seenPlayers.add(player.connection)) {
                            System.out.println("[CTAW-Track] Forcing entity tracking for " + entity.getName().getString() + " (" + entity.getClass().getSimpleName() + ") at chunk " + entityChunk + " for " + player.getName().getString());
                            addPairingMethod.invoke(serverEntity, player);
                        }
                        ci.cancel();
                    }
                }
            }
        } catch (Throwable t) {
            System.err.println("[CTAW] Error in TrackedEntityMixin: " + t);
        }
    }
}
