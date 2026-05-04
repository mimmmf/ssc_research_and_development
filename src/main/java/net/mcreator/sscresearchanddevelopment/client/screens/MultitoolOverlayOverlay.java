package net.mcreator.sscresearchanddevelopment.client.screens;

import net.mcreator.sscresearchanddevelopment.component.MultitoolComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.units.qual.h;

import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

import net.mcreator.sscresearchanddevelopment.procedures.MultitoolOverlayDisplayOverlayIngameProcedure;

import java.awt.*;

import static net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents.MULTITOOL_DATA;

@EventBusSubscriber(Dist.CLIENT)
public class MultitoolOverlayOverlay {
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void eventHandler(RenderGuiEvent.Pre event) {
		int w = event.getGuiGraphics().guiWidth();
		int h = event.getGuiGraphics().guiHeight();
		Level world = null;
		double x = 0;
		double y = 0;
		double z = 0;
		Player entity = Minecraft.getInstance().player;
		if (entity != null) {
			world = entity.level();
			x = entity.getX();
			y = entity.getY();
			z = entity.getZ();
		}
		if (MultitoolOverlayDisplayOverlayIngameProcedure.execute(entity)) {
			event.getGuiGraphics().drawString(Minecraft.getInstance().font, Component.translatable("gui.ssc_research_and_development.multitool_overlay.label_riezhim"), w / 2 + -29, h / 2 + 47, -1, false);
			ItemStack stack = entity.getItemInHand(InteractionHand.MAIN_HAND);
			MultitoolComponent comp = stack.getOrDefault(MULTITOOL_DATA.get(), new MultitoolComponent(false));
			if(comp.isListType()) {
				event.getGuiGraphics().drawString(Minecraft.getInstance().font, "Список", w / 2 + 5, h / 2 + 48, new Color(34, 255, 0).getRGB(), false);
			}
			else {
				event.getGuiGraphics().drawString(Minecraft.getInstance().font, "Связь", w / 2 + 5, h / 2 + 48, new Color(231, 0, 0).getRGB(), false);
			}

		}
	}
}