package net.mcreator.sscresearchanddevelopment.client;

import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactconsoleBlockEntity;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.NetworkSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record C2SRequestServerList(BlockPos pos) implements CustomPacketPayload {
    public static final Type<C2SRequestServerList> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ssc", "request_server_list"));

    public static final StreamCodec<FriendlyByteBuf, C2SRequestServerList> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, C2SRequestServerList::pos,
            C2SRequestServerList::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handleServer(IPayloadContext context) {
        context.enqueueWork(() -> {
            NetworkSavedData networkData = NetworkSavedData.get(context.player().level());
            if (networkData == null) return;

            List<S2CUpdateServerList.ServerInfo> list = new ArrayList<>();
            networkData.getAllServers().forEach((id, inst) ->
                    list.add(new S2CUpdateServerList.ServerInfo(id, inst.getName(), inst.getPoints()))
            );

            ServerLevel level = (ServerLevel) context.player().level();
            BlockEntity be = level.getBlockEntity(pos);

            UUID connected = C2SConnectServer.EMPTY;

            if (be instanceof ArtefactconsoleBlockEntity console) {
                connected = console.connectedServer != null
                        ? console.connectedServer
                        : C2SConnectServer.EMPTY;
            }

            context.reply(new S2CUpdateServerList(this.pos, list, connected));
        });
    }
}