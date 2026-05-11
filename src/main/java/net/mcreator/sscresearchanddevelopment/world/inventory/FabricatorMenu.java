package net.mcreator.sscresearchanddevelopment.world.inventory;

import net.mcreator.sscresearchanddevelopment.block.entity.AutolatheBlockEntity;
import net.mcreator.sscresearchanddevelopment.component.fabricator.AbstractFabricatorBlockEntity;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FabricatorMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final Level level;
    public FabricatorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readBlockPos());
    }

    public FabricatorMenu(int id, Inventory inv, BlockPos pos) {
        super(SscResearchAndDevelopmentModMenus.FABRICATOR.get(), id);

        this.level = inv.player.level();
        this.pos = pos;
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

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
