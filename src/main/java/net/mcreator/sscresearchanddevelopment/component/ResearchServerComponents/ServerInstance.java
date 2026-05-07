package net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.UUID;

import static net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.NetworkSavedData.UUID_CODEC;

public class ServerInstance {
    public static final Codec<ServerInstance> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUID_CODEC.fieldOf("id").forGetter(ServerInstance::getServerId),
                    BlockPos.CODEC.fieldOf("pos").forGetter(ServerInstance::getPos),
                    Codec.INT.fieldOf("points").forGetter(ServerInstance::getPoints),
                    Codec.STRING.fieldOf("name").forGetter(ServerInstance::getName),
                    Codec.list(Codec.STRING).fieldOf("techs").forGetter(inst -> new ArrayList<>(inst.unlockedTechs))
            ).apply(instance, (id, pos, points, name, techs) -> {
                ServerInstance s = new ServerInstance(id, pos, name);
                s.setPoints(points);
                s.unlockedTechs.addAll(techs);
                return s;
            })
    );

    private final UUID serverId;
    private final BlockPos pos;
    private int points;
    private String name;
    private final java.util.Set<String> unlockedTechs = new java.util.HashSet<>();

    public ServerInstance(UUID serverId, BlockPos pos, String name) {
        this.serverId = serverId;
        this.pos = pos;
        this.name = name;
    }

    public UUID getServerId() { return serverId; }
    public BlockPos getPos() { return pos; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public String getName() { return name; }
}