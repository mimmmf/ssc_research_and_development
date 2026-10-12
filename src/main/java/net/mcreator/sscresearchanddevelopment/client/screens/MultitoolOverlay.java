package net.mcreator.sscresearchanddevelopment.client.screens;

import net.mcreator.sscresearchanddevelopment.component.MultitoolComponent;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

import static net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents.MULTITOOL_DATA;

public class MultitoolOverlay {
	public static void render(GuiGraphics graphics, int w, int h, Player player) {
		if (execute(player)) {
			graphics.drawString(Minecraft.getInstance().font, Component.translatable("gui.ssc_research_and_development.multitool_overlay.label_riezhim"), w / 2 - 29, h / 2 + 47, -1, false);

			ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
			MultitoolComponent comp = stack.getOrDefault(MULTITOOL_DATA.get(), new MultitoolComponent(false));

			if (comp.isListType()) {
				graphics.drawString(Minecraft.getInstance().font, "Список", w / 2 + 5, h / 2 + 48, 0xFF34FF00, false);
			} else {
				graphics.drawString(Minecraft.getInstance().font, "Связь", w / 2 + 5, h / 2 + 48, 0xFFE70000, false);
			}
		}
	}
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		return (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == SscResearchAndDevelopmentModItems.MULTITOOL.get()
				|| (entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == SscResearchAndDevelopmentModItems.MULTITOOL.get();
	}
}