package net.mcreator.sscresearchanddevelopment.component.fabricator;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
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
                FabricatorRecipe recipe = FabricatorRecipe.Serializer.CODEC
                        .codec()
                        .parse(ops, element)
                        .getOrThrow();

                recipes.put(location, recipe);
            } catch (Exception e) {
                System.err.println("Failed to load recipe " + location + ": " + e.getMessage());
            }
        });

        System.out.println("Loaded fabricator recipes: " + recipes.size());
    }

    public static String getStorageKeyFromItem(ItemStack stack) {
        if (stack.isEmpty()) return null;

        if (stack.is(net.minecraft.world.item.Items.IRON_INGOT)) return "steel";
        if (stack.is(net.minecraft.world.item.Items.COPPER_INGOT)) return "copper";
        if (stack.is(net.minecraft.world.item.Items.GOLD_INGOT)) return "gold";
        if (stack.is(net.minecraft.world.item.Items.GLASS)) return "glass";

        return null;
    }
    public static ItemStack getItemFromStorageKey(String key) {
        Item item = switch (key.toLowerCase()) {
            case "steel"  -> Items.IRON_INGOT;
            case "copper" -> Items.COPPER_INGOT;
            case "gold"   -> Items.GOLD_INGOT;
            case "glass"  -> Items.GLASS;
            default -> null;
        };

        return item != null ? new ItemStack(item) : ItemStack.EMPTY;
    }
}