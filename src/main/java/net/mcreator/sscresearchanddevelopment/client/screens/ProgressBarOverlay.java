package net.mcreator.sscresearchanddevelopment.client.screens;

import net.mcreator.sscresearchanddevelopment.client.ClientProgressHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public class ProgressBarOverlay {

    public static void render(GuiGraphics guiGraphics, int width, int height) {
        int frame = ClientProgressHolder.getFrame();
        if (frame < 1 || frame > 6) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        ResourceLocation currentTexture = ResourceLocation.fromNamespaceAndPath(
                "ssc_research_and_development",
                "textures/ui/progress_bar_" + frame + ".png"
        );

        int textureWidth = 32;
        int textureHeight = 32;

        int x = width / 2 - (textureWidth / 2);
        int y = guiGraphics.guiHeight() - 120;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, currentTexture, x, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
    }
}