package hi.mixin.camera;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/** A sparse second chunk store for camera chunks outside the player's square cache. */
@Mixin(ClientChunkCache.class)
public abstract class ClientChunkCacheMixin {
    @Unique private final Map<Long, LevelChunk> ctaw$remoteCameraChunks = new HashMap<>();
    @Unique private int ctaw$primaryCenterX;
    @Unique private int ctaw$primaryCenterZ;
    @Unique private int ctaw$primaryRadius;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ctaw$rememberInitialRange(ClientLevel level, int viewDistance, CallbackInfo ci) {
        ctaw$primaryRadius = Math.max(2, viewDistance) + 3;
    }

    @Inject(method = "updateViewCenter", at = @At("HEAD"))
    private void ctaw$rememberViewCenter(int x, int z, CallbackInfo ci) {
        ctaw$primaryCenterX = x;
        ctaw$primaryCenterZ = z;
    }

    @Inject(method = "updateViewRadius", at = @At("HEAD"))
    private void ctaw$rememberViewRadius(int viewDistance, CallbackInfo ci) {
        ctaw$primaryRadius = Math.max(2, viewDistance) + 3;
    }

    @Inject(method = "getChunk", at = @At("HEAD"), cancellable = true)
    private void ctaw$getRemoteChunk(int x, int z, ChunkStatus status, boolean required,
                                     CallbackInfoReturnable<LevelChunk> cir) {
        if (ctaw$inPrimaryRange(x, z)) return;
        LevelChunk chunk = ctaw$remoteCameraChunks.get(ChunkPos.asLong(x, z));
        if (chunk != null) cir.setReturnValue(chunk);
    }

    @Inject(method = "replaceWithPacketData", at = @At("HEAD"), cancellable = true)
    private void ctaw$acceptRemoteChunk(int x, int z, FriendlyByteBuf buffer, CompoundTag tag,
                                        Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> output,
                                        CallbackInfoReturnable<LevelChunk> cir) {
        long key = ChunkPos.asLong(x, z);
        ClientLevel level = ((ClientChunkCacheAccessor) this).ctaw$getLevel();
        if (ctaw$inPrimaryRange(x, z)) {
            LevelChunk oldRemote = ctaw$remoteCameraChunks.remove(key);
            if (oldRemote != null) level.unload(oldRemote);
            return;
        }

        LevelChunk chunk = ctaw$remoteCameraChunks.get(key);
        boolean newlyLoaded = chunk == null;
        if (newlyLoaded) {
            chunk = new LevelChunk(level, new ChunkPos(x, z));
            ctaw$remoteCameraChunks.put(key, chunk);
        }
        chunk.replaceWithPacketData(buffer, tag, output);
        level.onChunkLoaded(new ChunkPos(x, z));
        if (newlyLoaded) NeoForge.EVENT_BUS.post(new ChunkEvent.Load(chunk, false));
        cir.setReturnValue(chunk);
    }

    @Inject(method = "replaceBiomes", at = @At("HEAD"), cancellable = true)
    private void ctaw$replaceRemoteBiomes(int x, int z, FriendlyByteBuf buffer, CallbackInfo ci) {
        if (ctaw$inPrimaryRange(x, z)) return;
        LevelChunk chunk = ctaw$remoteCameraChunks.get(ChunkPos.asLong(x, z));
        if (chunk != null) {
            chunk.replaceBiomes(buffer);
            ci.cancel();
        }
    }

    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    private void ctaw$dropRemoteChunk(ChunkPos pos, CallbackInfo ci) {
        LevelChunk chunk = ctaw$remoteCameraChunks.remove(pos.toLong());
        if (chunk == null) return;
        NeoForge.EVENT_BUS.post(new ChunkEvent.Unload(chunk));
        ((ClientChunkCacheAccessor) this).ctaw$getLevel().unload(chunk);
        ci.cancel();
    }

    @Inject(method = "getLoadedChunksCount", at = @At("RETURN"), cancellable = true)
    private void ctaw$countRemoteChunks(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() + ctaw$remoteCameraChunks.size());
    }

    @Unique
    private boolean ctaw$inPrimaryRange(int x, int z) {
        return Math.abs(x - ctaw$primaryCenterX) <= ctaw$primaryRadius
            && Math.abs(z - ctaw$primaryCenterZ) <= ctaw$primaryRadius;
    }
}
