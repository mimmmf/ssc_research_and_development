package net.mcreator.sscresearchanddevelopment.component.fabricator;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FabricatorManager extends SimpleJsonResourceReloadListener<JsonElement> {
    private final Map<ResourceLocation, FabricatorRecipe> recipes = new HashMap<>();
    private final HolderLookup.Provider registries;

    public FabricatorManager(HolderLookup.Provider registries) {
        super(
                Codec.PASSTHROUGH.xmap(
                        dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
                        json -> new Dynamic<>(JsonOps.INSTANCE, json)
                ),
                FileToIdConverter.json("recipes/fabricator")
        );
        this.registries = registries;
    }

    public Map<ResourceLocation, FabricatorRecipe> getRecipes() {
        return this.recipes;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object,
                         ResourceManager resourceManager,
                         ProfilerFiller profilerFiller) {
        recipes.clear();

        var ops = registries.createSerializationContext(JsonOps.INSTANCE);

        object.forEach((location, element) -> {
            try {
                if (element.isJsonArray()) {
                    List<FabricatorRecipe> parsedRecipes = FabricatorRecipe.Serializer.LIST_CODEC
                            .parse(ops, element)
                            .getOrThrow();

                    for (int i = 0; i < parsedRecipes.size(); i++) {
                        FabricatorRecipe recipe = parsedRecipes.get(i);
                        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(
                                location.getNamespace(),
                                location.getPath() + "_" + i
                        );
                        recipes.put(recipeId, recipe);
                    }
                } else if (element.isJsonObject()) {
                    FabricatorRecipe recipe = FabricatorRecipe.Serializer.CODEC
                            .codec()
                            .parse(ops, element)
                            .getOrThrow();

                    recipes.put(location, recipe);
                }
            } catch (Exception e) {
                System.err.println("Failed to load fabricator recipes from " + location + ": " + e.getMessage());
            }
        });

        System.out.println("Loaded fabricator recipes: " + recipes.size());
    }

    public static String getStorageKeyFromItem(ItemStack stack) {
        if (stack.isEmpty()) return "";

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.toString();
    }

    public static ItemStack getItemFromStorageKey(String key) {
        if (key == null || key.isEmpty()) return ItemStack.EMPTY;

        try {
            ResourceLocation id = ResourceLocation.parse(key);

            Item item = BuiltInRegistries.ITEM.get(id).get().value();

            if (item == Items.AIR) {
                return ItemStack.EMPTY;
            }

            return new ItemStack(item);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }
}