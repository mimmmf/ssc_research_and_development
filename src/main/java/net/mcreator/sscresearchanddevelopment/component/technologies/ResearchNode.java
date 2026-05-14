package net.mcreator.sscresearchanddevelopment.component.technologies;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ResearchNode {
    private final String id;
    private final Component name;
    private final Component description;
    private final ResourceLocation icon;
    private final int cost;
    private final List<String> parents;
    private final int x;
    private final int y;

    public ResearchNode(String id, Component name, Component description, ResourceLocation icon, int cost, List<String> parents, int x, int y) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.cost = cost;
        this.parents = parents;
        this.x = x;
        this.y = y;
    }

    // Геттеры
    public String getId() { return id; }
    public Component getName() { return name; }
    public Component getDescription() { return description; }
    public ResourceLocation getIcon() { return icon; }
    public int getCost() { return cost; }
    public List<String> getParents() { return parents; }
    public int getX() { return x; }
    public int getY() { return y; }

    public static class Serializer {
        public static final Codec<ResearchNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(ResearchNode::getId),
                ComponentSerialization.CODEC.fieldOf("name").forGetter(ResearchNode::getName),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(ResearchNode::getDescription),
                ResourceLocation.CODEC.fieldOf("icon").forGetter(ResearchNode::getIcon),
                Codec.INT.fieldOf("cost").forGetter(ResearchNode::getCost),
                Codec.STRING.listOf().fieldOf("parents").forGetter(ResearchNode::getParents),
                Codec.INT.fieldOf("x").forGetter(ResearchNode::getX),
                Codec.INT.fieldOf("y").forGetter(ResearchNode::getY)
        ).apply(instance, ResearchNode::new));
    }
}