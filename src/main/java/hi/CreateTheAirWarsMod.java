package hi;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.BiConsumer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.AbstractMap;

import hi.init.CreateTheAirWarsModTabs;
import hi.init.CreateTheAirWarsModSounds;
import hi.init.CreateTheAirWarsModParticleTypes;
import hi.init.CreateTheAirWarsModMobEffects;
import hi.init.CreateTheAirWarsModMenus;
import hi.init.CreateTheAirWarsModExtraMenus;
import hi.init.CreateTheAirWarsModItems;
import hi.init.CreateTheAirWarsModFluids;
import hi.init.CreateTheAirWarsModFluidTypes;
import hi.init.CreateTheAirWarsModEntities;
import hi.init.CreateTheAirWarsModBlocks;
import hi.init.CreateTheAirWarsModBlockEntities;
import hi.config.CameraMonitorServerConfig;
import hi.config.RadarServerConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.Mod;

@Mod("create_the_air_wars")
public class CreateTheAirWarsMod {
	public static final Logger LOGGER = LogManager.getLogger(CreateTheAirWarsMod.class);
	public static final String MODID = "create_the_air_wars";

	public CreateTheAirWarsMod(net.neoforged.bus.api.IEventBus modEventBus, ModContainer modContainer) {
		// Start of user code block mod constructor
		// End of user code block mod constructor
		net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(this);
		modContainer.registerConfig(ModConfig.Type.COMMON, CameraMonitorServerConfig.SPEC, "create_the_air_wars-server.toml");
		modContainer.registerConfig(ModConfig.Type.SERVER, RadarServerConfig.SPEC, "create_the_air_wars-radar.toml");
		modEventBus.addListener(this::registerNetworking);
		net.neoforged.bus.api.IEventBus bus = modEventBus;
		CreateTheAirWarsModSounds.REGISTRY.register(bus);
		CreateTheAirWarsModBlocks.REGISTRY.register(bus);
		CreateTheAirWarsModBlockEntities.REGISTRY.register(bus);
		CreateTheAirWarsModItems.REGISTRY.register(bus);
		CreateTheAirWarsModEntities.REGISTRY.register(bus);
		hi.init.CreateTheAirWarsModForceGroups.REGISTRY.register(bus);

		CreateTheAirWarsModTabs.REGISTRY.register(bus);

		CreateTheAirWarsModMobEffects.REGISTRY.register(bus);

		CreateTheAirWarsModParticleTypes.REGISTRY.register(bus);

		CreateTheAirWarsModMenus.REGISTRY.register(bus);
		CreateTheAirWarsModExtraMenus.REGISTRY.register(bus);
		CreateTheAirWarsModFluids.REGISTRY.register(bus);
		CreateTheAirWarsModFluidTypes.REGISTRY.register(bus);
		// CreateAirwork.bootstrap(bus);
		bus.addListener(this::commonSetupAirwork);

		// Start of user code block mod init
		// End of user code block mod init
	}

