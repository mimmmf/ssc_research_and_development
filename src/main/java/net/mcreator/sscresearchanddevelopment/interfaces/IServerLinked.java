package net.mcreator.sscresearchanddevelopment.interfaces;

import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.NetworkSavedData;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.ServerInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IServerLinked {

    @Nullable
    UUID getConnectedServerId();

    void setConnectedServerId(@Nullable UUID id);

    @Nullable
    Level getLinkedLevel();

    default boolean isServerValid() {
        return resolveServer() != null;
    }

    default @Nullable ServerInstance resolveServer() {
        UUID id = getConnectedServerId();
        if (id == null) return null;

        Level level = getLinkedLevel();
        if (level == null) return null;

        NetworkSavedData data = NetworkSavedData.get(level);
        if (data == null) return null;

        ServerInstance server = data.getServer(id);

        if (server == null) {
            setConnectedServerId(null);

            if (this instanceof BlockEntity be) {
                be.setChanged();
            }
        }

        return server;
    }
}