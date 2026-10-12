package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.trigger;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactTrigger;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

public class ImpactOrDropTrigger extends ArtefactTrigger {
    @Override
    public boolean check(ArtefactContext context, ArtefactNode node) {
        Object[] args = context.context();
        if (args.length == 0) return false;

        if (args[0] instanceof String eventType) {
            if ("item_dropped".equals(eventType)) {
                return true;
            }
            if ("take_damage".equals(eventType) && args.length >= 2) {
                if (args[1] instanceof DamageSource source) {
                    return source.is(DamageTypes.FALL);
                }
            }
        }

        return false;
    }
}
