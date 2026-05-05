package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents;

import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

// РЕЕСТРЫ - РЕГИСТРАЦИЯ ТРИГГЕРОВ И РЕАКЦИЙ
/* ИСПОЛЬЗОВАНИЕ
                String reactionIdFromNode = node.reactionId();
				SscResearchAndDevelopmentMod.LOGGER.debug("Reaction: " + reactionIdFromNode);
				ResourceLocation reactionLoc = reactionIdFromNode.contains(":")
						? ResourceLocation.parse(reactionIdFromNode)
						: ResourceLocation.fromNamespaceAndPath(ArtefactRegistries.MODID, reactionIdFromNode);
				ArtefactReaction reaction = ArtefactRegistries.REACTIONS.getRegistry().get().getValue(reactionLoc);
 */
public class ArtefactRegistries {
    public static final String MODID = "ssc_research_and_development";
    public static final ResourceKey<Registry<ArtefactTrigger>> TRIGGER_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MODID, "artefact_triggers"));
    public static final ResourceKey<Registry<ArtefactReaction>> REACTION_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MODID, "artefact_reactions"));

    public static final DeferredRegister<ArtefactTrigger> TRIGGERS = DeferredRegister.create(TRIGGER_REGISTRY_KEY, MODID);
    public static final DeferredRegister<ArtefactReaction> REACTIONS = DeferredRegister.create(REACTION_REGISTRY_KEY, MODID);

    static {
        TRIGGERS.makeRegistry(builder -> {});
        REACTIONS.makeRegistry(builder -> {});
    }
    // --- РЕГИСТРАЦИЯ ТРИГГЕРОВ ---
    public static final DeferredHolder<ArtefactTrigger, ArtefactTrigger> TIMER = TRIGGERS.register("timer", () -> new TimerTrigger(200));
    public static final DeferredHolder<ArtefactReaction, ArtefactReaction> NONE = REACTIONS.register("none", () -> new ArtefactReaction() {
        @Override
        public void execute(ArtefactContext context, ArtefactNode node) {}
    });
    // --- РЕГИСТРАЦИЯ РЕАКЦИЙ ---
    public static final Supplier<ArtefactReaction> EXPLOSION = REACTIONS.register("explosion", () -> new ExplosionReaction(2));
    public static final Supplier<ArtefactReaction> LIGHTNING = REACTIONS.register("lighting", LightningReaction::new);

    public static void init(IEventBus bus) {
        TRIGGERS.register(bus);
        REACTIONS.register(bus);
    }
    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---
    public static String getRandomTrigger(int depth, int tier, RandomSource random) {
        List<String> pool = TRIGGERS.getEntries().stream()
                .filter(holder -> holder.value().canSpawn(depth, tier, random))
                .map(holder -> holder.getKey().location().toString())
                .toList();

        return pool.isEmpty() ? "ssc_research_and_development:timer" : pool.get(random.nextInt(pool.size()));
    }

    public static String getRandomReaction(int depth, int tier, RandomSource random) {
        List<String> pool = REACTIONS.getEntries().stream()
                .filter(holder -> holder.value().canSpawn(depth, tier, random))
                .map(holder -> holder.getKey().location().toString())
                .toList();

        return pool.isEmpty() ? "ssc_research_and_development:none" : pool.get(random.nextInt(pool.size()));
    }
}
