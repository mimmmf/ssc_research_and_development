/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.sscresearchanddevelopment.init;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.mcreator.sscresearchanddevelopment.client.gui.ArtefactConsoleGUIScreen;

@EventBusSubscriber(Dist.CLIENT)
public class SscResearchAndDevelopmentModScreens {
	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(SscResearchAndDevelopmentModMenus.ARTEFACT_CONSOLE_GUI.get(), ArtefactConsoleGUIScreen::new);
	}

	public interface ScreenAccessor {
		void updateMenuState(int elementType, String name, Object elementState);
	}
}