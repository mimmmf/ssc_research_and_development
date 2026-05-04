package net.mcreator.sscresearchanddevelopment.procedures;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModItems;

public class ArtefactanalyzerOnClickProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		boolean found = false;
		double sx = 0;
		double sy = 0;
		double sz = 0;
		ItemStack temp_item = ItemStack.EMPTY;
		ItemStack sub_item = ItemStack.EMPTY;
		String entity_name = "";
		temp_item = (itemFromBlockInventory(world, BlockPos.containing(x, y, z), 0).copy()).copy();
		if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SscResearchAndDevelopmentModItems.ARTEFACT.get() && temp_item.getItem() == Blocks.AIR.asItem()) {
			if (world instanceof ILevelExtension _ext && _ext.getCapability(Capabilities.ItemHandler.BLOCK, BlockPos.containing(x, y, z), null) instanceof IItemHandlerModifiable _itemHandlerModifiable) {
				ItemStack _setstack = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).copy();
				_setstack.setCount(1);
				_itemHandlerModifiable.setStackInSlot(0, _setstack);
			}
			if (entity instanceof LivingEntity _entity) {
				ItemStack _setstack6 = new ItemStack(Blocks.AIR).copy();
				_setstack6.setCount(1);
				_entity.setItemInHand(InteractionHand.MAIN_HAND, _setstack6);
				if (_entity instanceof Player _player)
					_player.getInventory().setChanged();
			}
			if (world instanceof ServerLevel _level)
				_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
						"summon item_display ~0.5 ~0.5 ~0.5 {Tags:[\"analyzer_display\"]}");
			if (world instanceof ServerLevel _level)
				_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
						"execute as @e[tag=analyzer_display, limit=1, sort=nearest] run item replace entity @s container.0 from block ~ ~ ~ container.0");
			if (world instanceof ServerLevel _level)
				_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
						"execute as @e[tag=analyzer_display, limit=1, sort=nearest] run data merge entity @s {item_display:\"ground\", transformation:{scale:[1f, 1f, 1f]}}");
		} else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Blocks.AIR.asItem() && temp_item.getItem() == SscResearchAndDevelopmentModItems.ARTEFACT.get()) {
			if (entity instanceof LivingEntity _entity) {
				ItemStack _setstack13 = temp_item.copy();
				_setstack13.setCount(1);
				_entity.setItemInHand(InteractionHand.MAIN_HAND, _setstack13);
				if (_entity instanceof Player _player)
					_player.getInventory().setChanged();
			}
			if (world instanceof ILevelExtension _ext && _ext.getCapability(Capabilities.ItemHandler.BLOCK, BlockPos.containing(x, y, z), null) instanceof IItemHandlerModifiable _itemHandlerModifiable) {
				ItemStack _setstack = new ItemStack(Blocks.AIR).copy();
				_setstack.setCount(1);
				_itemHandlerModifiable.setStackInSlot(0, _setstack);
			}
			if (world instanceof ServerLevel _level)
				_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
						"kill @e[type=item_display, tag=analyzer_display, distance=..2]");
			if (world instanceof ServerLevel _level)
				_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(), "tell @a sus");
		} else if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SscResearchAndDevelopmentModItems.ARTEFACT.get() && temp_item.getItem() == SscResearchAndDevelopmentModItems.ARTEFACT.get()) {
			sub_item = temp_item.copy();
			if (world instanceof ILevelExtension _ext && _ext.getCapability(Capabilities.ItemHandler.BLOCK, BlockPos.containing(x, y, z), null) instanceof IItemHandlerModifiable _itemHandlerModifiable) {
				ItemStack _setstack = (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).copy();
				_setstack.setCount(1);
				_itemHandlerModifiable.setStackInSlot(0, _setstack);
			}
			if (entity instanceof LivingEntity _entity) {
				ItemStack _setstack22 = sub_item.copy();
				_setstack22.setCount(1);
				_entity.setItemInHand(InteractionHand.MAIN_HAND, _setstack22);
				if (_entity instanceof Player _player)
					_player.getInventory().setChanged();
			}
			if (world instanceof ServerLevel _level)
				_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
						"kill @e[type=item_display, tag=analyzer_display, distance=..2]");
			if (world instanceof ServerLevel _level)
				_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
						"summon item_display ~0.5 ~0.5 ~0.5 {Tags:[\"analyzer_display\"]}");
			if (world instanceof ServerLevel _level)
				_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
						"execute as @e[tag=analyzer_display, limit=1, sort=nearest] run item replace entity @s container.0 from block ~ ~ ~ container.0");
			if (world instanceof ServerLevel _level)
				_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
						"execute as @e[tag=analyzer_display, limit=1, sort=nearest] run data merge entity @s {item_display:\"ground\", transformation:{scale:[1f, 1f, 1f]}}");
		}
	}

	private static ItemStack itemFromBlockInventory(LevelAccessor world, BlockPos pos, int slot) {
		if (world instanceof ILevelExtension ext) {
			IItemHandler itemHandler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
			if (itemHandler != null)
				return itemHandler.getStackInSlot(slot);
		}
		return ItemStack.EMPTY;
	}
}