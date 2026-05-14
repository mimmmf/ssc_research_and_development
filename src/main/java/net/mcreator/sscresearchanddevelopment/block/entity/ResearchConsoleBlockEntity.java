package net.mcreator.sscresearchanddevelopment.block.entity;

import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.NetworkSavedData;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.ServerInstance;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModBlockEntities;
import net.mcreator.sscresearchanddevelopment.interfaces.IServerLinked;
import net.mcreator.sscresearchanddevelopment.world.inventory.ResearchConsoleMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ResearchConsoleBlockEntity extends BlockEntity implements MenuProvider, IServerLinked {
    public UUID connectedServer;
    public ResearchConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(SscResearchAndDevelopmentModBlockEntities.RESEARCH_CONSOLE.get(), pos, state);
    }
    @Override
    public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.worldPosition);

        buffer.writeNullable(
                this.connectedServer,
                (b, uuid) -> b.writeUUID(uuid)
        );
    }


    @Override
    public Component getDisplayName() {
        return Component.literal("Консоль исследований");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        ServerInstance server = resolveServer();
        if (server == null) {
            this.connectedServer = null;
            setChanged();
        }
        NetworkSavedData data = NetworkSavedData.get(level);
        return new ResearchConsoleMenu(i, inventory, this.worldPosition, NetworkSavedData.get(level).getServer(this.connectedServer));
    }

    @javax.annotation.Nullable
    @Override
    public UUID getConnectedServerId() {
        return connectedServer;
    }

    @Override
    public void setConnectedServerId(@javax.annotation.Nullable UUID id) {
        this.connectedServer = id;
    }

    @javax.annotation.Nullable
    @Override
    public Level getLinkedLevel() {
        return level;
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        if (connectedServer != null)
            valueOutput.putString("connectedServer", connectedServer.toString());
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        String uuid = valueInput.getString("connectedServer").orElse("");
        if (!uuid.isEmpty()) this.connectedServer = UUID.fromString(uuid);
        else this.connectedServer = null;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

}
