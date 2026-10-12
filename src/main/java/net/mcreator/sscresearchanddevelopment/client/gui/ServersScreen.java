package net.mcreator.sscresearchanddevelopment.client.gui;

import net.mcreator.sscresearchanddevelopment.network.C2SConnectServer;
import net.mcreator.sscresearchanddevelopment.network.S2CUpdateServerList;
import net.mcreator.sscresearchanddevelopment.client.gui.misc.FlatButton;
import net.mcreator.sscresearchanddevelopment.interfaces.IScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServersScreen extends Screen implements IScreenHelper {
    private static final ResourceLocation BG_TEXTURE = ResourceLocation.parse("ssc_research_and_development:textures/screens/research_server_gui.png");

    private static final int COLOR_BTN_NORMAL = 0xFF3D3F56;
    private static final int COLOR_BTN_HOVER = 0xFF633B00;

    private final BlockPos consolePos;
    private final List<S2CUpdateServerList.ServerInfo> servers = new ArrayList<>();
    private UUID selectedId = null;
    private UUID connectedId = null;
    private int leftPos, topPos;
    private Button selectButton;

    public ServersScreen(BlockPos pos, List<S2CUpdateServerList.ServerInfo> servers, UUID connectedServer) {
        super(Component.translatable("gui.ssc_research_and_development.servers_gui.title"));
        this.consolePos = pos;
        this.servers.addAll(servers);
        this.connectedId = C2SConnectServer.EMPTY.equals(connectedServer) ? null : connectedServer;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - 240) / 2;
        this.topPos = (this.height - 160) / 2;

        this.selectButton = this.addRenderableWidget(new FlatButton(
                leftPos + 70, topPos + 138, 100, 12,
                Component.translatable("gui.ssc_research_and_development.servers_gui.connect").getString(),
                0.8f, COLOR_BTN_NORMAL, COLOR_BTN_HOVER, font, b -> {
            if (selectedId != null) {
                ClientPacketDistributor.sendToServer(new C2SConnectServer(consolePos, selectedId));
            }
            this.onClose();
        }
        ));

        this.selectButton.active = false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.blit(RenderPipelines.GUI_TEXTURED, BG_TEXTURE, this.leftPos + 60, this.topPos - 1, 0, 0, 120, 160, 120, 160);

        String titleText = Component.translatable("gui.ssc_research_and_development.servers_gui.title").getString();
        renderScaledText(font, g, titleText, leftPos + 120, topPos + 7, 1.0f, 0xFF55FFFF, true);

        int startX = listStartX();
        int startY = listStartY();

        for (int i = 0; i < servers.size(); i++) {
            S2CUpdateServerList.ServerInfo info = servers.get(i);
            int itemY = startY + (i * 15);
            boolean isHovered = mx >= startX && mx <= startX + 160 && my >= itemY && my <= itemY + 14;
            boolean isSelected = info.id().equals(selectedId);
            boolean isConnected = info.id().equals(connectedId);

            int bgColor = isConnected ? 0xAA55FF55 : (isSelected ? 0x6655FF55 : (isHovered ? 0x33FFFFFF : 0));
            if (bgColor != 0) g.fill(startX, itemY, startX + 104, itemY + 14, bgColor);

            if (isConnected) {
                g.drawString(font, "✔", startX + 2, itemY + 3, 0xFF00FF00, false);
            }

            renderScaledText(font, g, info.name(), startX + 30, itemY + 4, 0.8f, isSelected ? 0xFFFFFF00 : 0xFFFFFFFF, true);

            String pointsText = Component.translatable("gui.ssc_research_and_development.servers_gui.points", info.points()).getString();
            renderScaledText(font, g, pointsText, startX + 80, itemY + 4, 0.8f, 0xFFAAAAAA, true);
        }

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int startX = listStartX();
        int startY = listStartY();

        for (int i = 0; i < servers.size(); i++) {
            int itemY = startY + (i * 15);
            if (mx >= startX && mx <= startX + 160 && my >= itemY && my <= itemY + 14) {
                this.selectedId = servers.get(i).id();
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                if (selectButton != null) selectButton.active = true;
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override public boolean isPauseScreen() { return false; }
    private int listStartX() { return leftPos + 68; }
    private int listStartY() { return topPos + 22; }
}