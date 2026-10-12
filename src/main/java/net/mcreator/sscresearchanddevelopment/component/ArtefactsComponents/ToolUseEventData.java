package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ToolUseEventData(Player player, ItemStack usedTool) {}