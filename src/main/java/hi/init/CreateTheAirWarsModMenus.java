
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package hi.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.world.inventory.MenuType;

import hi.world.inventory.UfyMenu;
import hi.world.inventory.MgMenu;
import hi.world.inventory.FdgddMenu;
import hi.world.inventory.DfgdfgMenu;

import hi.CreateTheAirWarsMod;

public class CreateTheAirWarsModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, CreateTheAirWarsMod.MODID);
	public static final java.util.function.Supplier<net.minecraft.world.inventory.MenuType<FdgddMenu>> FDGDD = REGISTRY.register("fdgdd", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(FdgddMenu::new));
	public static final java.util.function.Supplier<net.minecraft.world.inventory.MenuType<DfgdfgMenu>> DFGDFG = REGISTRY.register("dfgdfg", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(DfgdfgMenu::new));
	public static final java.util.function.Supplier<net.minecraft.world.inventory.MenuType<UfyMenu>> UFY = REGISTRY.register("ufy", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(UfyMenu::new));
	public static final java.util.function.Supplier<net.minecraft.world.inventory.MenuType<MgMenu>> MG = REGISTRY.register("mg", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(MgMenu::new));
	public static final java.util.function.Supplier<net.minecraft.world.inventory.MenuType<hi.world.inventory.AntiAircraftLauncherMenu>> ANTI_AIRCRAFT_LAUNCHER = REGISTRY.register("anti_aircraft_launcher", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(hi.world.inventory.AntiAircraftLauncherMenu::new));
}
