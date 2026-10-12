package net.mcreator.sscresearchanddevelopment.client;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.mcreator.sscresearchanddevelopment.component.technologies.ResearchNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientResearchCache {
    public static final Map<String, ResearchNode> NODES = new ConcurrentHashMap<>();

    public static void updateNodesFromJson(String json) {
        if (json == null || json.isEmpty()) {
            System.out.println("[CLIENT] Received empty research JSON.");
            return;
        }

        try {
            JsonElement rawElement = JsonParser.parseString(json);
            JsonArray cleanedArray = new JsonArray();

            if (rawElement.isJsonObject()) {
                JsonObject rootObj = rawElement.getAsJsonObject();
                for (String key : rootObj.keySet()) {
                    JsonElement element = rootObj.get(key);
                    if (element.isJsonObject()) {
                        cleanedArray.add(cleanNodeJson(element.getAsJsonObject()));
                    }
                }
            } else if (rawElement.isJsonArray()) {
                for (JsonElement element : rawElement.getAsJsonArray()) {
                    if (element.isJsonObject()) {
                        cleanedArray.add(cleanNodeJson(element.getAsJsonObject()));
                    }
                }
            }

            List<ResearchNode> parsedNodes = ResearchNode.Serializer.LIST_CODEC
                    .parse(JsonOps.INSTANCE, cleanedArray)
                    .getOrThrow();

            NODES.clear();
            for (ResearchNode node : parsedNodes) {
                NODES.put(node.getId(), node);
            }

            System.out.println("[CLIENT] Research nodes successfully synced: " + NODES.size());

        } catch (Exception e) {
            System.err.println("[CLIENT] Failed to parse research nodes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Map<String, ResearchNode> getNodes() {
        return NODES;
    }

    private static JsonObject cleanNodeJson(JsonObject rawNode) {
        JsonObject node = rawNode.deepCopy();

        // 1. Очистка и нормализация Icon
        if (node.has("icon")) {
            JsonElement iconEl = node.get("icon");
            if (iconEl.isJsonObject()) {
                JsonObject iconObj = iconEl.getAsJsonObject();
                String ns = iconObj.has("namespace") ? iconObj.get("namespace").getAsString() : "minecraft";
                String path = iconObj.has("path") ? iconObj.get("path").getAsString() : "";
                node.addProperty("icon", ns + ":" + path);
            }
        }

        // 2. Нормализация parents
        if (!node.has("parents") || node.get("parents").isJsonNull()) {
            node.add("parents", new JsonArray());
        }

        // 3. ИСПРАВЛЕНИЕ: Превращаем Component (JsonObject) обратно в простую String
        // Перечислите здесь все текстовые поля вашей ResearchNode (например: "title", "description", "name")
        flattenComponentToString(node, "title");
        flattenComponentToString(node, "description");
        flattenComponentToString(node, "name");

        return node;
    }
    private static void flattenComponentToString(JsonObject node, String fieldName) {
        if (!node.has(fieldName)) return;
        JsonElement el = node.get(fieldName);
        if (el.isJsonObject()) {
            JsonObject compObj = el.getAsJsonObject();
            String extractedText = "";
            if (compObj.has("contents") && compObj.get("contents").isJsonObject()) {
                JsonObject contents = compObj.getAsJsonObject("contents");
                if (contents.has("text")) {
                    extractedText = contents.get("text").getAsString();
                }
            } else if (compObj.has("text")) {
                extractedText = compObj.get("text").getAsString();
            }
            node.addProperty(fieldName, extractedText);
        }
    }
}