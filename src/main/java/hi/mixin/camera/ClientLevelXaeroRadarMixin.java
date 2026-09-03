package hi.mixin.camera;

import hi.client.radar.ClientMissileTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mixin(ClientLevel.class)
public abstract class ClientLevelXaeroRadarMixin {
    @Inject(method = "entitiesForRendering", at = @At("RETURN"), cancellable = true, require = 0)
    private void ctaw$injectTrackedMissilesIntoXaeroRadar(CallbackInfoReturnable<Iterable<Entity>> cir) {
        List<Entity> tracked = ClientMissileTracker.getTrackedMissiles();
        if (tracked.isEmpty()) {
            return;
        }

        Iterable<Entity> original = cir.getReturnValue();
        if (original == null) {
            return;
        }

        Set<UUID> existingUUIDs = new HashSet<>();
        List<Entity> combined = new ArrayList<>();
        for (Entity e : original) {
            if (e != null) {
                combined.add(e);
                if (e.getUUID() != null && !e.getPersistentData().getBoolean(ClientMissileTracker.RADAR_VIRTUAL_TAG)) {
                    existingUUIDs.add(e.getUUID());
                }
            }
        }

        for (Entity m : tracked) {
            if (m != null && m.getUUID() != null && !existingUUIDs.contains(m.getUUID()) && !combined.contains(m)) {
                combined.add(m);
            }
        }
        cir.setReturnValue(combined);
    }
}
