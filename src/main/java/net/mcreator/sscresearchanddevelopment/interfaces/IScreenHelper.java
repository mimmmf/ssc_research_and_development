package net.mcreator.sscresearchanddevelopment.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


public interface IScreenHelper {
    default Font getFont() {
        return Minecraft.getInstance().font;
    }
    default void renderScaledText(GuiGraphics g, String text, float x, float y, float scale, int color, boolean centered) {
        Font font = getFont();
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        if (centered) {
            g.drawCenteredString(font, text, 0, 0, color);
        } else {
            g.drawString(font, text, 0, 0, color, false);
        }
        g.pose().popMatrix();
    }
    default void renderScaledIcon(ResourceLocation texture, GuiGraphics g, int x, int y, float scale, int width, int height) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0, 0, width, height, width, height);
        g.pose().popMatrix();
    }
    default int renderWrappedText(GuiGraphics g, String text, int x, int y, int maxWidth, float scale, int color) {
        Font font = getFont();
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);

        var lines = font.split(Component.literal(text), (int) (maxWidth / scale));
        int lineY = 0;
        for (var line : lines) {
            g.drawString(font, line, 0, lineY, color, false);
            lineY += font.lineHeight;
        }

        g.pose().popMatrix();
        return (int) (lineY * scale);
    }
}
