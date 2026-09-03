package hi.ponder;

import hi.init.CreateTheAirWarsModBlocks;
import hi.init.CreateTheAirWarsModParticleTypes;
import hi.entity.TomahawkbultEntity;
import hi.init.CreateTheAirWarsModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.minecraft.world.level.block.Blocks;
import net.createmod.catnip.math.Pointing;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;

public class TomahawkScenes {

    public static void intro(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("tomahawk_missile", "Using the Tomahawk Missile");
        scene.configureBasePlate(0, 0, 15);
        scene.scaleSceneView(0.6f); // Zoom out to show more area
        scene.setSceneOffsetY(-1.5f);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos launchPos = util.grid().at(1, 1, 7);
        scene.overlay().showText(60)
            .text("The Tomahawk Missile is a guided rocket")
            .pointAt(util.vector().topOf(launchPos))
            .placeNearTarget();
        
        scene.idle(20);

        ElementLink<EntityElement> missile = scene.world().createEntity(w -> {
            TomahawkbultEntity entity = CreateTheAirWarsModEntities.TOMAHAWKBULT.get().create(w);
            Vec3 v = util.vector().topOf(launchPos);
            entity.setPosRaw(v.x, v.y, v.z);
            return entity;
        });

        scene.idle(40);

        // Show obstacle
        BlockPos obstaclePos = util.grid().at(7, 2, 7);
        scene.world().setBlock(obstaclePos, Blocks.STONE.defaultBlockState(), false);
        scene.world().setBlock(obstaclePos.above(), Blocks.STONE.defaultBlockState(), false);
        scene.world().showSection(util.select().fromTo(7, 1, 7, 7, 3, 7), Direction.DOWN);
        
        scene.idle(20);
        scene.overlay().showText(80)
            .text("It can automatically navigate around obstacles")
            .pointAt(util.vector().topOf(obstaclePos))
            .placeNearTarget();
            
        scene.idle(40);

        // Animate flight and camera
        // Move to obstacle
        Vec3 start = util.vector().topOf(launchPos);
        Vec3 overObstacle = new Vec3(7.5, 4.5, 7.5);
        int steps = 60;
        for (int i = 1; i <= steps; i++) {
            double progress = (double) i / steps;
            double x = start.x + (overObstacle.x - start.x) * progress;
            double y = start.y + (overObstacle.y - start.y) * progress;
            double z = start.z + (overObstacle.z - start.z) * progress;
            scene.world().modifyEntity(missile, e -> e.setPos(x, y, z));
            
            // Add particles
            scene.addInstruction(s -> {
                s.getWorld().addParticle(CreateTheAirWarsModParticleTypes.MACHINEGUN_SMOKE.get(), x, y, z, 0, 0, 0);
            });
            
            scene.rotateCameraY(-0.2f); // cinematic pan
            scene.idle(1);
        }
        
        scene.idle(10);
        
        // Show polygon/target
        BlockPos targetPos = util.grid().at(13, 1, 7);
        scene.world().setBlock(targetPos, Blocks.RED_CONCRETE.defaultBlockState(), false);
        scene.world().showSection(util.select().position(targetPos), Direction.UP);
        
        scene.overlay().showText(60)
            .text("And strike designated target polygons")
            .pointAt(util.vector().topOf(targetPos))
            .placeNearTarget();
            
        scene.idle(40);

        // Impact
        Vec3 end = util.vector().centerOf(targetPos);
        int steps2 = 60;
        for (int i = 1; i <= steps2; i++) {
            double progress = (double) i / steps2;
            double x = overObstacle.x + (end.x - overObstacle.x) * progress;
            double y = overObstacle.y + (end.y - overObstacle.y) * progress;
            double z = overObstacle.z + (end.z - overObstacle.z) * progress;
            scene.world().modifyEntity(missile, e -> e.setPos(x, y, z));
            
            // Add particles
            scene.addInstruction(s -> {
                s.getWorld().addParticle(CreateTheAirWarsModParticleTypes.MACHINEGUN_SMOKE.get(), x, y, z, 0, 0, 0);
            });
            
            scene.rotateCameraY(-0.2f); // cinematic pan
            scene.idle(1);
        }
        
        // Explosion
        scene.world().modifyEntity(missile, Entity::discard);
        scene.world().setBlock(targetPos, Blocks.AIR.defaultBlockState(), true);
        
        scene.addInstruction(s -> {
            Vec3 v = util.vector().centerOf(targetPos);
            for(int i=0; i<10; i++) {
                s.getWorld().addParticle(CreateTheAirWarsModParticleTypes.EXLOSION.get(), v.x + (Math.random()-0.5), v.y + (Math.random()-0.5), v.z + (Math.random()-0.5), 0, 0, 0);
            }
        });

        scene.idle(40);
    }
}
