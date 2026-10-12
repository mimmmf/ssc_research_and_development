package net.mcreator.sscresearchanddevelopment.network;

import net.mcreator.sscresearchanddevelopment.client.ClientFabricatorCache;
import net.mcreator.sscresearchanddevelopment.component.fabricator.FabricatorRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record S2CSyncFabricatorRecipes(Map<ResourceLocation, FabricatorRecipe> recipes) implements CustomPacketPayload {
    public static final Type<S2CSyncFabricatorRecipes> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ssc", "sync_fabricator_recipes"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncFabricatorRecipes> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, FabricatorRecipe.Serializer.STREAM_CODEC),
            S2CSyncFabricatorRecipes::recipes,
            S2CSyncFabricatorRecipes::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(S2CSyncFabricatorRecipes payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientFabricatorCache.setRecipes(payload.recipes()));
    }
}