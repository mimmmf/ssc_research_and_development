package net.mcreator.sscresearchanddevelopment.network;

import io.netty.buffer.ByteBuf;
import net.mcreator.sscresearchanddevelopment.init.ClientNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

public record S2CUpdateServerList(BlockPos consolePos, List<ServerInfo> servers, UUID connectedId) implements CustomPacketPayload {
    public static final Type<S2CUpdateServerList> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ssc", "update_server_list"));

    public record ServerInfo(UUID id, String name, int points) {
        public static final StreamCodec<ByteBuf, ServerInfo> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, ServerInfo::id,
                ByteBufCodecs.STRING_UTF8, ServerInfo::name,
                ByteBufCodecs.VAR_INT, ServerInfo::points,
                ServerInfo::new
        );
    }

    public static final StreamCodec<FriendlyByteBuf, S2CUpdateServerList> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, S2CUpdateServerList::consolePos,
                    ServerInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), S2CUpdateServerList::servers,
                    UUIDUtil.STREAM_CODEC, S2CUpdateServerList::connectedId,
                    S2CUpdateServerList::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handleClient(IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientNetworkHandler.handleUpdateServerList(this);
        });
    }
}