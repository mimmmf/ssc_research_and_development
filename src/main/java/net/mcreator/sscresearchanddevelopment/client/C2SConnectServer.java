package net.mcreator.sscresearchanddevelopment.client;

import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactconsoleBlockEntity;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.NetworkSavedData;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.ServerInstance;
import net.mcreator.sscresearchanddevelopment.interfaces.IServerLinked;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record C2SConnectServer(BlockPos pos, UUID serverId)
        implements CustomPacketPayload {

    public static final Type<C2SConnectServer> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("ssc", "connect_server"));

    public static final StreamCodec<FriendlyByteBuf, C2SConnectServer> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, C2SConnectServer::pos,
                    UUIDUtil.STREAM_CODEC, C2SConnectServer::serverId,
                    C2SConnectServer::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleServer(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            Level level = player.level();
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof IServerLinked linked))
                return;
            linked.setConnectedServerId(serverId);
            be.setChanged();
        });
    }
    public static final UUID EMPTY = new UUID(0, 0);
}