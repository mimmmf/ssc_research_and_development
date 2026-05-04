package net.mcreator.sscresearchanddevelopment.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.NodeStatus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ArtifactProperties(
        int tier,
        int totalPoints,
        Map<Integer, ArtefactNode> nodes
) {
    private static final Codec<Map<Integer, ArtefactNode>> NODES_MAP_CODEC =
            Codec.unboundedMap(Codec.STRING, ArtefactNode.CODEC).xmap(
                    sMap -> sMap.entrySet().stream().collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), Map.Entry::getValue)),
                    iMap -> iMap.entrySet().stream().collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue))
            );

    public static final Codec<ArtifactProperties> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("tier").forGetter(ArtifactProperties::tier),
                    Codec.INT.fieldOf("total_points").forGetter(ArtifactProperties::totalPoints),
                    NODES_MAP_CODEC.fieldOf("nodes").forGetter(ArtifactProperties::nodes)
            ).apply(instance, ArtifactProperties::new));

    // StreamCodec (для сети) лучше оставить ручным для стабильности и производительности
    public static final StreamCodec<RegistryFriendlyByteBuf, ArtifactProperties> STREAM_CODEC = StreamCodec.of(
            (buf, val) -> {
                buf.writeVarInt(val.tier());
                buf.writeVarInt(val.totalPoints());
                buf.writeMap(val.nodes(), ByteBufCodecs.VAR_INT, (b, node) -> {
                    b.writeVarInt(node.id());
                    b.writeVarInt(node.depth());
                    b.writeVarInt(node.points()); // Добавлены скобки
                    b.writeUtf(node.status().name());
                    // Запись списка
                    ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).encode(b, node.childIds());
                    b.writeUtf(node.triggerId());  // Добавлены скобки
                    b.writeUtf(node.reactionId()); // Добавлены скобки
                });
            },
            buf -> {
                int tier = buf.readVarInt();
                int total = buf.readVarInt();
                Map<Integer, ArtefactNode> nodes = buf.readMap(
                        HashMap::new,
                        ByteBufCodecs.VAR_INT,
                        b -> new ArtefactNode(
                                b.readVarInt(),
                                b.readVarInt(),
                                b.readVarInt(),
                                NodeStatus.valueOf(b.readUtf()),
                                ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).decode(b),
                                b.readUtf(), // triggerId
                                b.readUtf() // reactionId
                        )
                );
                return new ArtifactProperties(tier, total, nodes);
            }
    );
    public static ArtifactProperties generate(int tier, RandomSource random) {
        int budget = switch (tier) {
            case 0 -> random.nextInt(100, 301);
            case 1 -> random.nextInt(400, 801);
            default -> random.nextInt(1000, 2001);
        };

        Map<Integer, ArtefactNode> nodes = new HashMap<>();
        int nodeIdCounter = 0;

        int rootPoints = (int) (budget * 0.15);
        int remainingPoints = budget - rootPoints;

        ArtefactNode root = new ArtefactNode(
                nodeIdCounter++, 0, rootPoints, NodeStatus.ACTIVE,
                new ArrayList<>(), "timer", "none"
        );
        nodes.put(root.id(), root);

        List<Integer> availableParents = new ArrayList<>();
        availableParents.add(root.id());

        while (remainingPoints > 30 && nodes.size() < 12) {
            int parentId = availableParents.get(random.nextInt(availableParents.size()));
            ArtefactNode parent = nodes.get(parentId);

            int nodePoints = random.nextInt(30, Math.max(31, remainingPoints / 2));
            int newId = nodeIdCounter++;
            int depth = parent.depth() + 1;

            ArtefactNode newNode = new ArtefactNode(
                    newId, depth, nodePoints, NodeStatus.LOCKED,
                    new ArrayList<>(), "timer", "reaction_type_" + depth
            );

            parent.childIds().add(newId);

            nodes.put(newId, newNode);
            availableParents.add(newId);
            remainingPoints -= nodePoints;
        }

        return new ArtifactProperties(tier, budget, nodes);
    }
    public ArtifactProperties withNodeStatus(int nodeId, NodeStatus newStatus) {
        Map<Integer, ArtefactNode> newNodes = new HashMap<>(this.nodes);
        ArtefactNode oldNode = newNodes.get(nodeId);
        if (oldNode != null) {
            // Создаем копию узла с новым статусом
            newNodes.put(nodeId, new ArtefactNode(
                    oldNode.id(), oldNode.depth(), oldNode.points(),
                    newStatus, oldNode.childIds(), oldNode.triggerId(), oldNode.reactionId()
            ));
        }
        return new ArtifactProperties(this.tier, this.totalPoints, newNodes);
    }
}
