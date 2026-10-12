package net.mcreator.sscresearchanddevelopment.world.inventory;

import net.mcreator.sscresearchanddevelopment.component.fabricator.AbstractFabricatorBlockEntity;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class FabricatorMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final Level level;
    private final UUID connectedServer;
    public FabricatorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        super(SscResearchAndDevelopmentModMenus.FABRICATOR.get(), id);

        this.level = inv.player.level();
        this.pos = buf.readBlockPos();
        this.connectedServer =
                buf.readNullable(b -> b.readUUID());
    }

    public FabricatorMenu(int id, Inventory inv, BlockPos pos, UUID connectedServer) {
        super(SscResearchAndDevelopmentModMenus.FABRICATOR.get(), id);

        this.level = inv.player.level();
        this.pos = pos;
        this.connectedServer = connectedServer;
    }
    public AbstractFabricatorBlockEntity getBlockEntity() {
        if (this.level.getBlockEntity(this.pos) instanceof AbstractFabricatorBlockEntity be) {
            return be;
        }
        return null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }
    public UUID getConnectedServer() { return this.connectedServer; }
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
