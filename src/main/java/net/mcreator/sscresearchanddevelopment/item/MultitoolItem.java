package net.mcreator.sscresearchanddevelopment.item;

import net.mcreator.sscresearchanddevelopment.component.LinkedBlockComponent;
import net.mcreator.sscresearchanddevelopment.component.MultitoolComponent;
import net.mcreator.sscresearchanddevelopment.interfaces.ILinkable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents.*;

public class MultitoolItem extends Item {
	public MultitoolItem(Item.Properties properties) {
		super(properties);
	}
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		ItemStack stack = context.getItemInHand();

		if (world.getBlockEntity(pos) instanceof ILinkable linkable) {
			if (!world.isClientSide) {
				boolean isListMode = stack.getOrDefault(MULTITOOL_DATA.get(), new MultitoolComponent(false)).isListType();
				GlobalPos currentTarget = GlobalPos.of(world.dimension(), pos);
				List<GlobalPos> links = new ArrayList<>(stack.getOrDefault(LINKED_BLOCKS.get(), List.of()));

				if(isListMode) {
					if (links.contains(currentTarget)) {
						if (linkable.tryUnlink(player, stack)) {
							links.remove(currentTarget);
							stack.set(LINKED_BLOCKS.get(), links);
							player.displayClientMessage(Component.literal("Связь разорвана!").withStyle(ChatFormatting.RED), true);
						}
					} else {
						if (linkable.tryLink(player, stack)) {
							links.add(currentTarget);
							stack.set(LINKED_BLOCKS.get(), links);
							player.displayClientMessage(Component.literal("Связь установлена!").withStyle(ChatFormatting.GREEN), true);
						}
					}
				}
				else {
					//Соединение
					handleSingleLinkMode(context, world, pos, player, stack);
				}

			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide && player.isShiftKeyDown()) {
			MultitoolComponent old = stack.getOrDefault(MULTITOOL_DATA.get(), new MultitoolComponent(false));
			stack.set(MULTITOOL_DATA.get(), new MultitoolComponent(!old.isListType()));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
		List<GlobalPos> links = stack.get(LINKED_BLOCKS.get());
		if (links != null && !links.isEmpty()) {
			tooltip.accept(Component.literal("Связано с блоками: " + links.size()).withStyle(ChatFormatting.GOLD));
		}
	}
	private InteractionResult handleSingleLinkMode(UseOnContext context, Level world, BlockPos pos, Player player, ItemStack stack) {
		var linkedData = stack.getOrDefault(LINKED_BLOCK.get(), new LinkedBlockComponent(Optional.empty()));

		// Шаг 1: Выбор первого блока
		if (linkedData.linked_block().isEmpty()) {
			if (world.getBlockEntity(pos) instanceof ILinkable) {
				stack.set(LINKED_BLOCK.get(), new LinkedBlockComponent(Optional.of(pos)));
				player.displayClientMessage(Component.literal("Блок зафиксирован: " + pos.toShortString()).withStyle(ChatFormatting.AQUA), true);
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		BlockPos firstPos = linkedData.linked_block().get();
		BlockPos secondPos = pos;

		// Сброс, если кликнули по тому же блоку
		if (firstPos.equals(secondPos)) {
			if (world.getBlockEntity(secondPos) instanceof ILinkable linkable) {
				linkable.tryUnlink(player, stack);
			}
			stack.set(LINKED_BLOCK.get(), new LinkedBlockComponent(Optional.empty()));
			player.displayClientMessage(Component.literal("Выбор сброшен").withStyle(ChatFormatting.YELLOW), true);
			return InteractionResult.SUCCESS;
		}

		ILinkable firstBE = world.getBlockEntity(firstPos) instanceof ILinkable lb ? lb : null;
		ILinkable secondBE = world.getBlockEntity(secondPos) instanceof ILinkable lb ? lb : null;

		// КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: Если первый блок был удален из мира
		if (firstBE == null) {
			player.displayClientMessage(Component.literal("Предыдущий блок более недоступен!").withStyle(ChatFormatting.RED), true);
			stack.set(LINKED_BLOCK.get(), new LinkedBlockComponent(Optional.empty()));
			return InteractionResult.FAIL;
		}

		// Если второй блок (на который кликаем сейчас) не линкуемый
		if (secondBE == null) {
			return InteractionResult.PASS;
		}

		// Логика поиска цели (Приемника)
		ILinkable target = null;
		BlockPos sourceCoords = null;

		if (secondBE.canStoreLink()) {
			target = secondBE;
			sourceCoords = firstPos;
		} else if (firstBE.canStoreLink()) {
			target = firstBE;
			sourceCoords = secondPos;
		}

		if (target != null) {
			// Проверяем, не та же ли это самая связь
			if (target.getLinkedPos().isPresent() && target.getLinkedPos().get().equals(sourceCoords)) {
				if (target.tryUnlink(player, stack)) {
					player.displayClientMessage(Component.literal("Связь разорвана!").withStyle(ChatFormatting.RED), true);
				}
			}
			else {
				// Переподключение: сначала очищаем старое, потом ставим новое
				if (target.getLinkedPos().isPresent()) {
					target.tryUnlink(player, stack);
				}

				if (target.tryLink(player, stack)) {
					target.setLinkedPos(sourceCoords);
					player.displayClientMessage(Component.literal("Связь установлена!").withStyle(ChatFormatting.GREEN), true);
				}
			}

			stack.set(LINKED_BLOCK.get(), new LinkedBlockComponent(Optional.empty()));
			return InteractionResult.SUCCESS;
		}

		// Если оба блока — источники и никто не может хранить связь
		player.displayClientMessage(Component.literal("Эти блоки не могут быть соединены").withStyle(ChatFormatting.GRAY), true);
		stack.set(LINKED_BLOCK.get(), new LinkedBlockComponent(Optional.empty()));
		return InteractionResult.FAIL;
	}
}