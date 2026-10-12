package net.mcreator.sscresearchanddevelopment.network;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.component.technologies.ResearchNode;
import net.mcreator.sscresearchanddevelopment.init.ClientNetworkHandler;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record S2CUpdateConsoleData(
        UUID serverId,
        int points,
        List<String> unlockedTechs,
        String nodesJson
) implements CustomPacketPayload {

    public static final Type<S2CUpdateConsoleData> TYPE = new Type<>(ResourceLocation.parse("ssc:update_console_data"));

    public static final StreamCodec<FriendlyByteBuf, S2CUpdateConsoleData> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, S2CUpdateConsoleData::serverId,
            ByteBufCodecs.VAR_INT, S2CUpdateConsoleData::points,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), S2CUpdateConsoleData::unlockedTechs,
            ByteBufCodecs.STRING_UTF8, S2CUpdateConsoleData::nodesJson,
            S2CUpdateConsoleData::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(S2CUpdateConsoleData payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientNetworkHandler.handleUpdateConsoleData(payload);
        });
    }
    public static String getSerializedNodes() {
        var nodesMap = SscResearchAndDevelopmentMod.ResearchDataHandler.MANAGER != null
                ? SscResearchAndDevelopmentMod.ResearchDataHandler.MANAGER.getNodes()
                : java.util.Collections.emptyMap();

        List<ResearchNode> nodeList = new ArrayList<>((java.util.Collection<ResearchNode>) nodesMap.values());
        try {
            com.google.gson.JsonElement jsonElement = ResearchNode.Serializer.LIST_CODEC
                    .encodeStart(com.mojang.serialization.JsonOps.INSTANCE, nodeList)
                    .getOrThrow();
            return jsonElement.toString();
        } catch (Exception e) {
            return "[]";
        }
    }
}