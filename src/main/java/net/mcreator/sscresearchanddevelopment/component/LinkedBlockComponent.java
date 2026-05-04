package net.mcreator.sscresearchanddevelopment.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record LinkedBlockComponent(Optional<BlockPos> linked_block) {
    public static final Codec<LinkedBlockComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.optionalFieldOf("linked_block").forGetter(LinkedBlockComponent::linked_block)
            ).apply(instance, LinkedBlockComponent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, LinkedBlockComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), LinkedBlockComponent::linked_block,
            LinkedBlockComponent::new);
}