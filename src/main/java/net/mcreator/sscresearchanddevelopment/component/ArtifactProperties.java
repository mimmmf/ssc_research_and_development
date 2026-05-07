package net.mcreator.sscresearchanddevelopment.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactanalyzerBlockEntity;
import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactconsoleBlockEntity;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactRegistries;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.NetworkSavedData;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.ServerInstance;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.NodeStatus;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents;
import net.mcreator.sscresearchanddevelopment.interfaces.IServerLinked;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

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

    public static final StreamCodec<RegistryFriendlyByteBuf, ArtifactProperties> STREAM_CODEC = StreamCodec.of(
            (buf, val) -> {
                buf.writeVarInt(val.tier());
                buf.writeVarInt(val.totalPoints());
                buf.writeMap(val.nodes(), ByteBufCodecs.VAR_INT, (b, node) -> {
                    b.writeVarInt(node.id());
                    b.writeVarInt(node.depth());
                    b.writeVarInt(node.points());
                    b.writeUtf(node.status().name());
                    ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).encode(b, node.childIds());
                    b.writeUtf(node.triggerId());
                    b.writeUtf(node.reactionId());
                    b.writeVarInt(node.timer());
                    b.writeVarInt(node.maxTimer());
                });
            },
            buf -> {
                int tier = buf.readVarInt();
                int total = buf.readVarInt();
                Map<Integer, ArtefactNode> nodes = buf.readMap(
                        HashMap::new,
                        ByteBufCodecs.VAR_INT,
                        b -> new ArtefactNode(
                                b.readVarInt(), // id
                                b.readVarInt(), // depth
                                b.readVarInt(), // points
                                NodeStatus.valueOf(b.readUtf()), // status
                                ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()).decode(b), // childIds
                                b.readUtf(), // triggerId
                                b.readUtf(), // reactionId
                                b.readVarInt(), // НОВОЕ: читаем текущий timer
                                b.readVarInt()  // НОВОЕ: читаем maxTimer
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
                new ArrayList<>(), ArtefactRegistries.getRandomTrigger(0, tier, random), ArtefactRegistries.getRandomReaction(0, tier, random), 0, 40
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

            int delay = 20 + random.nextInt(Math.min(200, 20 * depth));

            String randomTriggerId = ArtefactRegistries.getRandomTrigger(depth, tier, random);
            String randomReactionId = ArtefactRegistries.getRandomReaction(depth, tier, random);

            ArtefactNode newNode = new ArtefactNode(
                    newId, depth, nodePoints, NodeStatus.LOCKED,
                    new ArrayList<>(),
                    randomTriggerId,
                    randomReactionId,
                    0,
                    delay
            );

            parent.childIds().add(newId);
            nodes.put(newId, newNode);
            availableParents.add(newId);
            remainingPoints -= nodePoints;
        }
        return new ArtifactProperties(tier, 0, nodes);
    }
    // -- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ --
    public static void extractNodePoints(ServerPlayer player, BlockPos pos, int nodeId) {
        if (player.level().getBlockEntity(pos) instanceof ArtefactconsoleBlockEntity console) {
            BlockPos analyzerPos = console.getLinkedPos().orElse(null);
            if (analyzerPos == null) return;

            if (player.level().getBlockEntity(analyzerPos) instanceof ArtefactanalyzerBlockEntity analyzer) {
                ItemStack stack = analyzer.getItem(0);

                if (stack.isEmpty()) return;

                ArtifactProperties props = stack.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());
                if (props == null) return;

                ArtefactNode node = props.nodes().get(nodeId);

                if (node != null && node.status() == NodeStatus.READY_TO_EXTRACT) {
                    Map<Integer, ArtefactNode> updatedNodes = new HashMap<>(props.nodes());
                    updatedNodes.put(nodeId, node.update(NodeStatus.SOLVED, 0));

                    int newStoredEnergy = Math.max(0, props.totalPoints() - node.points());

                    ArtifactProperties newProps = new ArtifactProperties(props.tier(), newStoredEnergy, updatedNodes);
                    stack.set(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get(), newProps);

                    analyzer.setItem(0, stack);

                    analyzer.setChanged();
                    player.level().sendBlockUpdated(analyzerPos, analyzer.getBlockState(), analyzer.getBlockState(), 3);

                    player.containerMenu.broadcastChanges();

                    player.level().playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0f, 0.8f);

                    if (console instanceof IServerLinked linked) {

                        ServerInstance server = linked.resolveServer();

                        if (server != null) {
                            NetworkSavedData data =
                                    NetworkSavedData.get(player.level());

                            if (data != null) {
                                data.addPoints(server.getServerId(), node.points());
                            }
                        }
                    }
                }
            }
        }
    }
}
