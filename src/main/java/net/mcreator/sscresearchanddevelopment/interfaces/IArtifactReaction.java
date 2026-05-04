package net.mcreator.sscresearchanddevelopment.interfaces;

import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactanalyzerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface IArtifactReaction {
    // Выполнение реакции. Сила реакции вычисляется как:
    // Power = BaseValue * (1 + depth * multiplier)
    void execute(ServerLevel level, BlockPos pos, int depth, ArtefactanalyzerBlockEntity be);
}
