package net.mcreator.sscresearchanddevelopment.init;

import net.mcreator.sscresearchanddevelopment.block.entity.AutolatheBlockEntity;
import net.mcreator.sscresearchanddevelopment.client.ClientResearchCache;
import net.mcreator.sscresearchanddevelopment.network.S2CUpdateServerList;
import net.mcreator.sscresearchanddevelopment.client.screens.ResearchConsoleScreen;
import net.mcreator.sscresearchanddevelopment.client.screens.ServersScreen;
import net.mcreator.sscresearchanddevelopment.network.S2CSyncCraftingState;
import net.mcreator.sscresearchanddevelopment.network.S2CUpdateConsoleData;
import net.minecraft.client.Minecraft;
public class ClientNetworkHandler {
    public static void handleUpdateServerList(S2CUpdateServerList packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.setScreen(new ServersScreen(packet.consolePos(), packet.servers(), packet.connectedId()));
        }
    }
    public static void handleSyncCraftingState(S2CSyncCraftingState packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.getBlockEntity(packet.pos()) instanceof AutolatheBlockEntity be) {
            be.syncFromPacket(
                    packet.materials(),
                    packet.queue(),
                    packet.activeRecipeId().orElse(null),
                    packet.craftProgress(),
                    packet.craftTotalTime(),
                    packet.techs()
            );
        }
    }
    public static void handleUpdateConsoleData(S2CUpdateConsoleData payload) {
        ClientResearchCache.updateNodesFromJson(payload.nodesJson());
        if (Minecraft.getInstance().screen instanceof ResearchConsoleScreen screen) {
            screen.updateMenuState(0, "connectedServer", payload.serverId());
            screen.updateMenuState(0, "points", payload.points());
            screen.updateMenuState(0, "unlockedTechs", payload.unlockedTechs());
            screen.rebuildTechTree();
        }
    }
    public static void handleMenuStateUpdate(int elementType, String name, Object elementState) {
        if (Minecraft.getInstance().screen instanceof SscResearchAndDevelopmentModScreens.ScreenAccessor accessor) {
            accessor.updateMenuState(elementType, name, elementState);
        }
    }
    public static void updateScreenState(int elementType, String name, Object elementState) {
        if (net.minecraft.client.Minecraft.getInstance().screen instanceof SscResearchAndDevelopmentModScreens.ScreenAccessor accessor) {
            accessor.updateMenuState(elementType, name, elementState);
        }
    }
}