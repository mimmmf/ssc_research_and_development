package net.mcreator.sscresearchanddevelopment.component.fabricator;

import io.netty.buffer.ByteBuf;
import net.mcreator.sscresearchanddevelopment.block.entity.AutolatheBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record S2CSyncCraftingState(
        BlockPos pos,
        Map<String, Integer> materials,
        List<ResourceLocation> queue,
        Optional<ResourceLocation> activeRecipeId,
        int craftProgress,
        int craftTotalTime,
        List<String> techs
) implements CustomPacketPayload {
    public static final Type<S2CSyncCraftingState> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("ssc", "sync_crafting_state"));

    private static final StreamCodec<ByteBuf, Map<String, Integer>> MAP_STREAM_CODEC =
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT);

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncCraftingState> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, S2CSyncCraftingState::pos,
            MAP_STREAM_CODEC, S2CSyncCraftingState::materials,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), S2CSyncCraftingState::queue,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), S2CSyncCraftingState::activeRecipeId,
            ByteBufCodecs.VAR_INT, S2CSyncCraftingState::craftProgress,
            ByteBufCodecs.VAR_INT, S2CSyncCraftingState::craftTotalTime,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), S2CSyncCraftingState::techs,
            S2CSyncCraftingState::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handleClient(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.level.getBlockEntity(pos) instanceof AutolatheBlockEntity be) {
                be.syncFromPacket(materials, queue, activeRecipeId.orElse(null), craftProgress, craftTotalTime, techs);
            }
        });
    }
}