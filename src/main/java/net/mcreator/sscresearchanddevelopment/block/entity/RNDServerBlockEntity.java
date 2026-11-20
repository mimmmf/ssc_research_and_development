package net.mcreator.sscresearchanddevelopment.block.entity;

import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.NetworkSavedData;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.ServerInstance;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModBlockEntities;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentSounds;
import net.mcreator.sscresearchanddevelopment.interfaces.ILoopSoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;
import java.util.UUID;

public class RNDServerBlockEntity extends BlockEntity implements ILoopSoundPlayer {
    public UUID serverId;
    private boolean soundStarted = false;
    public RNDServerBlockEntity(BlockPos position, BlockState state) {
        super(SscResearchAndDevelopmentModBlockEntities.RNDSERVER.get(), position, state);
    }
    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        if (serverId  != null) valueOutput.putString("serverId", serverId.toString());
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        Optional<String> optional = valueInput.getString("serverId");
        if (optional.isPresent()) {
            try {
                this.serverId = UUID.fromString(optional.get());
            } catch (Exception e) {
                this.serverId = UUID.randomUUID();
                setChanged();
            }
        } else {
            this.serverId = null;
        }
    }

    public void initializeServer() {
        if (level == null || level.isClientSide)
            return;
        NetworkSavedData data = NetworkSavedData.get(level);
        if (data == null)
            return;
        if (serverId == null) {
            serverId = UUID.randomUUID();
            setChanged();
        }
        if (data.getServer(serverId) != null)
            return;
        int serverNumber = data.getAllServers().size() + 1;
        String friendlyName = "Сервер " + serverNumber;
        ServerInstance inst =
                new ServerInstance(serverId, worldPosition, friendlyName);
        data.addServer(inst);
        setChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level == null)
            return;

        if (level.isClientSide) {
            tryPlayLoopSound(this, soundStarted, () -> soundStarted = true);
        } else {
            if (serverId == null) {
                serverId = UUID.randomUUID();
                setChanged();
            }
            initializeServer();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide) {
            soundStarted = false;
        }
    }

    @Override
    public SoundEvent getLoopSound() {
        return SscResearchAndDevelopmentSounds.SERVER_FANS.get();
    }

    // --- СЕТЕВАЯ СИНХРОНИЗАЦИЯ ---
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

}