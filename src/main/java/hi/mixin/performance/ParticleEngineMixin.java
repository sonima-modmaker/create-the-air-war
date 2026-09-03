package hi.mixin.performance;

import hi.client.performance.ClientPerformanceLimiter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void createTheAirWars$limitParticles(Particle effect, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (ClientPerformanceLimiter.shouldCullParticle(minecraft, effect)) {
            ci.cancel();
        }
    }
}
