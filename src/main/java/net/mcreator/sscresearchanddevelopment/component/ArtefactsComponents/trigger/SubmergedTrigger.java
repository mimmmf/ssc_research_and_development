package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.trigger;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactTrigger;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;

public class SubmergedTrigger extends ArtefactTrigger {

    @Override
    public boolean check(ArtefactContext context, ArtefactNode node) {
        Object[] args = context.context();
        if (args.length > 0 && "bucket_water".equals(args[0])) {
            return true;
        }
        Level level = context.level();
        BlockPos pos = context.pos();
        Entity entity = context.source();
        if (level == null || pos == null) return false;
        if (entity instanceof Player player) {
            return player.isInWater() || player.isUnderWater();
        }
        if (entity instanceof ItemEntity itemEntity) {
            return itemEntity.isInWater();
        }
        return level.getFluidState(pos).isSourceOfType(Fluids.WATER);
    }
}