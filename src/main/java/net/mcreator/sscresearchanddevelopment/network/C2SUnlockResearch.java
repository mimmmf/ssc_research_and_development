package net.mcreator.sscresearchanddevelopment.network;

import net.mcreator.ssc.init.Ssc14ModItems;
import net.mcreator.ssc.init.Ssc14ModSounds;
import net.mcreator.ssc.network.Ssc14ModVariables;
import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.NetworkSavedData;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.ServerInstance;
import net.mcreator.sscresearchanddevelopment.component.technologies.ResearchNode;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentSounds;
import net.mcreator.sscresearchanddevelopment.interfaces.IServerLinked;
import net.mcreator.sscresearchanddevelopment.libs.Ssc14Handle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;

import static net.mcreator.sscresearchanddevelopment.libs.Ssc14Handle.hasHeadsetWithKeyCustom;

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
                    if (player instanceof ServerPlayer serverPlayer) {
                        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SscResearchAndDevelopmentSounds.SCAN_FINISH.get(), SoundSource.BLOCKS, 1, 1);

                        String messageText = "Успешно разблокирована технология: " + node.getName().getString();

                        Ssc14Handle.sendRadioMessage(
                                level,
                                "Научный",
                                13466829,
                                Ssc14ModItems.ENCRYPTION_KEY_RND.get(),
                                "Консоль исследований",
                                messageText
                        );
                    }

                    if (linked instanceof BlockEntity be) {
                        ServerInstance updatedServer = data.getServer(server.getServerId());
                        if (updatedServer != null && player instanceof ServerPlayer serverPlayer) {
                            var nodesMap = SscResearchAndDevelopmentMod.ResearchDataHandler.MANAGER != null
                                    ? SscResearchAndDevelopmentMod.ResearchDataHandler.MANAGER.getNodes()
                                    : java.util.Collections.emptyMap();
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            String nodesJson = gson.toJson(nodesMap);
                            PacketDistributor.sendToPlayersInDimension((ServerLevel) level, new S2CUpdateConsoleData(
                                    updatedServer.getServerId(),
                                    updatedServer.getPoints(),
                                    new ArrayList<>(updatedServer.getUnlockedTechs()),
                                    S2CUpdateConsoleData.getSerializedNodes()
                            ));
                        }
                    }
                }
            }
        });
    }
}