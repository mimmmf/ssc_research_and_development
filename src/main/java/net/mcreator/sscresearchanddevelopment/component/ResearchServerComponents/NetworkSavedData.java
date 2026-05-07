package net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkSavedData extends SavedData {
    private final Map<UUID, ServerInstance> servers;


    public static final Codec<UUID> UUID_CODEC =
            Codec.STRING.xmap(
                    UUID::fromString,
                    UUID::toString
            );
    private static final Codec<Map<UUID, ServerInstance>> SERVERS_MAP_CODEC =
            Codec.unboundedMap(UUID_CODEC, ServerInstance.CODEC);

    public static final Codec<NetworkSavedData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    SERVERS_MAP_CODEC.fieldOf("servers").forGetter(ds -> ds.servers)
            ).apply(instance, NetworkSavedData::new)
    );

    public static final SavedDataType<NetworkSavedData> TYPE =
            new SavedDataType<>(
                    "rnd_network",
                    NetworkSavedData::new,
                    CODEC
            );

    public NetworkSavedData() {
        this.servers = new HashMap<>();
    }

    public NetworkSavedData(Map<UUID, ServerInstance> servers) {
        this.servers = new HashMap<>(servers);
    }

    public static NetworkSavedData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(TYPE);
        }
        return null;
    }

    public void addServer(ServerInstance inst) {
        if (servers.containsKey(inst.getServerId()))
            return;

        servers.put(inst.getServerId(), inst);
        setDirty();
    }

    public ServerInstance getServer(UUID id) {
        return servers.get(id);
    }

    public Map<UUID, ServerInstance> getAllServers() {
        return servers;
    }
    public void removeServer(UUID id) {
        servers.remove(id);
        setDirty();
    }
    public void setPoints(UUID serverId, int points) {
        ServerInstance server = servers.get(serverId);

        if (server != null) {
            server.setPoints(points);
            setDirty();
        }
    }
    public void addPoints(UUID serverId, int points) {
        ServerInstance server = servers.get(serverId);

        if (server != null) {
            server.setPoints(server.getPoints() + points);
            setDirty();
            System.out.println("RND DATA DIRTY");
        }
    }
    @Override
    public void setDirty() {
        super.setDirty();
    }

    public void removeServerAt(BlockPos pos) {
        UUID toRemove = servers.entrySet().stream()
                .filter(entry -> entry.getValue().getPos().equals(pos))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (toRemove != null) {
            servers.remove(toRemove);
            setDirty();
            System.out.println("Server at " + pos + " removed from network registry.");
        }
    }
}