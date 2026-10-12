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
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
                        player.playSound(SscResearchAndDevelopmentSounds.SCAN_FINISH.get(), 1.0f, 1.0f);

                        String messageText = "Успешно разблокирована технология: " + node.getName().getString();
                        int channelColor = 13466829;
                        Component radioMessage = Component.literal("[Научный] Консоль исследований говорит, \"" + messageText + "\"")
                                .setStyle(net.minecraft.network.chat.Style.EMPTY.withColor(net.minecraft.network.chat.TextColor.fromRgb(channelColor)));
                        Item rndKeyItem = Ssc14ModItems.ENCRYPTION_KEY_RND.get();
                        boolean hasConnection = false;
                        try {
                            Ssc14ModVariables.MapVariables vars = Ssc14ModVariables.MapVariables.get(level);
                            hasConnection = vars.RND_Radio_Frequency;
                        } catch (Exception e) {
                            hasConnection = true;
                        }
                        if (hasConnection) {
                            for (Player target : level.players()) {
                                if (target.level() == level && hasHeadsetWithKeyCustom(target, rndKeyItem)) {
                                    target.displayClientMessage(radioMessage, false);
                                }
                            }
                        }
                        net.minecraft.server.MinecraftServer serverInstance = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
                        if (serverInstance != null) {
                            for (Player target : serverInstance.getPlayerList().getPlayers()) {
                                if (target.isSpectator()) {
                                    target.displayClientMessage(radioMessage, false);
                                }
                            }
                        }
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
    private static boolean hasHeadsetWithKeyCustom(net.minecraft.world.entity.player.Player player, net.minecraft.world.item.Item requiredKey) {
        net.minecraft.tags.TagKey<net.minecraft.world.item.Item> headsetTag = net.minecraft.tags.ItemTags.create(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ssc14", "headsets")
        );
        net.minecraft.world.entity.player.Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            net.minecraft.world.item.ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.is(headsetTag) && hasSpecificKeyInsideCustom(stack, requiredKey)) {
                return true;
            }
        }

        try {
            var atomicFound = new java.util.concurrent.atomic.AtomicBoolean(false);
            CuriosApi.getCuriosInventory(player).ifPresent((invHandler) -> {
                var earHandler = invHandler.getStacksHandler("ear");
                if (earHandler.isPresent()) {
                    net.neoforged.neoforge.items.IItemHandler handler = earHandler.get().getStacks();
                    for (int i = 0; i < handler.getSlots(); ++i) {
                        net.minecraft.world.item.ItemStack stack = handler.getStackInSlot(i);
                        if (!stack.isEmpty() && stack.is(headsetTag) && hasSpecificKeyInsideCustom(stack, requiredKey)) {
                            atomicFound.set(true);
                        }
                    }
                }
            });
            if (atomicFound.get()) return true;
        } catch (Exception ignored) {}

        return false;
    }

    private static boolean hasSpecificKeyInsideCustom(net.minecraft.world.item.ItemStack headset, net.minecraft.world.item.Item requiredKey) {
        IItemHandler container = headset.getCapability(
                Capabilities.ItemHandler.ITEM
        );
        if (container == null) {
            return false;
        }
        for (int i = 0; i < container.getSlots(); ++i) {
            ItemStack itemInSlot = container.getStackInSlot(i);
            if (!itemInSlot.isEmpty() && itemInSlot.is(requiredKey)) {
                return true;
            }
        }
        return false;
    }
}