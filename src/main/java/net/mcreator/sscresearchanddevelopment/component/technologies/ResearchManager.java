package net.mcreator.sscresearchanddevelopment.component.technologies;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class ResearchManager extends SimpleJsonResourceReloadListener<JsonElement> {
    private final Map<String, ResearchNode> nodes = new HashMap<>();
    private final HolderLookup.Provider registries;


    public ResearchManager(HolderLookup.Provider registries) {
        super(
                Codec.PASSTHROUGH.xmap(
                        dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
                        json -> new Dynamic<>(JsonOps.INSTANCE, json)
                ),
                FileToIdConverter.json("research/nodes")
        );
        this.registries = registries;
    }

    public Map<String, ResearchNode> getNodes() {
        return this.nodes;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        nodes.clear();
        object.forEach((location, element) -> {
            try {
                ResearchNode node = ResearchNode.Serializer.CODEC
                        .parse(JsonOps.INSTANCE, element)
                        .getOrThrow();
                nodes.put(node.getId(), node);
            } catch (Exception e) {
                System.err.println("Failed to load research node " + location + ": " + e.getMessage());
            }
        });
        System.out.println("Loaded research nodes: " + nodes.size());
    }
}