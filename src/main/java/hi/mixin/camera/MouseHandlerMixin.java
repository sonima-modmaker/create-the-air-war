package hi.mixin.camera;

import hi.client.camera.CameraMonitorClientHandler;
import hi.client.DroneControllerClientHandler;
import hi.network.CameraAdjustPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void onTurnPlayer(CallbackInfo ci) {
        if (DroneControllerClientHandler.captureMouseTurn(this.accumulatedDX, this.accumulatedDY)) {
            this.accumulatedDX = 0.0D;
            this.accumulatedDY = 0.0D;
            ci.cancel();
            return;
        }
        if (CameraMonitorClientHandler.hasActiveMonitor()) {
            double dx = this.accumulatedDX;
            double dy = this.accumulatedDY;
            this.accumulatedDX = 0.0D;
            this.accumulatedDY = 0.0D;

            if (dx != 0.0D || dy != 0.0D) {
                Minecraft minecraft = Minecraft.getInstance();
                double sensitivityVal = minecraft.options.sensitivity().get();
                float sensitivity = (float) sensitivityVal;
                float yawDelta = (float) (dx * sensitivity * 0.15F);
                float pitchDelta = (float) (dy * sensitivity * 0.15F);

                if (minecraft.options.invertYMouse().get()) {
                    pitchDelta = -pitchDelta;
                }

                if (CameraMonitorClientHandler.getActiveCameraPos() != null) {
                    PacketDistributor.sendToServer(new CameraAdjustPacket(CameraMonitorClientHandler.getActiveCameraPos(), yawDelta, pitchDelta));
                }
            }
            ci.cancel();
        }
    }
}
