package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents;

import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;

public class TimerTrigger extends ArtefactTrigger {
    private float timer = 10f;
    public TimerTrigger(float timer) {
        this.timer = timer;
    }
    @Override
    public boolean check(ArtefactContext context, ArtefactNode node) {
        return context.level().getGameTime() % this.timer == 0;
    }
}
