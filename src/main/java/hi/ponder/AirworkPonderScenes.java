package hi.ponder;

import hi.init.CreateTheAirWarsModBlocks;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

public class AirworkPonderScenes {

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<DeferredBlock<? extends Block>> HELPER = 
            helper.withKeyFunction(DeferredBlock::getId);

        HELPER.forComponents(CreateTheAirWarsModBlocks.ROCKET_DATA_LINK)
            .addStoryBoard("rocket_data_link/intro", RocketDataLinkScenes::intro);

        HELPER.forComponents(CreateTheAirWarsModBlocks.ROCKET_ENGINE)
            .addStoryBoard("rocket_engine/intro", RocketEngineScenes::intro);
    }
}
