package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.trigger;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactTrigger;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;

public class NearbyDeathTrigger extends ArtefactTrigger {

    @Override
    public boolean check(ArtefactContext context, ArtefactNode node) {
        Object[] args = context.context();
        if (args.length == 0) return false;
        return args[0] instanceof String eventType && "nearby_death".equals(eventType);
    }
}