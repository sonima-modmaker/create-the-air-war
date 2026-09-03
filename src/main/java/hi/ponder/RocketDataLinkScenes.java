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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class RocketDataLinkScenes {

    public static void intro(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("rocket_data_link", "Using the Rocket Data Link");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        // Positions from NBT structure (5x2x5):
        // (1,1,1) - Rocket Data Link
        // (2,1,1) - Lever
        // (2,1,2) - Cogwheel
        // (2,1,3) - Creative Motor
        // (3,1,1) - Rocket Engine (facing north)
        // (3,1,2) - Mechanical Pump
        // (3,1,3) - Fluid Tank with lava
        
        BlockPos dataLinkPos = util.grid().at(1, 1, 1);
        BlockPos leverPos = util.grid().at(2, 1, 1);
        BlockPos enginePos = util.grid().at(3, 1, 1);
        BlockPos pumpPos = util.grid().at(3, 1, 2);
        BlockPos tankPos = util.grid().at(3, 1, 3);
        BlockPos motorPos = util.grid().at(2, 1, 3);
        BlockPos cogwheelPos = util.grid().at(2, 1, 2);

        Selection dataLinkSelect = util.select().position(dataLinkPos);
        Selection engineSelect = util.select().position(enginePos);
        Selection tankSelect = util.select().position(tankPos);
        Selection pumpSelect = util.select().position(pumpPos);
        Selection leverSelect = util.select().position(leverPos);
        Selection fuelSystem = util.select().fromTo(3, 1, 2, 3, 1, 3);
        Selection kineticSystem = util.select().fromTo(2, 1, 2, 2, 1, 3);

        // Show structure in parts
        scene.world().showSection(util.select().fromTo(0, 1, 0, 4, 1, 4), Direction.DOWN);
        scene.idle(20);

        // Step 1: Introduce the Rocket Engine
        scene.overlay().showOutline(PonderPalette.GREEN, "engine", engineSelect, 80);
        scene.overlay().showText(80)
            .text("The Rocket Engine provides thrust for Valkyrien Skies ships")
            .attachKeyFrame()
            .pointAt(util.vector().centerOf(enginePos))
            .placeNearTarget();
        scene.idle(90);

        // Step 2: Fuel system explanation
        scene.overlay().showOutline(PonderPalette.BLUE, "fuel", fuelSystem, 80);
        scene.overlay().showText(80)
            .text("Fill it with Lava using a Fluid Tank and Mechanical Pump")
            .attachKeyFrame()
            .pointAt(util.vector().centerOf(tankPos))
            .placeNearTarget();
        scene.idle(90);

        // Step 3: Data Link introduction
        scene.overlay().showOutline(PonderPalette.OUTPUT, "datalink", dataLinkSelect, 80);
        scene.overlay().showText(80)
            .text("The Rocket Data Link monitors and controls multiple engines")
            .attachKeyFrame()
            .pointAt(util.vector().centerOf(dataLinkPos))
            .placeNearTarget();
        scene.idle(90);

        // Step 4: Get linking tool
        scene.overlay().showControls(util.vector().centerOf(dataLinkPos), Pointing.DOWN, 50)
            .rightClick()
            .withItem(new ItemStack(CreateTheAirWarsModBlocks.ROCKET_DATA_LINK.get()));
        scene.idle(10);

        scene.overlay().showText(70)
            .text("Right-click to get the linking tool")
            .pointAt(util.vector().centerOf(dataLinkPos))
            .placeNearTarget();
        scene.idle(80);

        // Step 5: Link engine
        scene.overlay().showControls(util.vector().centerOf(enginePos), Pointing.DOWN, 50)
            .rightClick()
            .withItem(new ItemStack(CreateTheAirWarsModBlocks.ROCKET_DATA_LINK.get()));
        scene.idle(10);

        scene.overlay().showOutline(PonderPalette.GREEN, "linked", engineSelect, 70);
        scene.overlay().showText(70)
            .text("Click engines to link them to the Data Link")
            .colored(PonderPalette.GREEN)
            .pointAt(util.vector().centerOf(enginePos))
            .placeNearTarget();
        scene.idle(80);

        // Step 6: Open interface
        scene.overlay().showControls(util.vector().centerOf(dataLinkPos), Pointing.DOWN, 50)
            .rightClick();
        scene.idle(10);

        scene.overlay().showText(80)
            .text("Right-click Data Link again to open the control panel")
            .attachKeyFrame()
            .pointAt(util.vector().centerOf(dataLinkPos))
            .placeNearTarget();
        scene.idle(90);

        // Step 7: Redstone control with lever - Data Link передаёт сигнал
        scene.overlay().showOutline(PonderPalette.RED, "lever", leverSelect, 60);
        scene.overlay().showOutline(PonderPalette.OUTPUT, "datalink2", dataLinkSelect, 60);
        scene.overlay().showText(80)
            .text("Redstone signal to Data Link controls ALL linked engines!")
            .attachKeyFrame()
            .colored(PonderPalette.RED)
            .pointAt(util.vector().centerOf(dataLinkPos))
            .placeNearTarget();
        scene.idle(40);

        // Activate lever
        scene.world().modifyBlock(leverPos, s -> s.setValue(LeverBlock.POWERED, true), false);
        scene.effects().indicateRedstone(leverPos);
        scene.effects().indicateRedstone(dataLinkPos);
        scene.idle(10);

        // Engine activates
        scene.world().modifyBlock(enginePos, s -> s
            .setValue(RocketEngineBlock.ACTIVE, true)
            .setValue(RocketEngineBlock.POWERED, true), false);
        scene.effects().indicateSuccess(enginePos);
        scene.idle(60);

        // Step 8: Analog control explanation
        scene.overlay().showText(100)
            .text("Signal strength controls thrust of ALL engines at once!")
            .attachKeyFrame()
            .colored(PonderPalette.OUTPUT)
            .pointAt(util.vector().centerOf(dataLinkPos))
            .placeNearTarget();
        scene.idle(60);

        scene.overlay().showText(80)
            .text("1 = 7% | 8 = 53% | 15 = 100%")
            .colored(PonderPalette.OUTPUT)
            .pointAt(util.vector().centerOf(enginePos))
            .placeNearTarget();
        scene.idle(90);

        // Deactivate
        scene.world().modifyBlock(leverPos, s -> s.setValue(LeverBlock.POWERED, false), false);
        scene.world().modifyBlock(enginePos, s -> s
            .setValue(RocketEngineBlock.ACTIVE, false)
            .setValue(RocketEngineBlock.POWERED, false), false);
        scene.idle(20);

        // Final tip
        scene.overlay().showText(80)
            .text("Monitor fuel levels and engine status from the interface!")
            .attachKeyFrame()
            .colored(PonderPalette.BLUE)
            .pointAt(util.vector().centerOf(dataLinkPos))
            .placeNearTarget();
        scene.idle(90);
    }
}
