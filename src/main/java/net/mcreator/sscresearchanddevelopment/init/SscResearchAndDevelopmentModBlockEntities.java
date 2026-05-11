/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.sscresearchanddevelopment.init;

import net.mcreator.sscresearchanddevelopment.block.AutolatheBlock;
import net.mcreator.sscresearchanddevelopment.block.entity.AutolatheBlockEntity;
import net.mcreator.sscresearchanddevelopment.block.entity.RNDServerBlockEntity;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactconsoleBlockEntity;
import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactanalyzerBlockEntity;
import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;

@EventBusSubscriber
public class SscResearchAndDevelopmentModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, SscResearchAndDevelopmentMod.MODID);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArtefactconsoleBlockEntity>> ARTEFACTCONSOLE = register("artefactconsole", SscResearchAndDevelopmentModBlocks.ARTEFACTCONSOLE, ArtefactconsoleBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArtefactanalyzerBlockEntity>> ARTEFACTANALYZER = register("artefactanalyzer", SscResearchAndDevelopmentModBlocks.ARTEFACTANALYZER, ArtefactanalyzerBlockEntity::new);

	// Start of user code block custom block entities
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RNDServerBlockEntity>> RNDSERVER = register("rndserver", SscResearchAndDevelopmentModBlocks.RND_SERVER, RNDServerBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutolatheBlockEntity>> AUTOLATHE = register("autolathe", SscResearchAndDevelopmentModBlocks.AUTOLATHE, AutolatheBlockEntity::new);
	// End of user code block custom block entities
	private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String registryname, DeferredHolder<Block, Block> block, BlockEntityType.BlockEntitySupplier<T> supplier) {
		return REGISTRY.register(registryname, () -> new BlockEntityType(supplier, block.get()));
	}

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ARTEFACTANALYZER.get(), SidedInvWrapper::new);
	}
}