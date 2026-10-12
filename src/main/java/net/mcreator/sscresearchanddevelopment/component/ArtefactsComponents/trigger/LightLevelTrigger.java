package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.trigger;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactTrigger;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class LightLevelTrigger extends ArtefactTrigger {

    @Override
    public boolean check(ArtefactContext context, ArtefactNode node) {
        Level level = context.level();
        BlockPos pos = context.pos();
        if (level == null || pos == null) return false;
        int lightLevel = level.getMaxLocalRawBrightness(pos);
        return lightLevel == 0;
    }
}