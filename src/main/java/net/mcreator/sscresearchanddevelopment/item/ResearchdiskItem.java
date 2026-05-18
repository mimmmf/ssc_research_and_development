package net.mcreator.sscresearchanddevelopment.item;

import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class ResearchdiskItem extends Item {
	public ResearchdiskItem(Item.Properties properties) {
		super(properties.rarity(Rarity.UNCOMMON).stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		int score = stack.getOrDefault(SscResearchAndDevelopmentDataComponents.SCORE.get(), 0);

		if (score == Integer.MAX_VALUE) {
			tooltipComponents.accept(Component.literal("Очки: МАКСИМУМ"));
		} else {
			tooltipComponents.accept(Component.literal("Очки: " + score));
		}
		super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
	}
}