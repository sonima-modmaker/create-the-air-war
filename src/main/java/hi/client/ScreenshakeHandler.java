package hi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import hi.CreateTheAirWarsMod;

@EventBusSubscriber(modid = CreateTheAirWarsMod.MODID, value = Dist.CLIENT)
public class ScreenshakeHandler {
    public static double shakeTime = 0.0;
    public static double shakeRadius = 0.0;
    public static double shakeAmplitude = 0.0;
    public static final double[] shakePos = new double[3];
    public static double shakeType = 0.0;

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Player player = mc.player;
        if (player == null || player.isSpectator()) return;

        float delta = mc.getTimer().getGameTimeDeltaTicks();
        shakeTime = Mth.lerp(0.05f * delta, (float) shakeTime, 0.0f);

        if (shakeTime > 0) {
            double distance = player.position().distanceTo(new Vec3(shakePos[0], shakePos[1], shakePos[2]));
            if (distance < shakeRadius) {
                float shakeRadiusAmplitude = (float) (1.0 - (distance / shakeRadius));
                shakeRadiusAmplitude = Mth.clamp(shakeRadiusAmplitude, 0.0f, 1.0f);

                float sinFactor = (float) (shakeTime * Math.sin(0.5 * Math.PI * shakeTime));
                float strength = (float) (sinFactor * shakeAmplitude * shakeRadiusAmplitude * 3.0);

                boolean onVehicle = player.getVehicle() != null;
                double vehicleMultiplier = onVehicle ? 0.1 : 1.0;

                float yawChange = (float) (strength * shakeType * vehicleMultiplier);
                float pitchChange = (float) (strength * shakeType * vehicleMultiplier);
                float rollChange = (float) (strength * vehicleMultiplier);

                if (shakeType > 0) {
                    event.setYaw(event.getYaw() + yawChange);
                    event.setPitch(event.getPitch() - pitchChange);
                    event.setRoll(event.getRoll() - rollChange);
                } else {
                    event.setYaw(event.getYaw() - yawChange);
                    event.setPitch(event.getPitch() + pitchChange);
                    event.setRoll(event.getRoll() + rollChange);
                }
            }
        }
    }
}
