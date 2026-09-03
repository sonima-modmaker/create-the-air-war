
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package hi.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import net.minecraft.world.level.block.Block;

import hi.block.TitaniumoreBlock;
import hi.block.TestBlock;
import hi.block.TitaniumblockBlock;
import hi.block.ThtrueBlock;
import hi.block.TOMAHAWKBlock;
import hi.block.SulfurOreBlock;
import hi.block.SonarBlock;
import hi.block.SC250Block;
import hi.block.Rim7activeBlock;
import hi.block.Rim7Block;
import hi.block.OZM72Block;
import hi.block.Ninek119mactvBlock;
import hi.block.NineK119mBlock;
import hi.block.GtdfdgfBlock;
import hi.block.GdffgdgdgBlock;
import hi.block.HeattrapBlock;
import hi.block.Fab3000trueblockBlock;
import hi.block.Fab3000Block;
import hi.block.DsfsdsfBlock;
import hi.block.DeepslatetitaniumoreBlock;
import hi.block.DeepdlatesulfuroreBlock;
import hi.block.DeepStateAmalgamOreBlock;
import hi.block.C3kBlock;
import hi.block.MachinegunBlock;
import hi.block.MinigunBlock;
import hi.block.C25actvBlock;
import hi.block.C25aBlock;
import hi.block.CameraBlock;
import hi.block.AmalgamOreBlock;
import hi.block.AmalgamBucketBlock;
import hi.block.Aim9xBlock;
import hi.block.Aim9xactiveBlock;
import hi.block.MonitorBlock;
import hi.block.VihrLauncherBlock;
import hi.block.X25mlBlock;

import hi.CreateTheAirWarsMod;

