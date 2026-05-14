package net.mcreator.sscresearchanddevelopment.component.technologies;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.NetworkSavedData;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.ServerInstance;
import net.mcreator.sscresearchanddevelopment.interfaces.IServerLinked;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;

public record C2SUnlockResearch(BlockPos pos, String techId) implements CustomPacketPayload {
    public static final Type<C2SUnlockResearch> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ssc_research_and_development", "unlock_research"));

    public static final StreamCodec<FriendlyByteBuf, C2SUnlockResearch> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, C2SUnlockResearch::pos,
            ByteBufCodecs.STRING_UTF8, C2SUnlockResearch::techId,
            C2SUnlockResearch::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handleServer(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            Level level = player.level();

            if (level.getBlockEntity(pos) instanceof IServerLinked linked) {
                ServerInstance server = linked.resolveServer();
                if (server == null) return;

                ResearchNode node = SscResearchAndDevelopmentMod.ResearchDataHandler.MANAGER.getNodes().get(techId);
                if (node == null) return;

                NetworkSavedData data = NetworkSavedData.get(level);
                if (data != null && data.unlockTechnology(server.getServerId(), node)) {
                    if (linked instanceof BlockEntity be) {
                        ServerInstance updatedServer = data.getServer(server.getServerId());
                        if (updatedServer != null && player instanceof ServerPlayer serverPlayer) {

                            serverPlayer.connection.send(new S2CUpdateConsoleData(
                                    updatedServer.getServerId(),
                                    updatedServer.getPoints(),
                                    new ArrayList<>(updatedServer.getUnlockedTechs())
                            ));
                        }
                    }
                }
            }
        });
    }
}