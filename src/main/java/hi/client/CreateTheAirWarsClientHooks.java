package hi.client;

import hi.CreateTheAirWarsMod;
import hi.client.gui.DfgdfgScreen;
import hi.client.gui.FdgddScreen;
import hi.client.gui.MgScreen;
import hi.client.gui.UfyScreen;
import hi.client.camera.CameraMonitorClientHandler;
import hi.client.DroneControllerClientHandler;
import hi.client.renderer.GyroStabilizerRenderer;
import hi.client.renderer.CameraBlockEntityRenderer;
import hi.client.renderer.MonitorBlockEntityRenderer;
import hi.client.renderer.MinigunBlockEntityRenderer;
import hi.client.renderer.RocketEngineThermalRenderer;
import hi.client.renderer.VihrLauncherRenderer;
import hi.client.renderer.X25mlBlockEntityRenderer;
import hi.init.CreateTheAirWarsModBlockEntities;
import hi.init.CreateTheAirWarsModMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@net.neoforged.fml.common.EventBusSubscriber(modid = CreateTheAirWarsMod.MODID, bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CreateTheAirWarsClientHooks {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CameraMonitorClientHandler.registerGameEvents();
            DroneControllerClientHandler.registerGameEvents();
            registerMenuScreen(CreateTheAirWarsModMenus.UFY.get(), (menu, inventory, title) -> new UfyScreen((hi.world.inventory.UfyMenu) menu, inventory, title));
            registerMenuScreen(CreateTheAirWarsModMenus.MG.get(), (menu, inventory, title) -> new MgScreen((hi.world.inventory.MgMenu) menu, inventory, title));
            registerMenuScreen(CreateTheAirWarsModMenus.FDGDD.get(), (menu, inventory, title) -> new FdgddScreen((hi.world.inventory.FdgddMenu) menu, inventory, title));
            registerMenuScreen(CreateTheAirWarsModMenus.DFGDFG.get(), (menu, inventory, title) -> new DfgdfgScreen((hi.world.inventory.DfgdfgMenu) menu, inventory, title));
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CreateTheAirWarsModBlockEntities.GYRO_STABILIZER.get(), GyroStabilizerRenderer::new);
        event.registerBlockEntityRenderer(CreateTheAirWarsModBlockEntities.MONITOR.get(), MonitorBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(CreateTheAirWarsModBlockEntities.CAMERA.get(), CameraBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(CreateTheAirWarsModBlockEntities.VIHR.get(), VihrLauncherRenderer::new);
        event.registerBlockEntityRenderer(CreateTheAirWarsModBlockEntities.X25ML.get(), X25mlBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(CreateTheAirWarsModBlockEntities.ROCKET_ENGINE.get(), RocketEngineThermalRenderer::new);
        event.registerBlockEntityRenderer(CreateTheAirWarsModBlockEntities.MINIGUN.get(), MinigunBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/camera_platform"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/camera_base"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/camera_head"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/vihr_base"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/vihr_rocket_1"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/vihr_rocket_2"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/vihr_rocket_3"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/vihr_rocket_4"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/vihr_rocket_5"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/vihr_rocket_6"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/vihr_panel_1"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/vihr_panel_2"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/vihr_panel_3"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/vihr_panel_4"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/vihr_panel_5"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/vihr_panel_6"), "standalone"));
        event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(CreateTheAirWarsMod.MODID, "block/minigun_barrels"), "standalone"));
    }
    private static void registerMenuScreen(
        MenuType<?> menuType,
        ScreenFactory factory
    ) {
        try {
            Class<?> screenConstructorClass = Class.forName("net.minecraft.client.gui.screens.MenuScreens$ScreenConstructor");
            Method registerMethod = MenuScreens.class.getDeclaredMethod("register", MenuType.class, screenConstructorClass);
            registerMethod.setAccessible(true);

            InvocationHandler handler = (proxy, method, args) -> {
                if ("create".equals(method.getName()) && args != null && args.length == 3) {
                    return factory.create((AbstractContainerMenu) args[0], (Inventory) args[1], (Component) args[2]);
                }
                return null;
            };

            Object constructorProxy = Proxy.newProxyInstance(
                Minecraft.class.getClassLoader(),
                new Class<?>[]{screenConstructorClass},
                handler
            );
            registerMethod.invoke(null, menuType, constructorProxy);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to register client screen for menu " + menuType, e);
        }
    }

    @FunctionalInterface
    private interface ScreenFactory {
        Screen create(AbstractContainerMenu menu, Inventory inventory, Component title);
    }
}
