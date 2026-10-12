package net.mcreator.sscresearchanddevelopment.init;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactReaction;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactTrigger;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.reaction.*;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.trigger.*;
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
    public static final DeferredHolder<ArtefactTrigger, ArtefactTrigger> TIMER =
            TRIGGERS.register("timer", () -> new TimerTrigger(200));
    public static final DeferredHolder<ArtefactReaction, ArtefactReaction> NONE =
            REACTIONS.register("none", () -> new ArtefactReaction() {
        @Override
        public void execute(ArtefactContext context, ArtefactNode node) {}
    });
    public static final DeferredHolder<ArtefactTrigger,ArtefactTrigger> ATTACK =
            TRIGGERS.register("attack", AttackTrigger::new);
    public static final DeferredHolder<ArtefactTrigger,ArtefactTrigger> IGNITION =
            TRIGGERS.register("ignition", IgnitionTrigger::new);
    public static final DeferredHolder<ArtefactTrigger, ArtefactTrigger> IMPACT_OR_DROP =
            TRIGGERS.register("impact_or_drop", ImpactOrDropTrigger::new);
    public static final DeferredHolder<ArtefactTrigger, ArtefactTrigger> NEARBY_DEATH =
            TRIGGERS.register("nearby_death", NearbyDeathTrigger::new);
    public static final DeferredHolder<ArtefactTrigger, ArtefactTrigger> LIGHT_LEVEL =
            TRIGGERS.register("light_level", LightLevelTrigger::new);
    public static final DeferredHolder<ArtefactTrigger, ArtefactTrigger> SUBMERGED =
            TRIGGERS.register("submerged", SubmergedTrigger::new);
    public static final DeferredHolder<ArtefactTrigger, ArtefactTrigger> PULSATION =
            TRIGGERS.register("pulsation", () -> new ToolUseTrigger(List.of(
                    "ssc_research_and_development:multitool",
                    "ssc_14:multitul"
            )));
    public static final DeferredHolder<ArtefactTrigger, ArtefactTrigger> PRYING =
            TRIGGERS.register("prying", () -> new ToolUseTrigger(List.of(
                    "ssc_14:crowbar",
                    "ssc_14:fire_axe"
            )));
    public static final DeferredHolder<ArtefactTrigger, ArtefactTrigger> TIGHTENING =
            TRIGGERS.register("tightening", () -> new ToolUseTrigger(List.of(
                    "ssc_14:spanner"
            )));
    public static final DeferredHolder<ArtefactTrigger, ArtefactTrigger> SCREWING =
            TRIGGERS.register("screwing", () -> new ToolUseTrigger(List.of(
                    "ssc_14:screwdriver"
            )));
    public static final DeferredHolder<ArtefactTrigger, ArtefactTrigger> CUTTING =
            TRIGGERS.register("cutting", () -> new ToolUseTrigger(List.of(
                    "ssc_14:nippers"
            )));
    public static final Supplier<ArtefactReaction> EXPLOSION =
            REACTIONS.register("explosion", () -> new ExplosionReaction(2));
    public static final Supplier<ArtefactReaction> LIGHTING =
            REACTIONS.register("lighting", LightingReaction::new);
    public static final DeferredHolder<ArtefactReaction, ArtefactReaction> JUNK_RESOURCE_SPAWN =
            REACTIONS.register("junk_resource_spawn", () -> new ResourceSpawnReaction(
                    List.of(
                            "ssc_14:eggshell",
                            "ssc_14:rotten_meat",
                            "ssc_14:glass_shard",
                            "ssc_14:arm_glass_shard",
                            "ssc_14:led_light_tube_broken",
                            "ssc_14:light_bulb_broken",
                            "ssc_14:boritos_trash",
                            "ssc_14:popcorn_trash",
                            "energy_bar_trash"
                    ),
                    10, 30
            ));
    public static final DeferredHolder<ArtefactReaction, ArtefactReaction> COMMON_RESOURCE_SPAWN =
            REACTIONS.register("common_resource_spawn", () -> new ResourceSpawnReaction(
                    List.of(
                            "ssc_14:steel",
                            "ssc_14:glass",
                            "ssc_14:raw_plasma",
                            "ssc_14:raw_silver",
                            "ssc_14:raw_diamond",
                            "ssc_14:raw_quartz",
                            "ssc_14:raw_gold",
                            "ssc_14:raw_uran",
                            "ssc_14:raw_iron",
                            "ssc_14:raw_coal"
                    ),
                    5, 15
            ));
    public static final DeferredHolder<ArtefactReaction, ArtefactReaction> RARE_RESOURCE_SPAWN =
            REACTIONS.register("rare_resource_spawn", () -> new ResourceSpawnReaction(
                    List.of(
                            "ssc_14:plasteel",
                            "ssc_14:arm_glass",
                            "ssc_14:plastic",
                            "ssc_14:plasma",
                            "ssc_14:ssc_14_gold",
                            "ssc_14:silver",
                            "ssc_14:uran"
                    ),
                    1, 5
            ));
    public static final DeferredHolder<ArtefactReaction, ArtefactReaction> ARTEFACT_SPAWN =
            REACTIONS.register("artefact_spawn", () -> new ResourceSpawnReaction(
                    List.of(
                            "ssc_research_and_development:artefact"
                    ),
                    1, 1
            ));
    public static final DeferredHolder<ArtefactReaction, ArtefactReaction> GROW_TREE =
            REACTIONS.register("grow_tree", GrowTreeReaction::new);
    // 1. МИРНЫЕ СУЩНОСТИ (Свиньи, коровы, овцы, пчелы, жители)
    public static final DeferredHolder<ArtefactReaction, ArtefactReaction> SUMMON_PEACEFUL =
            REACTIONS.register("summon_peaceful", () -> new EntitySummonReaction(
                    List.of(
                            "minecraft:cow",
                            "minecraft:sheep",
                            "minecraft:pig",
                            "minecraft:bee",
                            "minecraft:chicken",
                            "minecraft:villager"
                    ),
                    1, 3
            ));

    // 2. ВРАЖДЕБНЫЕ СУЩНОСТИ (Зомби, скелеты, ифриты, пауки, чешуйницы)
    public static final DeferredHolder<ArtefactReaction, ArtefactReaction> SUMMON_HOSTILE =
            REACTIONS.register("summon_hostile", () -> new EntitySummonReaction(
                    List.of(
                            "minecraft:zombie",
                            "minecraft:skeleton",
                            "minecraft:spider",
                            "minecraft:blaze",
                            "minecraft:cave_spider",
                            "minecraft:silverfish"
                    ),
                    2, 4
            ));
    public static final DeferredHolder<ArtefactReaction, ArtefactReaction> AREA_DAMAGE_LOW =
            REACTIONS.register("area_damage_low", () -> new AreaDamageReaction(4.0F, 5.0D));

    // 2. Сильная аномальная вспышка урона (12 HP = 6 сердец в радиусе 8 блоков)
    public static final DeferredHolder<ArtefactReaction, ArtefactReaction> AREA_DAMAGE_HIGH =
            REACTIONS.register("area_damage_high", () -> new AreaDamageReaction(12.0F, 8.0D));

    // 3. Импульс исцеления (6 HP = 3 сердца в радиусе 6 блоков)
    public static final DeferredHolder<ArtefactReaction, ArtefactReaction> AREA_HEAL =
            REACTIONS.register("area_heal", () -> new AreaHealReaction(6.0F, 6.0D));
    public static final DeferredHolder<ArtefactReaction, ArtefactReaction> IMPULSE_PUSH =
            REACTIONS.register("impulse_push", () -> new ImpulsePushReaction(8.0D, 2.0D));
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
