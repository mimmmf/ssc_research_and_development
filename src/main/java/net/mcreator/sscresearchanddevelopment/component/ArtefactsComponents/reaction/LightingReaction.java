package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.reaction;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactReaction;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class LightingReaction extends ArtefactReaction {
    @Override
    public void execute(ArtefactContext context, ArtefactNode node) {
        Level level = context.level();
        BlockPos pos = context.pos();

        // Создаем сущность молнии
        EntityType.LIGHTNING_BOLT.spawn((ServerLevel) level, pos, EntitySpawnReason.EVENT);
    }
}