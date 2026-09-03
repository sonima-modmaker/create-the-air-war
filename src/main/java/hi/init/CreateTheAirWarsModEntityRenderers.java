
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package hi.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.api.distmarker.Dist;

import hi.client.renderer.MachinegunShellRenderer;
import hi.client.renderer.OrientedProjectileItemRenderer;
import hi.client.renderer.HeattrapFlareRenderer;
import hi.client.renderer.VihrRocketEntityRenderer;
import hi.client.renderer.X25mlEntityRenderer;
import hi.client.renderer.C75RocketRenderer;
import hi.client.renderer.FpvDroneRenderer;
import hi.client.model.MachinegunShellModel;

@net.neoforged.fml.common.EventBusSubscriber(bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CreateTheAirWarsModEntityRenderers {
	@SubscribeEvent
	public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(CreateTheAirWarsModBlockEntities.GYRO_STABILIZER.get(),
			hi.client.renderer.GyroStabilizerRenderer::new);
	}

	@SubscribeEvent
	public static void registerAdditionalModels(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional event) {
		event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
			net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/gyro_stabilizer_flywheel_active"), "standalone"));
		event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
			net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("create_the_air_wars", "block/gyro_stabilizer_flywheel_deactive"), "standalone"));
	}

	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(CreateTheAirWarsModEntities.C_3KTRUE.get(), OrientedProjectileItemRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.MACHINEGUN_SHELL.get(), MachinegunShellRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.GVRDCRCD.get(), OrientedProjectileItemRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.C_25.get(), OrientedProjectileItemRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.RIM_7ACTVBULT.get(), OrientedProjectileItemRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.AIM9XBULT.get(), OrientedProjectileItemRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.NINEK_119MACTVBULT.get(), OrientedProjectileItemRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.C_25ACTVBULT.get(), OrientedProjectileItemRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.M_24BULT.get(), OrientedProjectileItemRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.TOMAHAWKBULT.get(), OrientedProjectileItemRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.BGGH.get(), OrientedProjectileItemRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.FAB_3000TRUE.get(), OrientedProjectileItemRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.ASTRK.get(), OrientedProjectileItemRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.HEATTRAP_FLARE.get(), HeattrapFlareRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.VIHR_ROCKET.get(), OrientedProjectileItemRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.X25ML_MISSILE.get(), X25mlEntityRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.C75_ROCKET.get(), C75RocketRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.FPV_DRONE.get(), FpvDroneRenderer::new);
		event.registerEntityRenderer(CreateTheAirWarsModEntities.UXO_ENTITY.get(), hi.client.renderer.UxoRenderer::new);
	}

	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(MachinegunShellModel.LAYER_LOCATION, MachinegunShellModel::createBodyLayer);
	}
}
