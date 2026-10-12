package net.mcreator.sscresearchanddevelopment.network;

import net.mcreator.sscresearchanddevelopment.component.ArtifactProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod.MODID;

public record ExtractPointsPayload(BlockPos pos, int nodeId) implements CustomPacketPayload {
    public static final Type<ExtractPointsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "extract_points"));

    public static final StreamCodec<FriendlyByteBuf, ExtractPointsPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ExtractPointsPayload::pos,
            ByteBufCodecs.VAR_INT, ExtractPointsPayload::nodeId,
            ExtractPointsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ExtractPointsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ArtifactProperties.extractNodePoints(serverPlayer, payload.pos(), payload.nodeId());
            }
        });
    }
}