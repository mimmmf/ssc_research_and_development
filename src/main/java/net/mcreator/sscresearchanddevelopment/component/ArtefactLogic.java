package net.mcreator.sscresearchanddevelopment.component;

import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.NodeStatus;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class ArtefactLogic {
    public static final Map<String, ArtefactContext.IArtefactTrigger> TRIGGERS = new HashMap<>();
    public static final Map<String, ArtefactContext.IArtefactReaction> REACTIONS = new HashMap<>();

    public static void init() {
        // РЕАКЦИИ
        REACTIONS.put("none", (ctx, node) -> {});
        REACTIONS.put("explosion", (ctx, node) -> {
            ctx.level().explode(null, ctx.pos().getX(), ctx.pos().getY(), ctx.pos().getZ(), 2.0F, Level.ExplosionInteraction.BLOCK);
        });
        REACTIONS.put("spawn_mob", (ctx, node) -> {
            EntityType.ZOMBIE.spawn((ServerLevel)ctx.level(), ctx.pos(), EntitySpawnReason.EVENT);
        });

        // ТРИГГЕРЫ
        TRIGGERS.put("timer", (ctx, node, args) -> ctx.level().getGameTime() % 200 == 0);
        TRIGGERS.put("hit", (ctx, node, args) -> args.length > 0 && args[0] instanceof String s && s.equals("attack"));
        TRIGGERS.put("heat", (ctx, node, args) -> ctx.level().getBiome(ctx.pos()).value().getBaseTemperature() > 1.0f);
    }
    public static void extractNode(ItemStack artefact, int nodeId) {
        ArtifactProperties props = artefact.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());
        if (props == null) return;

        Map<Integer, ArtefactNode> nodes = new HashMap<>(props.nodes());
        ArtefactNode target = nodes.get(nodeId);

        if (target != null && target.status() == NodeStatus.READY_TO_EXTRACT) {
            // 1. Помечаем текущий узел как завершенный
            nodes.put(nodeId, target.withStatus(NodeStatus.EXHAUSTED));

            // 2. Активируем потомков
            for (int childId : target.childIds()) {
                ArtefactNode child = nodes.get(childId);
                if (child != null && child.status() == NodeStatus.LOCKED) {
                    nodes.put(childId, child.withStatus(NodeStatus.ACTIVE));
                }
            }

            // 3. Добавляем очки в общую сумму артефакта (опционально)
            int newTotalPoints = props.totalPoints() + target.points();

            // Обновляем компонент
            artefact.set(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get(),
                    new ArtifactProperties(props.tier(), newTotalPoints, nodes));
        }
    }
}