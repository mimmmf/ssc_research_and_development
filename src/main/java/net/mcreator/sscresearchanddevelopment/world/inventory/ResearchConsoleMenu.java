package net.mcreator.sscresearchanddevelopment.world.inventory;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.block.entity.ResearchConsoleBlockEntity;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.ServerInstance;
import net.mcreator.sscresearchanddevelopment.network.S2CUpdateConsoleData;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;

public class ResearchConsoleMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final Level level;
    private final UUID connectedServer;
    public ResearchConsoleMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(SscResearchAndDevelopmentModMenus.RESEARCH_CONSOLE.get(), id);
        this.level = inv.player.level();
        this.pos = buf.readBlockPos();
        this.connectedServer =
                buf.readNullable(b -> b.readUUID());
    }

    public ResearchConsoleMenu(int id, Inventory inv, BlockPos pos, @Nullable ServerInstance server) {
        super(SscResearchAndDevelopmentModMenus.RESEARCH_CONSOLE.get(), id);
        this.level = inv.player.level();
        this.pos = pos;

        this.connectedServer = (server != null) ? server.getServerId() : null;

        if (server != null && inv.player instanceof ServerPlayer serverPlayer) {
            var nodesMap = SscResearchAndDevelopmentMod.ResearchDataHandler.MANAGER != null
                    ? SscResearchAndDevelopmentMod.ResearchDataHandler.MANAGER.getNodes()
                    : java.util.Collections.emptyMap();
            com.google.gson.Gson gson = new com.google.gson.Gson();
            String nodesJson = gson.toJson(nodesMap);
            serverPlayer.connection.send(new S2CUpdateConsoleData(
                    server.getServerId(),
                    server.getPoints(),
                    new ArrayList<>(server.getUnlockedTechs()),
                    S2CUpdateConsoleData.getSerializedNodes()
            ));
        }
    }
    public ResearchConsoleBlockEntity getBlockEntity() {
        if (this.level.getBlockEntity(this.pos) instanceof ResearchConsoleBlockEntity be) {
            return be;
        }
        return null;
    }

    public BlockPos getPos() { return this.pos; }
    public UUID getConnectedServer() { return this.connectedServer; }

    @Override public ItemStack quickMoveStack(Player player, int i) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) { return true; }
}