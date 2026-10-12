package net.mcreator.sscresearchanddevelopment.interfaces;

import net.mcreator.sscresearchanddevelopment.enums.ToolType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface IInteractable {
    boolean onToolInteract(LevelAccessor world, BlockPos pos, Player player, ToolType toolType, ItemStack heldItem, int elapsedTicks);
    int getDurationTicks(BlockState state, ToolType toolType, ItemStack heldItem);
}
