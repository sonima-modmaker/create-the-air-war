package hi.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import hi.block.RocketEngineBlock;
import hi.init.CreateTheAirWarsModBlocks;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RocketEngineScenes {

    public static void intro(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("rocket_engine", "Using the Rocket Engine");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        // Positions from NBT (5x2x5):
        // (1,1,3) - Analog Lever
        // (2,1,0) - Fluid Tank
        // (2,1,1) - Mechanical Pump (facing south)
        // (2,1,2) - Glass Fluid Pipe
        // (2,1,3) - Rocket Engine (facing south)
        // (3,1,0) - Creative Motor (facing south)
        // (3,1,1) - Cogwheel
        
        BlockPos enginePos = util.grid().at(2, 1, 3);
        BlockPos pipePos = util.grid().at(2, 1, 2);
        BlockPos pumpPos = util.grid().at(2, 1, 1);
        BlockPos tankPos = util.grid().at(2, 1, 0);
        BlockPos leverPos = util.grid().at(1, 1, 3);
        BlockPos motorPos = util.grid().at(3, 1, 0);
        BlockPos cogwheelPos = util.grid().at(3, 1, 1);

        Selection engineSelect = util.select().position(enginePos);
        Selection tankSelect = util.select().position(tankPos);
        Selection leverSelect = util.select().position(leverPos);
        Selection fuelSystem = util.select().fromTo(2, 1, 0, 2, 1, 2);
        Selection kineticSystem = util.select().fromTo(3, 1, 0, 3, 1, 1);

        // Show structure
        scene.world().showSection(util.select().fromTo(0, 1, 0, 4, 1, 4), Direction.DOWN);
        scene.idle(20);

        // Step 1: Introduce the Rocket Engine
        scene.overlay().showOutline(PonderPalette.GREEN, "engine", engineSelect, 100);
        scene.overlay().showText(100)
            .text("The Rocket Engine creates thrust for Valkyrien Skies ships")
            .attachKeyFrame()
            .pointAt(util.vector().centerOf(enginePos))
            .placeNearTarget();
        scene.idle(110);

        // Step 2: Fuel requirement
        scene.overlay().showOutline(PonderPalette.BLUE, "tank", tankSelect, 80);
        scene.overlay().showText(80)
            .text("It requires Rocket Fuel")
            .attachKeyFrame()
            .colored(PonderPalette.BLUE)
            .pointAt(util.vector().centerOf(tankPos))
            .placeNearTarget();
        scene.idle(90);

        // Step 3: Pump system
        scene.overlay().showOutline(PonderPalette.INPUT, "fuel", fuelSystem, 80);
        scene.overlay().showText(80)
            .text("Use a Mechanical Pump to feed Rocket Fuel from behind")
            .pointAt(util.vector().centerOf(pumpPos))
            .placeNearTarget();
        scene.idle(90);

        // Step 4: Manual start
        scene.overlay().showControls(util.vector().centerOf(enginePos), Pointing.DOWN, 60)
            .rightClick();
        scene.idle(20);

        scene.overlay().showText(80)
            .text("Right-click to manually start the engine")
            .attachKeyFrame()
            .pointAt(util.vector().centerOf(enginePos))
            .placeNearTarget();
        scene.idle(90);

        // Step 5: Analog Lever control
        scene.overlay().showOutline(PonderPalette.RED, "lever", leverSelect, 80);
        scene.overlay().showText(80)
            .text("Redstone signal activates thrust when engine is running")
            .attachKeyFrame()
            .colored(PonderPalette.RED)
            .pointAt(util.vector().centerOf(leverPos))
            .placeNearTarget();
        scene.idle(50);

        // Simulate lever activation
        scene.effects().indicateRedstone(leverPos);
        scene.idle(10);

        // Engine activates
        scene.world().modifyBlock(enginePos, s -> s
            .setValue(RocketEngineBlock.ACTIVE, true)
            .setValue(RocketEngineBlock.POWERED, true), false);
        scene.effects().indicateSuccess(enginePos);
        scene.idle(40);

        // Step 6: Analog control explanation
        scene.overlay().showText(100)
            .text("Signal strength controls thrust power:")
            .attachKeyFrame()
            .colored(PonderPalette.OUTPUT)
            .pointAt(util.vector().centerOf(leverPos))
            .placeNearTarget();
        scene.idle(60);

        scene.overlay().showText(80)
            .text("1 = 7% | 8 = 53% | 15 = 100%")
            .colored(PonderPalette.OUTPUT)
            .pointAt(util.vector().centerOf(enginePos))
            .placeNearTarget();
        scene.idle(90);

        // Step 7: Goggles info
        scene.overlay().showControls(util.vector().centerOf(enginePos), Pointing.DOWN, 60)
            .withItem(new ItemStack(Items.LEATHER_HELMET));
        scene.idle(20);

        scene.overlay().showText(80)
            .text("Wear Engineer's Goggles to see status and fuel level")
            .attachKeyFrame()
            .colored(PonderPalette.BLUE)
            .pointAt(util.vector().centerOf(enginePos))
            .placeNearTarget();
        scene.idle(90);

        // Deactivate
        scene.world().modifyBlock(enginePos, s -> s
            .setValue(RocketEngineBlock.ACTIVE, false)
            .setValue(RocketEngineBlock.POWERED, false), false);
        scene.idle(20);

        // Final tip
        scene.overlay().showText(80)
            .text("Use Rocket Data Link to control multiple engines at once!")
            .attachKeyFrame()
            .colored(PonderPalette.GREEN)
            .pointAt(util.vector().centerOf(enginePos))
            .placeNearTarget();
        scene.idle(90);
    }
}
