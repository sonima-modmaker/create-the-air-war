
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package hi.init;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import hi.item.WtItem;
import hi.item.WiresItem;
import hi.item.TitaniumshovelItem;
import hi.item.TitaniumsheetItem;
import hi.item.TitaniumpickaxeItem;
import hi.item.TitaniumnuggetItem;
import hi.item.TitaniumingotItem;
import hi.item.TitaniumHoeItem;
import hi.item.TitaniumAxeItem;
import hi.item.TargetGunnergadgetItem;
import hi.item.RoughAmalgamItem;
import hi.item.RocketengineItem;
import hi.item.RawtitaniumItem;
import hi.item.RawSulfurItem;
import hi.item.PieceofMirrorItem;
import hi.item.MiniexplzvItem;
import hi.item.MIRRORItem;
import hi.item.MinigunBlockItem;
import hi.item.M24Item;
import hi.item.ExplosiveItem;
import hi.item.DisplayItem;
import hi.item.DroneControllerItem;
import hi.item.HeattrapChargeItem;
import hi.item.FpvDroneItem;
import hi.item.CrushedAmalgamItem;
import hi.item.CrashedtitaniumItem;
import hi.item.CrashedsulfurItem;
import hi.item.ChipItem;
import hi.item.ChickItem;
import hi.item.BigexplzvItem;
import hi.item.AntennaItem;
import hi.item.AmpilifierItem;
import hi.item.AmalgamBucketItem;
import hi.item.ActivatorItem;
import hi.item.CameraLinkItem;
import hi.item.C75RocketItem;
import hi.item.VihrRocketItem;
import hi.item.X25mlBlockItem;

import hi.CreateTheAirWarsMod;

