package net.mcreator.sscresearchanddevelopment.component.subcomponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mcreator.sscresearchanddevelopment.enums.NodeStatus;

import java.util.List;

public record ArtefactNode(
        int id,
        int depth,
        int points,
        NodeStatus status,
        List<Integer> childIds,
        String triggerId,
        String reactionId,
        int timer,
        int maxTimer
) {
    public static final Codec<ArtefactNode> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("id").forGetter(ArtefactNode::id),
                    Codec.INT.fieldOf("depth").forGetter(ArtefactNode::depth),
                    Codec.INT.fieldOf("points").forGetter(ArtefactNode::points),
                    Codec.STRING.xmap(NodeStatus::valueOf, NodeStatus::name).fieldOf("status").forGetter(ArtefactNode::status),
                    Codec.INT.listOf().fieldOf("children").forGetter(ArtefactNode::childIds),
                    Codec.STRING.fieldOf("triggerId").forGetter(ArtefactNode::triggerId),
                    Codec.STRING.fieldOf("reactionId").forGetter(ArtefactNode::reactionId),
                    Codec.INT.fieldOf("timer").forGetter(ArtefactNode::timer),
                    Codec.INT.fieldOf("maxTimer").forGetter(ArtefactNode::maxTimer)
            ).apply(instance, ArtefactNode::new));

    public ArtefactNode update(NodeStatus newStatus, int newTimer) {
        return new ArtefactNode(id, depth, points, newStatus, childIds, triggerId, reactionId, newTimer, maxTimer);
    }
}