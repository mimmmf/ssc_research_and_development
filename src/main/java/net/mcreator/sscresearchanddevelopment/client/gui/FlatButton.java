package net.mcreator.sscresearchanddevelopment.client.gui;

import net.mcreator.sscresearchanddevelopment.interfaces.IScreenHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class FlatButton extends Button implements IScreenHelper {
    private final String label;
    private final float scale;
    private final int normalColor;
    private final int hoverColor;

    public FlatButton(int x, int y, int width, int height, String label, float scale, int normalColor, int hoverColor, OnPress press) {
        super(x, y, width, height, Component.empty(), press, DEFAULT_NARRATION);
        this.label = label;
        this.scale = scale;
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
    }

    @Override
    public void renderWidget(GuiGraphics g, int mx, int my, float pt) {
        int color = isHovered() ? hoverColor : normalColor;
        g.fill(getX(), getY(), getX() + width, getY() + height, color);
        renderScaledText(g, label, getX() + width / 2f, getY() + (height - 8 * scale) / 2f, scale, active ? 0xFFFFFFFF : 0xFFA0A0A0, true);
    }
}