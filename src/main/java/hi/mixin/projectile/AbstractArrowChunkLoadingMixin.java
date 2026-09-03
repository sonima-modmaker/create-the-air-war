package hi.mixin.projectile;

import hi.CreateTheAirWarsMod;
import hi.util.ProjectileChunkLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowChunkLoadingMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void ctaw$keepProjectileChunksLoaded(CallbackInfo ci) {
        AbstractArrow projectile = (AbstractArrow) (Object) this;
        if (isCtawProjectile(projectile)) {
            ProjectileChunkLoader.update(projectile);
        }
    }

    private static boolean isCtawProjectile(AbstractArrow projectile) {
        return CreateTheAirWarsMod.MODID.equals(BuiltInRegistries.ENTITY_TYPE.getKey(projectile.getType()).getNamespace());
    }
}
