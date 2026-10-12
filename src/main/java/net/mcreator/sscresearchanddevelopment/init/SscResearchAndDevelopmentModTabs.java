/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.sscresearchanddevelopment.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;

@EventBusSubscriber
public class SscResearchAndDevelopmentModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SscResearchAndDevelopmentMod.MODID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SSC_14RND = REGISTRY.register("ssc_14rnd",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.ssc_research_and_development.ssc_14rnd")).icon(() -> new ItemStack(SscResearchAndDevelopmentModBlocks.ARTEFACTCONSOLE.get())).displayItems((parameters, tabData) -> {
				tabData.accept(SscResearchAndDevelopmentModBlocks.ARTEFACTCONSOLE.get().asItem());
				tabData.accept(SscResearchAndDevelopmentModBlocks.ARTEFACTANALYZER.get().asItem());
				tabData.accept(SscResearchAndDevelopmentModItems.ARTEFACT.get());
				tabData.accept(SscResearchAndDevelopmentModBlocks.RND_SERVER.get().asItem());
				tabData.accept(SscResearchAndDevelopmentModBlocks.RESEARCH_CONSOLE.get().asItem());
				tabData.accept(SscResearchAndDevelopmentModBlocks.AUTOLATHE.get().asItem());
				tabData.accept(SscResearchAndDevelopmentModBlocks.PROTOLATHE.get().asItem());
				tabData.accept(SscResearchAndDevelopmentModBlocks.CIRCUITIMPRINTER.get().asItem());
				tabData.accept(SscResearchAndDevelopmentModItems.MULTITOOL.get());
				tabData.accept(SscResearchAndDevelopmentModItems.RESEARCHDISK.get());
			}).build());
}