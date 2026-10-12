package net.mcreator.sscresearchanddevelopment.init;

import com.mojang.blaze3d.platform.Window;
import net.mcreator.sscresearchanddevelopment.client.screens.MultitoolOverlay;
import net.mcreator.sscresearchanddevelopment.client.screens.ProgressBarOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = "ssc_research_and_development")
public class SscResearchAndDevelopmentModOverlaysHandler {
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onRenderGui(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Window window = mc.getWindow();
        int w = window.getGuiScaledWidth();
        int h = window.getGuiScaledHeight();

        GuiGraphics graphics = event.getGuiGraphics();

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        MultitoolOverlay.render(graphics, w, h, player);
        ProgressBarOverlay.render(graphics, w, h);
    }
}
