package net.mcreator.sscresearchanddevelopment.client.screens;
import net.mcreator.sscresearchanddevelopment.client.gui.FlatButton;
import net.mcreator.sscresearchanddevelopment.interfaces.IScreenHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class CommunicationConsoleScreen extends Screen implements IScreenHelper {
    private static final ResourceLocation BG_TEXTURE = ResourceLocation.parse("ssc_research_and_development:textures/screens/communication_console_gui.png");

    private static final int COLOR_BTN_NORMAL = 0x003D3F56;
    private static final int COLOR_BTN_HOVER = 0x88633B00;
    private int leftPos, topPos;
    private boolean isThreatCategoryExpanded = false;

    private FlatButton btnAnnouncement;
    private FlatButton btnThreatCategory;
    private FlatButton btnShuttle;
    private final List<FlatButton> subButtons = new ArrayList<>();

    private MultiLineEditBox inputAnnouncement;

    public CommunicationConsoleScreen() {
        super(Component.literal("Консоль связи"));
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - 120) / 2;
        this.topPos = (this.height - 160) / 2;
        this.subButtons.clear();

        this.inputAnnouncement = MultiLineEditBox.builder().setShowDecorations(false).build(this.font, 108, 72, Component.empty());
        this.inputAnnouncement.setCharacterLimit(120);
        this.inputAnnouncement.setPosition(leftPos + 6, topPos + 34);
        this.addRenderableWidget(this.inputAnnouncement);

        this.btnAnnouncement = this.addRenderableWidget(new FlatButton(
                leftPos + 5, topPos + 109, 110, 14, "Сделать объявление", 0.75f,
                COLOR_BTN_NORMAL, COLOR_BTN_HOVER, font, b -> {
            this.sendAnnouncement();
        }
        ));

        this.btnThreatCategory = this.addRenderableWidget(new FlatButton(
                leftPos + 5, topPos + 125, 110, 14, "Сменить код угрозы...", 0.75f,
                COLOR_BTN_NORMAL, COLOR_BTN_HOVER, font, b -> {
            this.isThreatCategoryExpanded = !this.isThreatCategoryExpanded;
            this.updateLayout();
        }
        ));

        FlatButton subGreen = new FlatButton(leftPos + 10, topPos + 120, 98, 11, "• Зелёный код", 0.65f, 0xFF1B5E20, 0xFF2E7D32, font, b -> {});
        FlatButton subBlue  = new FlatButton(leftPos + 10, topPos + 133, 98, 11, "• Синий код", 0.65f, 0xFF0D47A1, 0xFF1565C0, font, b -> {});
        FlatButton subPurple   = new FlatButton(leftPos + 10, topPos + 146, 98, 11, "• Фиолетовый код", 0.65f, 0xFF9C28CA, 0xFFb755dd, font, b -> {});
        FlatButton subYellow   = new FlatButton(leftPos + 10, topPos + 146, 98, 11, "• Желтый код", 0.65f, 0xFFC6B628, 0xFFe1d56b, font, b -> {});
        FlatButton subRed   = new FlatButton(leftPos + 10, topPos + 146, 98, 11, "• Красный код", 0.65f, 0xFFB71C1C, 0xFFC62828, font, b -> {});

        this.subButtons.add(this.addRenderableWidget(subGreen));
        this.subButtons.add(this.addRenderableWidget(subBlue));
        this.subButtons.add(this.addRenderableWidget(subPurple));
        this.subButtons.add(this.addRenderableWidget(subYellow));
        this.subButtons.add(this.addRenderableWidget(subRed));

        this.btnShuttle = this.addRenderableWidget(new FlatButton(
                leftPos + 5, topPos + 120, 110, 14, "Вызвать шаттл эвакуации", 0.75f,
                COLOR_BTN_NORMAL, COLOR_BTN_HOVER, font, b -> {}
        ));

        this.updateLayout();
    }

    private void sendAnnouncement() {
        String text = this.inputAnnouncement.getValue().trim();
        if (!text.isEmpty() && Minecraft.getInstance().player != null) {
            text = text.replace("\n", " ");

            String formattedMessage = "[Консоль Связи] " + text;
            Minecraft.getInstance().player.connection.sendChat(formattedMessage);

            this.inputAnnouncement.setValue("");
            this.onClose();
        }
    }

    private void updateLayout() {
        int currentY = this.btnThreatCategory.getY() + this.btnThreatCategory.getHeight();

        if (this.isThreatCategoryExpanded) {
            for (FlatButton subBtn : this.subButtons) {
                subBtn.visible = true;
                subBtn.setY(currentY);
                currentY += subBtn.getHeight();
            }
        } else {
            for (FlatButton subBtn : this.subButtons) {
                subBtn.visible = false;
            }
        }

        this.btnShuttle.setY(currentY + 2);
        this.btnShuttle.visible = !this.isThreatCategoryExpanded;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.blit(RenderPipelines.GUI_TEXTURED, BG_TEXTURE, this.leftPos, this.topPos, 0, 0, 120, 160, 120, 160);

        renderScaledText(font, g, "Консоль связи", leftPos + 21, topPos + 5, 0.9f, 0xFFFFFFFF, false);
        renderScaledText(font, g, "Объявление:", leftPos + 8, topPos + 24, 0.75f, 0xFFFFFFFF, false);

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}