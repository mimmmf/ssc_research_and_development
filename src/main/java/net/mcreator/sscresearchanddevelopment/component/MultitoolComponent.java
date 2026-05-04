package net.mcreator.sscresearchanddevelopment.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record MultitoolComponent (boolean isListType) {
    public static final Codec<MultitoolComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("isListType").forGetter(MultitoolComponent::isListType)
            ).apply(instance, MultitoolComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MultitoolComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, MultitoolComponent::isListType,
            MultitoolComponent::new);
}
