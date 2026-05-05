package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents;

import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.world.level.Level;

public class ExplosionReaction extends ArtefactReaction {
    private final float power;

    public ExplosionReaction(float power) {
        this.power = power;
    }

    @Override
    public void execute(ArtefactContext ctx, ArtefactNode node) {
        ctx.level().explode(null, ctx.pos().getX(), ctx.pos().getY(), ctx.pos().getZ(),
                this.power, Level.ExplosionInteraction.BLOCK);
    }
}