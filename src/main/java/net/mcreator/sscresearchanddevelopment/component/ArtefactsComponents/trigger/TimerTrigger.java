package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.trigger;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactTrigger;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.util.RandomSource;

public class TimerTrigger extends ArtefactTrigger {
    private float timer = 10f;
    public TimerTrigger(float timer) {
        this.timer = timer;
    }
    @Override
    public boolean check(ArtefactContext context, ArtefactNode node) {
        return context.level().getGameTime() % this.timer == 0;
    }

    @Override
    public boolean canSpawn(int depth, int tier, RandomSource random) {
        return false;
    }
}
