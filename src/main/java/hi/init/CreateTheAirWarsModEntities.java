
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package hi.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;

import hi.entity.TomahawkbultEntity;
import hi.entity.Rim7actvbultEntity;
import hi.entity.Ninek119mactvbultEntity;
import hi.entity.M24bultEntity;
import hi.entity.MachinegunShellEntity;
import hi.entity.GvrdcrcdEntity;
import hi.entity.HeattrapFlareEntity;
import hi.entity.Fab3000trueEntity;
import hi.entity.C3ktrueEntity;
import hi.entity.C25actvbultEntity;
import hi.entity.C25Entity;
import hi.entity.BgghEntity;
import hi.entity.Aim9xbultEntity;
import hi.entity.AstrkEntity;
import hi.entity.VihrRocketEntity;
import hi.entity.X25mlEntity;
import hi.entity.C75RocketEntity;
import hi.entity.FpvDroneEntity;

import hi.CreateTheAirWarsMod;

@net.neoforged.fml.common.EventBusSubscriber(bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
public class CreateTheAirWarsModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, CreateTheAirWarsMod.MODID);
	public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<C3ktrueEntity>> C_3KTRUE = register("c_3ktrue",
			EntityType.Builder.<C3ktrueEntity>of(C3ktrueEntity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<MachinegunShellEntity>> MACHINEGUN_SHELL = register("machinegun_shell",
			EntityType.Builder.<MachinegunShellEntity>of(MachinegunShellEntity::new, MobCategory.MISC).clientTrackingRange(64).updateInterval(1).sized(0.25f, 0.25f));
	public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<GvrdcrcdEntity>> GVRDCRCD = register("gvrdcrcd",
			EntityType.Builder.<GvrdcrcdEntity>of(GvrdcrcdEntity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<C25Entity>> C_25 = register("c_25",
			EntityType.Builder.<C25Entity>of(C25Entity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<Rim7actvbultEntity>> RIM_7ACTVBULT = register("rim_7actvbult",
			EntityType.Builder.<Rim7actvbultEntity>of(Rim7actvbultEntity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<Aim9xbultEntity>> AIM9XBULT = register("aim9xbult",
			EntityType.Builder.<Aim9xbultEntity>of(Aim9xbultEntity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<Ninek119mactvbultEntity>> NINEK_119MACTVBULT = register("ninek_119mactvbult", EntityType.Builder.<Ninek119mactvbultEntity>of(Ninek119mactvbultEntity::new, MobCategory.MISC)
			.clientTrackingRange(256).updateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<C25actvbultEntity>> C_25ACTVBULT = register("c_25actvbult",
			EntityType.Builder.<C25actvbultEntity>of(C25actvbultEntity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<M24bultEntity>> M_24BULT = register("m_24bult",
			EntityType.Builder.<M24bultEntity>of(M24bultEntity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<TomahawkbultEntity>> TOMAHAWKBULT = register("tomahawkbult",
			EntityType.Builder.<TomahawkbultEntity>of(TomahawkbultEntity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<BgghEntity>> BGGH = register("bggh",
			EntityType.Builder.<BgghEntity>of(BgghEntity::new, MobCategory.MISC).clientTrackingRange(64).updateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<Fab3000trueEntity>> FAB_3000TRUE = register("fab_3000true",
			EntityType.Builder.<Fab3000trueEntity>of(Fab3000trueEntity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<AstrkEntity>> ASTRK = register("astrk",
			EntityType.Builder.<AstrkEntity>of(AstrkEntity::new, MobCategory.MISC).clientTrackingRange(64).updateInterval(1).sized(0.5f, 0.5f));
	public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<HeattrapFlareEntity>> HEATTRAP_FLARE = register("heattrap_flare",
			EntityType.Builder.<HeattrapFlareEntity>of(HeattrapFlareEntity::new, MobCategory.MISC).clientTrackingRange(128).updateInterval(1).sized(0.45f, 0.45f));
	public static final DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<VihrRocketEntity>> VIHR_ROCKET = register("vihr_rocket",
			EntityType.Builder.<VihrRocketEntity>of(VihrRocketEntity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(1).sized(0.4f, 0.4f));
	public static final DeferredHolder<EntityType<?>, EntityType<X25mlEntity>> X25ML_MISSILE = register("x25ml_missile",
			EntityType.Builder.<X25mlEntity>of(X25mlEntity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(1).sized(0.45f, 0.45f));
	public static final DeferredHolder<EntityType<?>, EntityType<C75RocketEntity>> C75_ROCKET = register("c75_rocket",
			EntityType.Builder.<C75RocketEntity>of(C75RocketEntity::new, MobCategory.MISC).clientTrackingRange(256).updateInterval(1).sized(0.45f, 0.45f));
	public static final DeferredHolder<EntityType<?>, EntityType<FpvDroneEntity>> FPV_DRONE = register("fpv_drone",
			EntityType.Builder.<FpvDroneEntity>of(FpvDroneEntity::new, MobCategory.MISC).clientTrackingRange(192).updateInterval(1).sized(0.8f, 0.35f));
	public static final DeferredHolder<EntityType<?>, EntityType<hi.entity.UxoEntity>> UXO_ENTITY = register("uxo_entity",
			EntityType.Builder.<hi.entity.UxoEntity>of(hi.entity.UxoEntity::new, MobCategory.MISC).clientTrackingRange(128).updateInterval(1).sized(0.6f, 0.6f));

	private static <T extends Entity> DeferredHolder<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	public static void init(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
		});
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
	}
}
