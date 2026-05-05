package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents;

import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.util.RandomSource;

public abstract class ArtefactReaction {
    public abstract void execute(ArtefactContext context, ArtefactNode node);
    public boolean canSpawn(int depth, int tier, RandomSource random) {
        return true;
    }
}