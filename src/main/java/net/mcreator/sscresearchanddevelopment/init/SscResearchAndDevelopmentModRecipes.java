package net.mcreator.sscresearchanddevelopment.init;

import net.mcreator.sscresearchanddevelopment.component.fabricator.FabricatorRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
public class SscResearchAndDevelopmentModRecipes {
    public static final String MOD_ID = "ssc_research_and_development";

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, FabricatorRecipe.Serializer> FABRICATOR_SERIALIZER =
            SERIALIZERS.register("fabricator", FabricatorRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<FabricatorRecipe>> FABRICATOR_TYPE =
            TYPES.register("fabricator", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return MOD_ID + ":fabricator";
                }
            });

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
