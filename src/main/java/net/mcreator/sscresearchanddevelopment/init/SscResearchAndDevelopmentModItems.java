/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.sscresearchanddevelopment.init;

import net.mcreator.sscresearchanddevelopment.item.*;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;

import java.util.function.Function;

public class SscResearchAndDevelopmentModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(SscResearchAndDevelopmentMod.MODID);
	public static final DeferredItem<Item> ARTEFACTCONSOLE;
	public static final DeferredItem<Item> ARTEFACTANALYZER;
	public static final DeferredItem<Item> ARTEFACT;
	public static final DeferredItem<Item> RND_SERVER;
	public static final DeferredItem<Item> RESEARCH_CONSOLE;
	public static final DeferredItem<Item> AUTOLATHE;
	public static final DeferredItem<Item> PROTOLATHE;
	public static final DeferredItem<Item> CIRCUITIMPRINTER;
	public static final DeferredItem<Item> MULTITOOL;
	public static final DeferredItem<Item> RESEARCHDISK;
	public static final DeferredItem<Item> HAND_TELEPORTER;
	public static final DeferredItem<Item> ANOMALY_VESSEL;
	public static final DeferredItem<Item> ANOMALY_SCANNER;
	static {
		ARTEFACTCONSOLE = block(SscResearchAndDevelopmentModBlocks.ARTEFACTCONSOLE, new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1));
		ARTEFACTANALYZER = block(SscResearchAndDevelopmentModBlocks.ARTEFACTANALYZER, new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1));
		ARTEFACT = register("artefact", ArtefactItem::new);
		RND_SERVER = block(SscResearchAndDevelopmentModBlocks.RND_SERVER, new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1));
		RESEARCH_CONSOLE = block(SscResearchAndDevelopmentModBlocks.RESEARCH_CONSOLE, new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1));
		AUTOLATHE = block(SscResearchAndDevelopmentModBlocks.AUTOLATHE, new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1));
		PROTOLATHE = block(SscResearchAndDevelopmentModBlocks.PROTOLATHE, new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1));
		CIRCUITIMPRINTER = block(SscResearchAndDevelopmentModBlocks.CIRCUITIMPRINTER, new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1));
		MULTITOOL = register("multitool", MultitoolItem::new);
		RESEARCHDISK = register("researchdisk", ResearchdiskItem::new);
		HAND_TELEPORTER = register("hand_teleporter", HandTeleporterItem::new);
		ANOMALY_VESSEL = block(SscResearchAndDevelopmentModBlocks.ANOMALY_VESSEL, new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1));
		ANOMALY_SCANNER = register("anomaly_scanner", AnomalyScannerItem::new);
	}

	// Start of user code block custom items
	// End of user code block custom items
	private static <I extends Item> DeferredItem<I> register(String name, Function<Item.Properties, ? extends I> supplier) {
		return REGISTRY.registerItem(name, supplier, new Item.Properties());
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
		return block(block, new Item.Properties());
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
		return REGISTRY.registerItem(block.getId().getPath(), prop -> new BlockItem(block.get(), prop), properties);
	}
}