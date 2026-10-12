package net.mcreator.sscresearchanddevelopment.client;

import net.mcreator.sscresearchanddevelopment.component.fabricator.FabricatorRecipe;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientFabricatorCache {
    public static final Map<ResourceLocation, FabricatorRecipe> RECIPES = new ConcurrentHashMap<>();

    public static void setRecipes(Map<ResourceLocation, FabricatorRecipe> newRecipes) {
        RECIPES.clear();
        if (newRecipes != null) {
            RECIPES.putAll(newRecipes);
        }
        System.out.println("[CLIENT] Fabricator recipes updated: " + RECIPES.size());
    }

    public static Map<ResourceLocation, FabricatorRecipe> getRecipes() {
        return RECIPES;
    }
}