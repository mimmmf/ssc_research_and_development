package net.mcreator.sscresearchanddevelopment.network;

import net.mcreator.sscresearchanddevelopment.client.ClientProgressHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CProgressSyncPacket(int frame) implements CustomPacketPayload {
    public static final Type<S2CProgressSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ssc_research_and_development", "progress_sync"));

    public static final StreamCodec<FriendlyByteBuf, S2CProgressSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeInt(packet.frame),
            buf -> new S2CProgressSyncPacket(buf.readInt())
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void handleClient(S2CProgressSyncPacket packet, IPayloadContext context) {
        ClientProgressHolder.setFrame(packet.frame());
    }
}