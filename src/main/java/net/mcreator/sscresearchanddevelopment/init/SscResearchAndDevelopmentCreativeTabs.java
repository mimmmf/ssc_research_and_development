package net.mcreator.sscresearchanddevelopment.init;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = "ssc_research_and_development")
public class SscResearchAndDevelopmentCreativeTabs {
    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == SscResearchAndDevelopmentModTabs.SSC_14RND.getKey()) {

            ItemStack disk1k = new ItemStack(SscResearchAndDevelopmentModItems.RESEARCHDISK.get());
            disk1k.set(SscResearchAndDevelopmentDataComponents.SCORE.get(), 1000);
            event.accept(disk1k);

            ItemStack disk5k = new ItemStack(SscResearchAndDevelopmentModItems.RESEARCHDISK.get());
            disk5k.set(SscResearchAndDevelopmentDataComponents.SCORE.get(), 5000);
            event.accept(disk5k);

            ItemStack disk10k = new ItemStack(SscResearchAndDevelopmentModItems.RESEARCHDISK.get());
            disk10k.set(SscResearchAndDevelopmentDataComponents.SCORE.get(), 10000);
            event.accept(disk10k);

            ItemStack diskMax = new ItemStack(SscResearchAndDevelopmentModItems.RESEARCHDISK.get());
            diskMax.set(SscResearchAndDevelopmentDataComponents.SCORE.get(), Integer.MAX_VALUE);
            event.accept(diskMax);
        }
    }
}
