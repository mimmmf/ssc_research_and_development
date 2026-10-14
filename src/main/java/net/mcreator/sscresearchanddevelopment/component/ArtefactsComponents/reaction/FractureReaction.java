package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.reaction;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactReaction;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class FractureReaction extends ArtefactReaction {
    public FractureReaction() {}

    @Override
    public void execute(ArtefactContext ctx, ArtefactNode node) {
        Level level = ctx.level();
        if (level.isClientSide) return;

        BlockPos centerPos = ctx.pos();

        int dx = level.random.nextInt(3) - 1;
        int dy = level.random.nextInt(3) - 1;
        int dz = level.random.nextInt(3) - 1;

        BlockPos targetPos = centerPos.offset(dx, dy, dz);

        if (level.isEmptyBlock(targetPos)) {
            Block portalBlock = BuiltInRegistries.BLOCK.get(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ssc14_dlc", "carp_portal")
            ).get().value();

            if (portalBlock != null && portalBlock != Blocks.AIR) {
                level.setBlock(targetPos, portalBlock.defaultBlockState(), 3);

                level.playSound(null, targetPos, SscResearchAndDevelopmentSounds.TELEPORT_ARRIVAL.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
            }
        }
    }
}