	private void commonSetupAirwork(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			// ModFluids.setupFluidProperties();
		});
	}

	// Start of user code block mod methods
	// End of user code block mod methods
	private void registerNetworking(final net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
		final net.neoforged.neoforge.network.registration.PayloadRegistrar registrar = event.registrar(MODID);
		registrar.playToServer(hi.network.UfyButtonMessage.TYPE, hi.network.UfyButtonMessage.STREAM_CODEC, hi.network.UfyButtonMessage::handleData);
		registrar.playToServer(hi.network.FdgddButtonMessage.TYPE, hi.network.FdgddButtonMessage.STREAM_CODEC, hi.network.FdgddButtonMessage::handleData);
		registrar.playToServer(hi.network.DebugOpenMenuMessage.TYPE, hi.network.DebugOpenMenuMessage.STREAM_CODEC, hi.network.DebugOpenMenuMessage::handleData);
		registrar.playToServer(hi.network.DebugRecipeSaveMessage.TYPE, hi.network.DebugRecipeSaveMessage.STREAM_CODEC, hi.network.DebugRecipeSaveMessage::handleData);
		registrar.playToServer(hi.network.RecipeDevSavePacket.TYPE, hi.network.RecipeDevSavePacket.STREAM_CODEC, hi.network.RecipeDevSavePacket::handleData);
		registrar.playToServer(hi.network.RocketDataLinkPacket.TYPE, hi.network.RocketDataLinkPacket.STREAM_CODEC, hi.network.RocketDataLinkPacket::handleData);
		registrar.playToServer(hi.network.CameraAdjustPacket.TYPE, hi.network.CameraAdjustPacket.STREAM_CODEC, hi.network.CameraAdjustPacket::handleData);
		registrar.playToServer(hi.network.CameraLockPacket.TYPE, hi.network.CameraLockPacket.STREAM_CODEC, hi.network.CameraLockPacket::handleData);
		registrar.playToServer(hi.network.VihrLaunchPacket.TYPE, hi.network.VihrLaunchPacket.STREAM_CODEC, hi.network.VihrLaunchPacket::handleData);
		registrar.playToServer(hi.network.VihrReloadPacket.TYPE, hi.network.VihrReloadPacket.STREAM_CODEC, hi.network.VihrReloadPacket::handleData);
		registrar.playToServer(hi.network.AntiAircraftLauncherUpdatePacket.TYPE, hi.network.AntiAircraftLauncherUpdatePacket.STREAM_CODEC, hi.network.AntiAircraftLauncherUpdatePacket::handleData);
		registrar.playToServer(hi.network.FpvDroneControlPacket.TYPE, hi.network.FpvDroneControlPacket.STREAM_CODEC, hi.network.FpvDroneControlPacket::handleData);
		registrar.playToClient(hi.network.CameraMonitorSettingsPacket.TYPE, hi.network.CameraMonitorSettingsPacket.STREAM_CODEC, hi.network.CameraMonitorSettingsPacket::handleData);
		registrar.playToClient(hi.network.RecipeDevOpenScreenPacket.TYPE, hi.network.RecipeDevOpenScreenPacket.STREAM_CODEC, hi.network.RecipeDevOpenScreenPacket::handleData);
		registrar.playToClient(hi.network.ScreenshakePacket.TYPE, hi.network.ScreenshakePacket.STREAM_CODEC, hi.network.ScreenshakePacket::handleData);
		registrar.playToClient(hi.network.MtsExplosionEffectPacket.TYPE, hi.network.MtsExplosionEffectPacket.STREAM_CODEC, hi.network.MtsExplosionEffectPacket::handleData);
		registrar.playToClient(hi.network.MissileRadarSyncPacket.TYPE, hi.network.MissileRadarSyncPacket.STREAM_CODEC, hi.network.MissileRadarSyncPacket::handleData);
	}

	private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();
	private int missileSyncCounter = 0;

	public static void queueServerWork(int tick, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
	}

	@SubscribeEvent
	public void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Pre event) {
		if (true) {
			List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
			workQueue.forEach(work -> {
				work.setValue(work.getValue() - 1);
				if (work.getValue() == 0)
					actions.add(work);
			});
			actions.forEach(e -> e.getKey().run());
			workQueue.removeAll(actions);
		}

		if (++missileSyncCounter >= 3) {
			missileSyncCounter = 0;
			broadcastActiveMissiles(event.getServer());
		}
	}

	private void broadcastActiveMissiles(net.minecraft.server.MinecraftServer server) {
		if (server == null) return;
		List<hi.network.MissileRadarSyncPacket.MissileData> list = new ArrayList<>();
		for (net.minecraft.server.level.ServerLevel level : server.getAllLevels()) {
			for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
				if (entity.isAlive() && (entity instanceof hi.entity.X25mlEntity || entity instanceof hi.entity.TomahawkbultEntity || entity instanceof hi.entity.C75RocketEntity || entity instanceof hi.entity.VihrRocketEntity)) {
					net.minecraft.world.phys.Vec3 pos = entity.position();
					net.minecraft.world.phys.Vec3 vel = entity.getDeltaMovement();
					list.add(new hi.network.MissileRadarSyncPacket.MissileData(
						entity.getUUID(),
						entity.getType(),
						pos.x, pos.y, pos.z,
						vel.x, vel.y, vel.z,
						true
					));
				}
			}
		}
		if (!list.isEmpty() || hasActiveClientTrackers(server)) {
			net.neoforged.neoforge.network.PacketDistributor.sendToAllPlayers(new hi.network.MissileRadarSyncPacket(list));
		}
	}

	private static boolean lastSyncHadMissiles = false;
	private boolean hasActiveClientTrackers(net.minecraft.server.MinecraftServer server) {
		boolean had = lastSyncHadMissiles;
		lastSyncHadMissiles = false;
		return had;
	}
}
