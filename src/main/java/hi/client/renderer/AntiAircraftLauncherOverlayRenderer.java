package hi.client.renderer;

import hi.CreateTheAirWarsMod;
import hi.block.entity.AntiAircraftLauncherBlockEntity;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = CreateTheAirWarsMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class AntiAircraftLauncherOverlayRenderer {
    private static final String OUTLINE_PREFIX = "create_the_air_wars_anti_aircraft_launcher_line_";

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) {
            clearLines();
            return;
        }

        if (!com.simibubi.create.content.equipment.goggles.GogglesItem.isWearingGoggles(player)) {
            clearLines();
            return;
        }

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) {
            clearLines();
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockEntity be = mc.level.getBlockEntity(pos);
        if (!(be instanceof AntiAircraftLauncherBlockEntity launcher)) {
            clearLines();
            return;
        }

        double angle = launcher.getDetectionAngle();
        double halfAngleRad = Math.toRadians(angle / 2.0D);
        double sinAngle = Math.sin(halfAngleRad);
        double cosAngle = Math.cos(halfAngleRad);
        double invSqrt2 = 0.70710678D;

        double[][] corners = {
            {-0.5D, -0.5D, -invSqrt2, -invSqrt2},
            {0.5D, -0.5D, invSqrt2, -invSqrt2},
            {0.5D, 0.5D, invSqrt2, invSqrt2},
            {-0.5D, 0.5D, -invSqrt2, invSqrt2}
        };

        double len = 4.0D;

        for (int i = 0; i < corners.length; i++) {
            double[] c = corners[i];
            double startX = 0.5D + c[0];
            double startZ = 0.5D + c[1];
            double startY = 1.0D;

            double dirX = sinAngle * c[2];
            double dirY = cosAngle;
            double dirZ = sinAngle * c[3];

            double endX = startX + dirX * len;
            double endY = startY + dirY * len;
            double endZ = startZ + dirZ * len;

            Vec3 start = new Vec3(pos.getX() + startX, pos.getY() + startY, pos.getZ() + startZ);
            Vec3 end = new Vec3(pos.getX() + endX, pos.getY() + endY, pos.getZ() + endZ);

            Outliner.getInstance()
                .showLine(OUTLINE_PREFIX + i, start, end)
                .colored(0xFFCB74)
                .lineWidth(1 / 16f)
                .disableCull();
        }
    }

    private static void clearLines() {
        for (int i = 0; i < 4; i++) {
            Outliner.getInstance().remove(OUTLINE_PREFIX + i);
        }
    }
}
