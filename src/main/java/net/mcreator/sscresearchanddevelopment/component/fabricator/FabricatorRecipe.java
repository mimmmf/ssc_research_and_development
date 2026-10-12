package net.mcreator.sscresearchanddevelopment.component.fabricator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

public class FabricatorRecipe implements Recipe<RecipeInput> {
    private final String category;
    private final ItemStack result;
    private final List<MaterialCost> materials;
    private final List<String> requiredTechnologies;
    private final int craftTime;

    public FabricatorRecipe(String category, ItemStack result, List<MaterialCost> materials, List<String> requiredTechnologies, int craftTime) {
        this.category = category;
        this.result = result;
        this.materials = materials;
        this.requiredTechnologies = requiredTechnologies;
        this.craftTime = craftTime;
    }

    public String getCategory() { return this.category; }
    public List<MaterialCost> getMaterials() { return this.materials; }
    public List<String> getRequiredTechnologies() { return this.requiredTechnologies; }
    public int getCraftTime() { return this.craftTime; }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return SscResearchAndDevelopmentModRecipes.FABRICATOR_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return SscResearchAndDevelopmentModRecipes.FABRICATOR_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return null;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return null;
    }

    public static class Serializer implements RecipeSerializer<FabricatorRecipe> {

        public static final MapCodec<FabricatorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("category").forGetter(FabricatorRecipe::getCategory),
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.result),
                MaterialCost.CODEC.listOf().fieldOf("materials").forGetter(FabricatorRecipe::getMaterials),
                Codec.STRING.listOf().fieldOf("required_technologies").forGetter(FabricatorRecipe::getRequiredTechnologies),
                Codec.INT.fieldOf("craft_time").forGetter(FabricatorRecipe::getCraftTime)
        ).apply(instance, FabricatorRecipe::new));

        public static final Codec<List<FabricatorRecipe>> LIST_CODEC = CODEC.codec().listOf();

        public static final StreamCodec<RegistryFriendlyByteBuf, FabricatorRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, FabricatorRecipe::getCategory,
                ItemStack.STREAM_CODEC, r -> r.result,
                MaterialCost.STREAM_CODEC.apply(ByteBufCodecs.list()), FabricatorRecipe::getMaterials,
                ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), FabricatorRecipe::getRequiredTechnologies,
                ByteBufCodecs.VAR_INT, FabricatorRecipe::getCraftTime,
                FabricatorRecipe::new
        );

        @Override
        public MapCodec<FabricatorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FabricatorRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}