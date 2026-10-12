package net.mcreator.sscresearchanddevelopment.network;

import net.mcreator.sscresearchanddevelopment.component.fabricator.AbstractFabricatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SRequestFabricatorSync(BlockPos pos) implements CustomPacketPayload {
    public static final Type<C2SRequestFabricatorSync> TYPE = new Type<>(ResourceLocation.parse("ssc_research_and_development:request_fabricator_sync"));

    public static final StreamCodec<FriendlyByteBuf, C2SRequestFabricatorSync> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, C2SRequestFabricatorSync::pos,
            C2SRequestFabricatorSync::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(C2SRequestFabricatorSync payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player.level().getBlockEntity(payload.pos()) instanceof AbstractFabricatorBlockEntity be) {
                be.syncToClients();
            }
        });
    }
}