package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents;

import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public record ArtefactContext (Level level, BlockPos pos, ItemStack stack, Object[] context, @Nullable Entity source) {
    public interface IArtefactTrigger {
        // Проверка, сработал ли триггер (например, на тике или при ударе)
        boolean check(ArtefactContext context, ArtefactNode node, Object... args);
    }

    public interface IArtefactReaction {
        // Выполнение действия
        void execute(ArtefactContext context, ArtefactNode node);
    }
}
