package net.mcreator.sscresearchanddevelopment.component.fabricator;

import net.mcreator.sscresearchanddevelopment.block.entity.AutolatheBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SStartCraft(BlockPos pos, ResourceLocation recipeId) implements CustomPacketPayload {
    public static final Type<C2SStartCraft> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("ssc", "start_craft"));

    public static final StreamCodec<FriendlyByteBuf, C2SStartCraft> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, C2SStartCraft::pos,
                    ResourceLocation.STREAM_CODEC, C2SStartCraft::recipeId,
                    C2SStartCraft::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handleServer(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player.level().getBlockEntity(pos) instanceof AutolatheBlockEntity be) {
                be.tryQueueRecipe(player, recipeId);
            }
        });
    }
}