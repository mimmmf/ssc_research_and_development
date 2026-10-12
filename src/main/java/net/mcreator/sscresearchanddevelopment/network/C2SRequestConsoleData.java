package net.mcreator.sscresearchanddevelopment.network;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.NetworkSavedData;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.ServerInstance;
import net.mcreator.sscresearchanddevelopment.world.inventory.ResearchConsoleMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.UUID;

public record C2SRequestConsoleData(BlockPos pos) implements CustomPacketPayload {
    public static final Type<C2SRequestConsoleData> TYPE = new Type<>(ResourceLocation.parse("ssc:request_console_data"));

    public static final StreamCodec<FriendlyByteBuf, C2SRequestConsoleData> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, C2SRequestConsoleData::pos,
            C2SRequestConsoleData::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(C2SRequestConsoleData payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player.containerMenu instanceof ResearchConsoleMenu menu) {
                Level level = player.level();
                NetworkSavedData data = NetworkSavedData.get(level);

                if (data != null) {
                    UUID serverId = menu.getConnectedServer();
                    if (serverId != null) {
                        ServerInstance inst = data.getServer(serverId);
                        if (inst != null) {
                            var nodesMap = SscResearchAndDevelopmentMod.ResearchDataHandler.MANAGER != null
                                    ? SscResearchAndDevelopmentMod.ResearchDataHandler.MANAGER.getNodes()
                                    : java.util.Collections.emptyMap();
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            String nodesJson = gson.toJson(nodesMap);
                            context.reply(new S2CUpdateConsoleData(
                                    inst.getServerId(),
                                    inst.getPoints(),
                                    new ArrayList<>(inst.getUnlockedTechs()),
                                    nodesJson
                            ));
                        }
                    }
                }
            }
        });
    }
}