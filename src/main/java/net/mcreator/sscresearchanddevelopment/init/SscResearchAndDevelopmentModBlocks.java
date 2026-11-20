/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.sscresearchanddevelopment.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;

import net.mcreator.sscresearchanddevelopment.block.*;
import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;

import java.util.function.Function;

public class SscResearchAndDevelopmentModBlocks {
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(SscResearchAndDevelopmentMod.MODID);
	public static final DeferredBlock<Block> ARTEFACTCONSOLE;
	public static final DeferredBlock<Block> ARTEFACTANALYZER;
	public static final DeferredBlock<Block> RND_SERVER;
	public static final DeferredBlock<Block> RESEARCH_CONSOLE;
	public static final DeferredBlock<Block> AUTOLATHE;
	public static final DeferredBlock<Block> PROTOLATHE;
	public static final DeferredBlock<Block> CIRCUITIMPRINTER;
	public static final DeferredBlock<Block> ANOMALY_VESSEL;
	static {
		ARTEFACTCONSOLE = register("artefactconsole", ArtefactconsoleBlock::new);
		ARTEFACTANALYZER = register("artefactanalyzer", ArtefactanalyzerBlock::new);
		RND_SERVER = register("rnd_server", RNDServerBlock::new);
		RESEARCH_CONSOLE = register("research_console", ResearchConsoleBlock::new);
		AUTOLATHE = register("autolathe", AutolatheBlock::new);
		PROTOLATHE = register("protolathe", ProtolatheBlock::new);
		CIRCUITIMPRINTER = register("circuitimprinter", CircuitimprinterBlock::new);
		ANOMALY_VESSEL = register("anomaly_vessel", AnomalyVesselBlock::new);
	}

	// Start of user code block custom blocks
	// End of user code block custom blocks
	private static <B extends Block> DeferredBlock<B> register(String name, Function<BlockBehaviour.Properties, ? extends B> supplier) {
		return REGISTRY.registerBlock(name, supplier);
	}
}