package hi.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import java.lang.reflect.Proxy;
import java.lang.reflect.Constructor;

import hi.block.entity.TestBlockEntity;
import hi.block.entity.TomahawkBlockEntity;
import hi.block.entity.MachinegunBlockEntity;
import hi.block.entity.MinigunBlockEntity;
import hi.block.entity.CameraBlockEntity;
import hi.block.entity.MonitorBlockEntity;
import hi.block.entity.VihrLauncherBlockEntity;
import hi.block.entity.X25mlBlockEntity;
import hi.CreateTheAirWarsMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = CreateTheAirWarsMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CreateTheAirWarsModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, CreateTheAirWarsMod.MODID);
	public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>, net.minecraft.world.level.block.entity.BlockEntityType<TomahawkBlockEntity>> TOMAHAWK = REGISTRY.register("tomahawk",
			() -> createType(TomahawkBlockEntity::new, CreateTheAirWarsModBlocks.TOMAHAWK.get()));
	public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>, net.minecraft.world.level.block.entity.BlockEntityType<MachinegunBlockEntity>> MACHINEGUN = REGISTRY.register("machinegun",
			() -> createType(MachinegunBlockEntity::new, CreateTheAirWarsModBlocks.MACHINEGUN.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MinigunBlockEntity>> MINIGUN = REGISTRY.register("minigun",
			() -> createType(MinigunBlockEntity::new, CreateTheAirWarsModBlocks.MINIGUN.get()));
	public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>, net.minecraft.world.level.block.entity.BlockEntityType<TestBlockEntity>> TEST = REGISTRY.register("test",
			() -> createType(TestBlockEntity::new, CreateTheAirWarsModBlocks.TEST.get()));
	public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>, net.minecraft.world.level.block.entity.BlockEntityType<CameraBlockEntity>> CAMERA = REGISTRY.register("camera",
			() -> createType(CameraBlockEntity::new, CreateTheAirWarsModBlocks.CAMERA.get()));
	public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>, net.minecraft.world.level.block.entity.BlockEntityType<MonitorBlockEntity>> MONITOR = REGISTRY.register("monitor",
			() -> createType(MonitorBlockEntity::new, CreateTheAirWarsModBlocks.MONITOR.get()));
	public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>, net.minecraft.world.level.block.entity.BlockEntityType<VihrLauncherBlockEntity>> VIHR = REGISTRY.register("vihr",
			() -> createType(VihrLauncherBlockEntity::new, CreateTheAirWarsModBlocks.VIHR.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<X25mlBlockEntity>> X25ML = REGISTRY.register("x25ml",
			() -> createType(X25mlBlockEntity::new, CreateTheAirWarsModBlocks.X25ML.get()));
	public static final DeferredHolder<net.minecraft.world.level.block.entity.BlockEntityType<?>, net.minecraft.world.level.block.entity.BlockEntityType<hi.block.entity.HeattrapBlockEntity>> HEATTRAP = REGISTRY.register("heattrap",
			() -> createType(hi.block.entity.HeattrapBlockEntity::new, CreateTheAirWarsModBlocks.HEATTRAP.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<hi.block.entity.GyroStabilizerBlockEntity>> GYRO_STABILIZER = REGISTRY.register("gyro_stabilizer",
			() -> createType(hi.block.entity.GyroStabilizerBlockEntity::new, CreateTheAirWarsModBlocks.GYRO_STABILIZER.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<hi.block.entity.LauncherCrashBlockEntity>> LAUNCHER_CRASH = REGISTRY.register("launcher_crash",
			() -> createType(hi.block.entity.LauncherCrashBlockEntity::new,
				CreateTheAirWarsModBlocks.OZM_72.get(),
				CreateTheAirWarsModBlocks.RIM_7.get(),
				CreateTheAirWarsModBlocks.NINE_K_119M.get(),
				CreateTheAirWarsModBlocks.SC_250.get(),
				CreateTheAirWarsModBlocks.C_25A.get()
			));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<hi.block.entity.Aim9xBlockEntity>> AIM9X = REGISTRY.register("aim9x",
			() -> createType(hi.block.entity.Aim9xBlockEntity::new, CreateTheAirWarsModBlocks.AIM9X.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<hi.block.entity.RocketEngineBlockEntity>> ROCKET_ENGINE = REGISTRY.register("rocket_engine",
			() -> createType(hi.block.entity.RocketEngineBlockEntity::new, CreateTheAirWarsModBlocks.ROCKET_ENGINE.get()));

    @SubscribeEvent
    public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
            ROCKET_ENGINE.get(),
            (be, side) -> be.getFuelInputSide() == side || side == null ? be.getFluidHandler() : null
        );
    }
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<hi.block.entity.RocketDataLinkBlockEntity>> ROCKET_DATA_LINK = REGISTRY.register("rocket_data_link",
			() -> createType(hi.block.entity.RocketDataLinkBlockEntity::new, CreateTheAirWarsModBlocks.ROCKET_DATA_LINK.get()));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<hi.block.entity.AntiAircraftLauncherBlockEntity>> ANTI_AIRCRAFT_LAUNCHER = REGISTRY.register("anti_aircraft_launcher",
			() -> createType(hi.block.entity.AntiAircraftLauncherBlockEntity::new, CreateTheAirWarsModBlocks.ANTI_AIRCRAFT_LAUNCHER.get()));

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static <T extends net.minecraft.world.level.block.entity.BlockEntity> BlockEntityType<T> createType(java.util.function.BiFunction<BlockPos, BlockState, T> supplier, Block... blocks) {
		try {
			Class<?> supplierClass = Class.forName("net.minecraft.world.level.block.entity.BlockEntityType$BlockEntitySupplier");
			Object proxy = Proxy.newProxyInstance(
				CreateTheAirWarsModBlockEntities.class.getClassLoader(),
				new Class<?>[] { supplierClass },
				(instance, method, args) -> supplier.apply((BlockPos) args[0], (BlockState) args[1])
			);
			Constructor<BlockEntityType> ctor = (Constructor<BlockEntityType>) BlockEntityType.class.getConstructor(supplierClass, java.util.Set.class, com.mojang.datafixers.types.Type.class);
			return (BlockEntityType<T>) ctor.newInstance(proxy, java.util.Set.of(blocks), null);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to create BlockEntityType reflectively", e);
		}
	}
}
