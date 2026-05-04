package net.mcreator.sscresearchanddevelopment.component.subcomponents;

import net.mcreator.sscresearchanddevelopment.component.ArtifactProperties;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArtefactGenerator {

    public static ArtifactProperties generate(int tier, RandomSource random) {
        int budget = switch(tier) {
            case 0 -> random.nextInt(100, 301);
            case 1 -> random.nextInt(400, 801);
            default -> random.nextInt(1000, 2001);
        };

        Map<Integer, ArtefactNode> nodes = new HashMap<>();
        int nodeIdCounter = 0;

        // Создаем корневой узел
        int rootPoints = (int) (budget * 0.1); // 10% в корень
        budget -= rootPoints;

        ArtefactNode root = new ArtefactNode(
                nodeIdCounter++, 0, rootPoints, NodeStatus.ACTIVE, new ArrayList<>(), "", ""
        );
        nodes.put(root.id(), root);

        // Очередь для наращивания глубины
        List<Integer> parentPool = new ArrayList<>();
        parentPool.add(root.id());

        while (budget > 20 && nodes.size() < 15) {
            int parentId = parentPool.get(random.nextInt(parentPool.size()));
            ArtefactNode parent = nodes.get(parentId);

            int nodePoints = random.nextInt(20, Math.max(21, budget / 2));
            budget -= nodePoints;

            int newId = nodeIdCounter++;
            int newDepth = parent.depth() + 1;

            ArtefactNode newNode = new ArtefactNode(
                    newId, newDepth, nodePoints, NodeStatus.LOCKED, new ArrayList<>(), "", ""
            );

            // Связываем
            parent.childIds().add(newId);
            nodes.put(newId, newNode);
            parentPool.add(newId);
        }

        return new ArtifactProperties(tier, budget, nodes);
    }
}