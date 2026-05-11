package net.mcreator.sscresearchanddevelopment.component.fabricator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;

public record MaterialCost(Ingredient ingredient, int count) {
    public static final Codec<MaterialCost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(MaterialCost::ingredient),
            Codec.INT.fieldOf("count").forGetter(MaterialCost::count)
    ).apply(instance, MaterialCost::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MaterialCost> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, MaterialCost::ingredient,
            ByteBufCodecs.VAR_INT, MaterialCost::count,
            MaterialCost::new
    );
}