package hi.mixin.camera;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkMap.class)
public interface ChunkMapAccessor {
    @Invoker("markChunkPendingToSend")
    void ctaw$markChunkPendingToSend(ServerPlayer player, ChunkPos chunkPos);

    @Invoker("dropChunk")
    static void ctaw$dropChunk(ServerPlayer player, ChunkPos chunkPos) {
        throw new UnsupportedOperationException();
    }
}
