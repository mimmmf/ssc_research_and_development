package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.trigger;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactTrigger;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

public class AttackTrigger extends ArtefactTrigger {

    @Override
    public boolean check(ArtefactContext context, ArtefactNode node) {
        Object[] args = context.context();
        if (args.length == 0) return false;
        if (args[0] instanceof String eventType) {
            return "attack".equals(eventType) || "left_click_block".equals(eventType);
        }
        return false;
    }
}