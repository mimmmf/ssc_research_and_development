package net.mcreator.sscresearchanddevelopment.interfaces;

import net.mcreator.sscresearchanddevelopment.component.subcomponents.InteractionStage;
import net.mcreator.sscresearchanddevelopment.enums.ToolType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.List;

public interface IMultiStageBlock extends IInteractable {
    List<InteractionStage> getDeconstructionStages();
    List<InteractionStage> getConstructionStages();
    InteractionStage getCurrentStage(BlockState state, ToolType tool, ItemStack heldItem);

    @Override
    default int getDurationTicks(BlockState state, ToolType toolType, ItemStack heldItem) {
        InteractionStage stage = getCurrentStage(state, toolType, heldItem);
        return stage != null ? stage.getDurationTicks() : 0;
    }

    @Override
    default boolean onToolInteract(LevelAccessor world, BlockPos pos, Player player, ToolType toolType, ItemStack heldItem, int elapsedTicks) {
        BlockState state = world.getBlockState(pos);
        InteractionStage stage = getCurrentStage(state, toolType, heldItem);

        if (stage == null) return false;

        if (elapsedTicks < stage.getDurationTicks()) {
            return true;
        }

        if (!world.isClientSide()) {
            ItemStack drop = stage.getDropItem();
            if (!drop.isEmpty()) {
                Block.popResource((Level) world, pos, drop);
            }

            if (stage.getRequiredMaterial() != null && !player.isCreative()) {
                heldItem.shrink(1);
            }

            BlockState nextState = stage.getNextState(state);

            if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && nextState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                nextState = nextState.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
            }
            if (state.hasProperty(BlockStateProperties.FACING) && nextState.hasProperty(BlockStateProperties.FACING)) {
                nextState = nextState.setValue(BlockStateProperties.FACING, state.getValue(BlockStateProperties.FACING));
            }

            world.setBlock(pos, nextState, 3);
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }
}
