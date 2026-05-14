package net.mcreator.sscresearchanddevelopment.block.entity;

import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.NetworkSavedData;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.ServerInstance;
import net.mcreator.sscresearchanddevelopment.component.fabricator.*;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModBlockEntities;
import net.mcreator.sscresearchanddevelopment.world.inventory.FabricatorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;

public class AutolatheBlockEntity extends AbstractFabricatorBlockEntity {
    private static final List<String> MATERIALS = List.of("steel", "copper", "gold", "glass");
    public AutolatheBlockEntity(BlockPos position, BlockState state) {
        super(SscResearchAndDevelopmentModBlockEntities.AUTOLATHE.get(), position, state);
    }

    @Override
    public String getFabricatorType() {
        return "autolathe";
    }

    @Override
    public List<String> getSupportedMaterials() {
        return MATERIALS;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Автолат");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        ServerInstance server = resolveServer();
        if (server == null) {
            this.linkedServerId = null;
            setChanged();
        }
        return new FabricatorMenu(i, inventory, this.worldPosition, server.getServerId());
    }
}