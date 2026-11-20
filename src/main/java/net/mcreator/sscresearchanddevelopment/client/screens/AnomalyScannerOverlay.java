package net.mcreator.sscresearchanddevelopment.client.screens;

import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModItems;
import net.mcreator.sscresearchanddevelopment.interfaces.IScreenHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class AnomalyScannerOverlay implements IScreenHelper {
    public static final AnomalyScannerOverlay INSTANCE = new AnomalyScannerOverlay();
    public static void render(GuiGraphics g, int w, int h, Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        int hand = execute(player);
        if (hand == -1) return;

        ResourceLocation currentTexture = ResourceLocation.fromNamespaceAndPath(
                "ssc_research_and_development",
                "textures/screens/anomaly_scanner_gui.png"
        );

        int textureWidth = 153;
        int textureHeight = 184;

        int x;
        int y = h / 2 - (textureHeight / 2);

        if (hand == 1) {
            x = (int)(0.025f * w);
        } else {
            x = (int)(0.975f * w - textureWidth);
        }

        g.blit(RenderPipelines.GUI_TEXTURED, currentTexture, x, y, 0, 0,
                textureWidth, textureHeight, textureWidth, textureHeight);

        INSTANCE.renderScaledText(g, Component.translatable("gui.ssc_research_and_development.anomaly_scanner_gui.title").getString(), x + 8, y + 10, 1f, 0xffe1aeff, false);
        INSTANCE.renderScaledText(g, Component.translatable("gui.ssc_research_and_development.anomaly_scanner_gui.subtitle").getString(), x + 106, y + 10, 1f, 0xffb0a1f0, false);

        INSTANCE.renderScaledText(g, Component.translatable("gui.ssc_research_and_development.anomaly_scanner_gui.current_severity").getString(), x + 8, y + 27, 0.7f, 0xffb0a1f0, false);
        INSTANCE.renderScaledText(g, "41.312%", x + 110, y + 27, 0.8f, 0xff713197, false);

        INSTANCE.renderScaledText(g, Component.translatable("gui.ssc_research_and_development.anomaly_scanner_gui.current_anomaly_state").getString(), x + 8, y + 45, 0.7f, 0xffb0a1f0, false);
        INSTANCE.renderScaledText(g, Component.literal("Stable").getString(), x + 10, y + 52, 0.7f, 0xff34f039, false);

        INSTANCE.renderScaledText(g, Component.translatable("gui.ssc_research_and_development.anomaly_scanner_gui.point_output").getString(), x + 8, y + 60, 0.7f, 0xffb0a1f0, false);
        INSTANCE.renderScaledText(g, Component.literal("44").getString(), x + 56, y + 60, 0.75f, 0xff713197, false);

        INSTANCE.renderScaledText(g, Component.translatable("gui.ssc_research_and_development.anomaly_scanner_gui.particle_reaction_analysis").getString(), x + 8, y + 72, 0.7f, 0xff8881f0, false);

        INSTANCE.renderScaledText(g, Component.translatable("gui.ssc_research_and_development.anomaly_scanner_gui.danger_type").getString(), x + 20, y + 81, 0.7f, 0xffdc143c, false);
        INSTANCE.renderScaledText(g, Component.literal("ERROR").getString(), x + 68, y + 81, 0.75f, 0xffe30b0b, false);

        INSTANCE.renderScaledText(g, Component.translatable("gui.ssc_research_and_development.anomaly_scanner_gui.unstable_type").getString(), x + 20, y + 93, 0.7f, 0xffb42fcf, false);
        INSTANCE.renderScaledText(g, Component.literal("ERROR").getString(), x + 72, y + 93, 0.75f, 0xffe30b0b, false);

        INSTANCE.renderScaledText(g, Component.translatable("gui.ssc_research_and_development.anomaly_scanner_gui.containment_type").getString(), x + 20, y + 104, 0.7f, 0xffdd9421, false);
        INSTANCE.renderScaledText(g, Component.literal("Delta particle").getString(), x + 82, y + 104, 0.7f, 0xffa899e5, false);

        INSTANCE.renderScaledText(g, Component.translatable("gui.ssc_research_and_development.anomaly_scanner_gui.transformation_type").getString(), x + 19, y + 116, 0.7f, 0xff3d4ef1, false);
        INSTANCE.renderScaledText(g, Component.literal("Epsilon particle").getString(), x + 94, y + 116, 0.7f, 0xffa899e5, false);

        INSTANCE.renderScaledText(g, Component.translatable("gui.ssc_research_and_development.anomaly_scanner_gui.behavior_deviation_analysis").getString(), x + 8, y + 131, 0.7f, 0xff8881f0, false);
        INSTANCE.renderWrappedText(g, Component.literal("- Interference detected. Some data cannot be read").getString(), x + 8, y + 138, 146, 0.7f, 0xffb0a1f0);
        INSTANCE.renderScaledText(g, Component.translatable("gui.ssc_research_and_development.anomaly_scanner_gui.anomaly_produces_percent").getString(), x + 8, y + 152, 0.7f, 0xffdd9421, false);

        INSTANCE.renderScaledText(g, Component.translatable("gui.ssc_research_and_development.anomaly_scanner_gui.time_until_next_pulse").getString(), x + 8, y + 170, 0.7f, 0xff8881f0, false);
        INSTANCE.renderScaledText(g, Component.literal("00:45").getString(), x + 123, y + 169, 0.95f, 0xff713197, false);
    }

    public static int execute(Entity entity) {
        if (entity == null)
            return -1;
        if (entity instanceof LivingEntity living) {
            if (living.getMainHandItem().getItem() == SscResearchAndDevelopmentModItems.ANOMALY_SCANNER.get()) {
                return 1;
            }
            if (living.getOffhandItem().getItem() == SscResearchAndDevelopmentModItems.ANOMALY_SCANNER.get()) {
                return 0;
            }
        }
        return -1;
    }
}