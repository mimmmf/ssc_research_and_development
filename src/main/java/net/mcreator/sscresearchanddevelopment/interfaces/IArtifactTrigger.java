package net.mcreator.sscresearchanddevelopment.interfaces;

import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactanalyzerBlockEntity;
import net.mcreator.sscresearchanddevelopment.component.ArtifactProperties;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IArtifactTrigger {
    // Проверка: можно ли продолжать прогресс в этом тике?
    boolean isConditionMet(Level level, BlockPos pos, ArtefactanalyzerBlockEntity be, ArtefactNode node);

    // Что делать, когда прогресс завершен
    void onComplete(Level level, BlockPos pos, ArtefactanalyzerBlockEntity be);
}
