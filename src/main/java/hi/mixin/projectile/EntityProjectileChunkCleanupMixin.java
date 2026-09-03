package hi.mixin.projectile;

import hi.CreateTheAirWarsMod;
import hi.util.ProjectileChunkLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityProjectileChunkCleanupMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void ctaw$releaseProjectileChunks(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof AbstractArrow projectile
            && CreateTheAirWarsMod.MODID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace())) {
            ProjectileChunkLoader.release(projectile);
        }
    }
}