public class CreateTheAirWarsModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems( CreateTheAirWarsMod.MODID);
	public static final DeferredItem<Item> C_3K = block(CreateTheAirWarsModBlocks.C_3K);
	public static final DeferredItem<Item> MACHINEGUN = block(CreateTheAirWarsModBlocks.MACHINEGUN);
	public static final DeferredItem<Item> MINIGUN = REGISTRY.register("minigun", () -> new MinigunBlockItem(CreateTheAirWarsModBlocks.MINIGUN.get(), new Item.Properties()));
	public static final DeferredItem<Item> SHELL = REGISTRY.register("shell", () -> new Item(new Item.Properties()));
	public static final DeferredItem<Item> DSFSDSF = block(CreateTheAirWarsModBlocks.DSFSDSF);
	public static final DeferredItem<Item> GTDFDGF = block(CreateTheAirWarsModBlocks.GTDFDGF);
	public static final DeferredItem<Item> SC_250 = block(CreateTheAirWarsModBlocks.SC_250);
	public static final DeferredItem<Item> CHIP = REGISTRY.register("chip", () -> new ChipItem());
	public static final DeferredItem<Item> TARGET_GUNNERGADGET = REGISTRY.register("target_gunnergadget", () -> new TargetGunnergadgetItem());
	public static final DeferredItem<Item> GDFFGDGDG = block(CreateTheAirWarsModBlocks.GDFFGDGDG);
	public static final DeferredItem<Item> OZM_72 = block(CreateTheAirWarsModBlocks.OZM_72);
	public static final DeferredItem<Item> RAWTITANIUM = REGISTRY.register("rawtitanium", () -> new RawtitaniumItem());
	public static final DeferredItem<Item> TITANIUMINGOT = REGISTRY.register("titaniumingot", () -> new TitaniumingotItem());
	public static final DeferredItem<Item> DEEPSLATETITANIUMORE = block(CreateTheAirWarsModBlocks.DEEPSLATETITANIUMORE);
	public static final DeferredItem<Item> ACTIVATOR = REGISTRY.register("activator", () -> new ActivatorItem());
	public static final DeferredItem<Item> EXPLOSIVE = REGISTRY.register("explosive", () -> new ExplosiveItem());
	public static final DeferredItem<Item> ROCKETENGINE = REGISTRY.register("rocketengine", () -> new RocketengineItem());
	public static final DeferredItem<Item> RIM_7 = block(CreateTheAirWarsModBlocks.RIM_7);
	public static final DeferredItem<Item> RIM_7ACTIVE = block(CreateTheAirWarsModBlocks.RIM_7ACTIVE);
	public static final DeferredItem<Item> AIM9X = block(CreateTheAirWarsModBlocks.AIM9X);
	public static final DeferredItem<Item> AIM9XACTIVE = block(CreateTheAirWarsModBlocks.AIM9XACTIVE);
	public static final DeferredItem<Item> TITANIUM_ORE = block(CreateTheAirWarsModBlocks.TITANIUM_ORE);
	public static final DeferredItem<Item> NINE_K_119M = block(CreateTheAirWarsModBlocks.NINE_K_119M);
	public static final DeferredItem<Item> NINEK_119MACTV = block(CreateTheAirWarsModBlocks.NINEK_119MACTV);
	public static final DeferredItem<Item> C_25A = block(CreateTheAirWarsModBlocks.C_25A);
	public static final DeferredItem<Item> C_25ACTV = block(CreateTheAirWarsModBlocks.C_25ACTV);
	public static final DeferredItem<Item> M_24 = REGISTRY.register("m_24", () -> new M24Item());
	public static final DeferredItem<Item> MINIEXPLZV = REGISTRY.register("miniexplzv", () -> new MiniexplzvItem());
	public static final DeferredItem<Item> BIGEXPLZV = REGISTRY.register("bigexplzv", () -> new BigexplzvItem());
	public static final DeferredItem<Item> WT = REGISTRY.register("wt", () -> new WtItem());
	public static final DeferredItem<Item> CRASHEDTITANIUM = REGISTRY.register("crashedtitanium", () -> new CrashedtitaniumItem());
	public static final DeferredItem<Item> TITANIUMSHEET = REGISTRY.register("titaniumsheet", () -> new TitaniumsheetItem());
	public static final DeferredItem<Item> TITANIUMNUGGET = REGISTRY.register("titaniumnugget", () -> new TitaniumnuggetItem());
	public static final DeferredItem<Item> ANTENNA = REGISTRY.register("antenna", () -> new AntennaItem());
	public static final DeferredItem<Item> CHICK = REGISTRY.register("chick", () -> new ChickItem());
	public static final DeferredItem<Item> TITANIUM_AXE = REGISTRY.register("titanium_axe", () -> new TitaniumAxeItem());
	public static final DeferredItem<Item> TITANIUMPICKAXE = REGISTRY.register("titaniumpickaxe", () -> new TitaniumpickaxeItem());
	public static final DeferredItem<Item> TITANIUMSHOVEL = REGISTRY.register("titaniumshovel", () -> new TitaniumshovelItem());
	public static final DeferredItem<Item> TITANIUM_HOE = REGISTRY.register("titanium_hoe", () -> new TitaniumHoeItem());
	public static final DeferredItem<Item> TOMAHAWK = block(CreateTheAirWarsModBlocks.TOMAHAWK);
	public static final DeferredItem<Item> THTRUE = block(CreateTheAirWarsModBlocks.THTRUE);
	public static final DeferredItem<Item> SULFUR_ORE = block(CreateTheAirWarsModBlocks.SULFUR_ORE);
	public static final DeferredItem<Item> RAW_SULFUR = REGISTRY.register("raw_sulfur", () -> new RawSulfurItem());
	public static final DeferredItem<Item> DEEPDLATESULFURORE = block(CreateTheAirWarsModBlocks.DEEPDLATESULFURORE);
	public static final DeferredItem<Item> CRASHEDSULFUR = REGISTRY.register("crashedsulfur", () -> new CrashedsulfurItem());
	public static final DeferredItem<Item> TITANIUMBLOCK = block(CreateTheAirWarsModBlocks.TITANIUMBLOCK);
	public static final DeferredItem<Item> SONAR = block(CreateTheAirWarsModBlocks.SONAR);
	public static final DeferredItem<Item> TEST = block(CreateTheAirWarsModBlocks.TEST);
	public static final DeferredItem<Item> HEATTRAP = block(CreateTheAirWarsModBlocks.HEATTRAP);
	public static final DeferredItem<Item> CAMERA = block(CreateTheAirWarsModBlocks.CAMERA);
	public static final DeferredItem<Item> MONITOR = block(CreateTheAirWarsModBlocks.MONITOR);
	public static final DeferredItem<Item> VIHR = block(CreateTheAirWarsModBlocks.VIHR);
	public static final DeferredItem<Item> X25ML = block(CreateTheAirWarsModBlocks.X25ML);
	public static final DeferredItem<Item> CAMERA_LINK = REGISTRY.register("camera_link", () -> new CameraLinkItem(new Item.Properties()));
	public static final DeferredItem<Item> VIHR_ROCKET = REGISTRY.register("vihr_rocket", () -> new VihrRocketItem(new Item.Properties()));
	public static final DeferredItem<Item> HEATTRAP_CHARGE = REGISTRY.register("heattrap_charge", () -> new HeattrapChargeItem());
	public static final DeferredItem<Item> ROUGH_AMALGAM = REGISTRY.register("rough_amalgam", () -> new RoughAmalgamItem());
	public static final DeferredItem<Item> AMALGAM_ORE = block(CreateTheAirWarsModBlocks.AMALGAM_ORE);
	public static final DeferredItem<Item> DEEP_STATE_AMALGAM_ORE = block(CreateTheAirWarsModBlocks.DEEP_STATE_AMALGAM_ORE);
	public static final DeferredItem<Item> CRUSHED_AMALGAM = REGISTRY.register("crushed_amalgam", () -> new CrushedAmalgamItem());
	public static final DeferredItem<Item> AMALGAM_BUCKET_BUCKET = REGISTRY.register("amalgam_bucket_bucket", () -> new AmalgamBucketItem());
	public static final DeferredItem<Item> MIRROR = REGISTRY.register("mirror", () -> new MIRRORItem());
	public static final DeferredItem<Item> PIECEOF_MIRROR = REGISTRY.register("pieceof_mirror", () -> new PieceofMirrorItem());
	public static final DeferredItem<Item> WIRES = REGISTRY.register("wires", () -> new WiresItem());
	public static final DeferredItem<Item> DISPLAY = REGISTRY.register("display", () -> new DisplayItem());
	public static final DeferredItem<Item> AMPILIFIER = REGISTRY.register("ampilifier", () -> new AmpilifierItem());
	public static final DeferredItem<Item> FAB_3000TRUEBLOCK = block(CreateTheAirWarsModBlocks.FAB_3000TRUEBLOCK);
	public static final DeferredItem<Item> FAB_3000 = block(CreateTheAirWarsModBlocks.FAB_3000);
	public static final DeferredItem<Item> SPACER = REGISTRY.register("spacer", () -> new Item(new Item.Properties()) {
		@Override
		public boolean canFitInsideContainerItems() {
			return false; // Prevent putting in bundles/shulker boxes
		}
	});

	public static final DeferredItem<Item> ROCKET_DATA_LINK_ITEM = REGISTRY.register("rocket_data_link", () -> new hi.item.RocketDataLinkItem(CreateTheAirWarsModBlocks.ROCKET_DATA_LINK.get(), new Item.Properties()));
	public static final DeferredItem<Item> GYRO_STABILIZER = block(CreateTheAirWarsModBlocks.GYRO_STABILIZER);
	public static final DeferredItem<Item> ROCKET_ENGINE = block(CreateTheAirWarsModBlocks.ROCKET_ENGINE);

	public static final DeferredItem<Item> C75_ROCKET = REGISTRY.register("c75", () -> new C75RocketItem(new Item.Properties()));
	public static final DeferredItem<Item> DRONE_CONTROLLER = REGISTRY.register("drone_controller", () -> new DroneControllerItem(new Item.Properties()));
	public static final DeferredItem<Item> FPV_DRONE = REGISTRY.register("fpv_drone", () -> new FpvDroneItem(new Item.Properties()));
	public static final DeferredItem<Item> ANTI_AIRCRAFT_LAUNCHER = block(CreateTheAirWarsModBlocks.ANTI_AIRCRAFT_LAUNCHER);
	// End of user code block custom items
	private static DeferredItem<Item> block(DeferredBlock<Block> block) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
	}
}