public class CreateTheAirWarsModBlocks {
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks( CreateTheAirWarsMod.MODID);
	public static final DeferredBlock<Block> C_3K = REGISTRY.register("c_3k", () -> new C3kBlock());
	public static final DeferredBlock<Block> MACHINEGUN = REGISTRY.register("machinegun", () -> new MachinegunBlock());
	public static final DeferredBlock<Block> MINIGUN = REGISTRY.register("minigun", () -> new MinigunBlock());
	public static final DeferredBlock<Block> DSFSDSF = REGISTRY.register("dsfsdsf", () -> new DsfsdsfBlock());
	public static final DeferredBlock<Block> GTDFDGF = REGISTRY.register("gtdfdgf", () -> new GtdfdgfBlock());
	public static final DeferredBlock<Block> SC_250 = REGISTRY.register("sc_250", () -> new SC250Block());
	public static final DeferredBlock<Block> GDFFGDGDG = REGISTRY.register("gdffgdgdg", () -> new GdffgdgdgBlock());
	public static final DeferredBlock<Block> OZM_72 = REGISTRY.register("ozm_72", () -> new OZM72Block());
	public static final DeferredBlock<Block> DEEPSLATETITANIUMORE = REGISTRY.register("deepslatetitaniumore", () -> new DeepslatetitaniumoreBlock());
	public static final DeferredBlock<Block> RIM_7 = REGISTRY.register("rim_7", () -> new Rim7Block());
	public static final DeferredBlock<Block> RIM_7ACTIVE = REGISTRY.register("rim_7active", () -> new Rim7activeBlock());
	public static final DeferredBlock<Block> AIM9X = REGISTRY.register("aim9x", () -> new Aim9xBlock());
	public static final DeferredBlock<Block> AIM9XACTIVE = REGISTRY.register("aim9xactive", () -> new Aim9xactiveBlock());
	public static final DeferredBlock<Block> TITANIUM_ORE = REGISTRY.register("titanium_ore", () -> new TitaniumoreBlock());
	public static final DeferredBlock<Block> NINE_K_119M = REGISTRY.register("nine_k_119m", () -> new NineK119mBlock());
	public static final DeferredBlock<Block> NINEK_119MACTV = REGISTRY.register("ninek_119mactv", () -> new Ninek119mactvBlock());
	public static final DeferredBlock<Block> C_25A = REGISTRY.register("c_25a", () -> new C25aBlock());
	public static final DeferredBlock<Block> C_25ACTV = REGISTRY.register("c_25actv", () -> new C25actvBlock());
	public static final DeferredBlock<Block> TOMAHAWK = REGISTRY.register("tomahawk", () -> new TOMAHAWKBlock());
	public static final DeferredBlock<Block> THTRUE = REGISTRY.register("thtrue", () -> new ThtrueBlock());
	public static final DeferredBlock<Block> SULFUR_ORE = REGISTRY.register("sulfur_ore", () -> new SulfurOreBlock());
	public static final DeferredBlock<Block> DEEPDLATESULFURORE = REGISTRY.register("deepdlatesulfurore", () -> new DeepdlatesulfuroreBlock());
	public static final DeferredBlock<Block> TITANIUMBLOCK = REGISTRY.register("titaniumblock", () -> new TitaniumblockBlock());
	public static final DeferredBlock<Block> SONAR = REGISTRY.register("sonar", () -> new SonarBlock());
	public static final DeferredBlock<Block> TEST = REGISTRY.register("test", () -> new TestBlock());
	public static final DeferredBlock<Block> HEATTRAP = REGISTRY.register("heattrap",
		() -> new HeattrapBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.COLOR_ORANGE).strength(3.5f).requiresCorrectToolForDrops().noOcclusion()));
	public static final DeferredBlock<Block> CAMERA = REGISTRY.register("camera",
		() -> new CameraBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.COLOR_GRAY).strength(2.0f).requiresCorrectToolForDrops().noOcclusion()));
	public static final DeferredBlock<Block> MONITOR = REGISTRY.register("monitor",
		() -> new MonitorBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.COLOR_BLACK).strength(2.5f).requiresCorrectToolForDrops()));
	public static final DeferredBlock<Block> VIHR = REGISTRY.register("vihr",
		() -> new VihrLauncherBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.COLOR_GREEN).strength(3.5f).requiresCorrectToolForDrops().noOcclusion()));
	public static final DeferredBlock<Block> X25ML = REGISTRY.register("x25ml",
		() -> new X25mlBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.COLOR_GRAY).strength(3.5f).requiresCorrectToolForDrops().noOcclusion()));
	public static final DeferredBlock<Block> AMALGAM_ORE = REGISTRY.register("amalgam_ore", () -> new AmalgamOreBlock());
	public static final DeferredBlock<Block> DEEP_STATE_AMALGAM_ORE = REGISTRY.register("deep_state_amalgam_ore", () -> new DeepStateAmalgamOreBlock());
	public static final DeferredBlock<Block> AMALGAM_BUCKET = REGISTRY.register("amalgam_bucket", () -> new AmalgamBucketBlock());
	public static final DeferredBlock<Block> FAB_3000TRUEBLOCK = REGISTRY.register("fab_3000trueblock", () -> new Fab3000trueblockBlock());
	public static final DeferredBlock<Block> FAB_3000 = REGISTRY.register("fab_3000", () -> new Fab3000Block());

	public static final DeferredBlock<Block> GYRO_STABILIZER = REGISTRY.register("gyro_stabilizer",
		() -> new hi.block.GyroStabilizerBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops().noOcclusion()));
	public static final DeferredBlock<Block> ROCKET_ENGINE = REGISTRY.register("rocket_engine", 
		() -> new hi.block.RocketEngineBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.COLOR_ORANGE).strength(4.0f).requiresCorrectToolForDrops().noOcclusion()));
	public static final DeferredBlock<Block> ROCKET_DATA_LINK = REGISTRY.register("rocket_data_link", 
		() -> new hi.block.RocketDataLinkBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.COLOR_CYAN).strength(3.5f).requiresCorrectToolForDrops()));

	// Start of user code block custom blocks
	public static final DeferredBlock<Block> ANTI_AIRCRAFT_LAUNCHER = REGISTRY.register("anti_aircraft_launcher",
		() -> new hi.block.AntiAircraftLauncherBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops().noOcclusion()));
	// End of user code block custom blocks
}
