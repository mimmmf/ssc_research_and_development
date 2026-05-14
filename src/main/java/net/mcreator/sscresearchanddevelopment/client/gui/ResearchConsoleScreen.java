package net.mcreator.sscresearchanddevelopment.client.gui;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.client.C2SRequestServerList;
import net.mcreator.sscresearchanddevelopment.client.gui.misc.FlatButton;
import net.mcreator.sscresearchanddevelopment.component.fabricator.FabricatorRecipe;
import net.mcreator.sscresearchanddevelopment.component.technologies.C2SRequestConsoleData;
import net.mcreator.sscresearchanddevelopment.component.technologies.C2SUnlockResearch;
import net.mcreator.sscresearchanddevelopment.component.technologies.ResearchNode;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModScreens;
import net.mcreator.sscresearchanddevelopment.interfaces.IScreenHelper;
import net.mcreator.sscresearchanddevelopment.world.inventory.ResearchConsoleMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
public class ResearchConsoleScreen extends AbstractContainerScreen<ResearchConsoleMenu> implements SscResearchAndDevelopmentModScreens.ScreenAccessor, IScreenHelper {
    private static final ResourceLocation BG_TEXTURE = ResourceLocation.parse("ssc_research_and_development:textures/screens/research_console_gui.png");

    private final int vX = -1, vY = 20, vW = 206, vH = 170;
    private final int infoPanelWidth = 72;
    private final int infoPanelHeight = 70;

    private float scrollX = 0, scrollY = 0;
    private float infoScrollY = 0;
    private int totalContentHeight = 0;
    private boolean isDragging = false;

    private UUID connectedServer;
    private int serverPoints = 0;
    private List<String> unlockedTechs = new ArrayList<>();

    private final List<TechButton> techButtons = new ArrayList<>();
    private ResearchNode selectedNode = null;
    private FlatButton serversButton, unlockButton;

    public ResearchConsoleScreen(ResearchConsoleMenu menu, Inventory inv, Component comp) {
        super(menu, inv, comp);
        this.imageWidth = 300;
        this.imageHeight = 200;
        this.connectedServer = menu.getConnectedServer();
    }

    @Override
    protected void init() {
        super.init();
        techButtons.clear();
        selectedNode = null;

        rebuildTechTree();

        this.serversButton = addRenderableWidget(new FlatButton(leftPos + 212, topPos + 180, 70, 10, "Сервер", 0.7f, 0xFF3D3F56, 0xFF633B00, font, b -> {
            ClientPacketDistributor.sendToServer(new C2SRequestServerList(menu.getPos()));
        }));

        this.unlockButton = addRenderableWidget(new FlatButton(leftPos + 212, topPos + 168, 70, 10, "Изучить", 0.7f, 0xFF1D5E1D, 0xFF2A8C2A, font, b -> {
            if (selectedNode != null && connectedServer != null) {
                ClientPacketDistributor.sendToServer(new C2SUnlockResearch(menu.getPos(), selectedNode.getId()));
            }
        }));

        ClientPacketDistributor.sendToServer(new C2SRequestConsoleData(this.menu.getPos()));
    }

    @Override
    public void updateMenuState(int type, String name, Object state) {
        switch (name) {
            case "connectedServer" -> this.connectedServer = (UUID) state;
            case "points" -> this.serverPoints = (int) state;
            case "unlockedTechs" -> this.unlockedTechs = (List<String>) state;
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBg(g, pt, mx, my);
        renderScaledText(font, g, "Консоль исследований", leftPos, topPos + 7, 1f, -26368, false);

        g.enableScissor(leftPos + vX, topPos + vY, leftPos + vX + vW, topPos + vY + vH);
        renderConnections(g);
        techButtons.forEach(btn -> {
            btn.updatePos();
            btn.render(g, mx, my, pt);
        });
        g.disableScissor();

        updateUnlockButtonLogic();

        super.render(g, mx, my, pt);

        renderInfoPanel(g, mx, my);
        renderStatus(g);
        renderInfoItemTooltips(g, mx, my);
        renderTooltip(g, mx, my);
    }

    private void updateUnlockButtonLogic() {
        if (selectedNode == null) {
            unlockButton.visible = false;
            return;
        }

        unlockButton.visible = true;
        boolean isConnected = connectedServer != null;
        boolean isUnlocked = unlockedTechs.contains(selectedNode.getId());
        boolean parentsUnlocked = selectedNode.getParents().stream().allMatch(unlockedTechs::contains);
        boolean canAfford = serverPoints >= selectedNode.getCost();

        unlockButton.active = isConnected && !isUnlocked && parentsUnlocked && canAfford;

        if (!isConnected) {
            unlockButton.setMessage(Component.literal("Нет связи"));
        } else if (isUnlocked) {
            unlockButton.setMessage(Component.literal("Изучено"));
        } else if (!parentsUnlocked) {
            unlockButton.setMessage(Component.literal("Закрыто"));
        } else if (!canAfford) {
            unlockButton.setMessage(Component.literal("Мало ОИ"));
        } else {
            unlockButton.setMessage(Component.literal("Изучить"));
        }
    }

    private void renderStatus(GuiGraphics g) {
        boolean isConnected = connectedServer != null;
        String connText = isConnected ? "Подключён" : "Нет подключения";
        int connColor = isConnected ? 0xFF55FF55 : 0xFFFF5555;
        renderScaledText(font, g, connText, isConnected ? leftPos + 240 : leftPos + 215, topPos + 8, 0.7f, connColor, false);

        if (isConnected) {
            renderScaledText(font, g, "Доступно ОИ: " + serverPoints, leftPos + 2, topPos + 182, 0.8f, 0xFFE6AC00, false);
        }
    }

    private void renderInfoPanel(GuiGraphics g, int mx, int my) {
        if (selectedNode == null) return;

        int panelX = leftPos + 212;
        int panelY = topPos + 95;

        renderScaledIcon(selectedNode.getIcon(), g, panelX + (infoPanelWidth / 2) - 24, panelY - 64, 3, 16, 16);
        g.enableScissor(panelX, panelY, panelX + infoPanelWidth, panelY + infoPanelHeight);

        int currentY = (int) (panelY + infoScrollY);
        int startY = currentY;

        currentY += renderWrappedText(font, g, selectedNode.getName().getString(), panelX, currentY, infoPanelWidth, 0.7f, 0xFFFFA500);
        currentY += 4;

        renderScaledText(font, g, "Цена: " + selectedNode.getCost() + " ОИ", panelX, currentY, 0.6f, 0xFFFFFFFF, false);
        currentY += 10;

        List<String> parents = selectedNode.getParents();
        if (!parents.isEmpty()) {
            renderScaledText(font, g, "Требуется:", panelX, currentY, 0.6f, 0xFF999999, false);
            currentY += 8;

            var allNodes = SscResearchAndDevelopmentMod.ResearchDataHandler.MANAGER.getNodes();
            for (String parentId : parents) {
                ResearchNode parentNode = allNodes.get(parentId);
                if (parentNode != null) {
                    boolean isParentUnlocked = unlockedTechs.contains(parentId);
                    int color = isParentUnlocked ? 0xFF55FF55 : 0xFFFF5555;

                    currentY += renderWrappedText(font, g, "• " + parentNode.getName().getString(), panelX, currentY, infoPanelWidth, 0.55f, color);
                    currentY += 2;
                }
            }
            currentY += 4;
        }
        currentY += 5;
        currentY += renderWrappedText(font, g, selectedNode.getDescription().getString(), panelX, currentY, infoPanelWidth, 0.6f, 0xFFAAAAAA);
        currentY += 8;
        renderScaledText(font, g, "Открывает:", panelX, currentY, 0.7f, 0xFF35DCBD, false);
        currentY += 12;
        List<ItemStack> unlockedItems = getUnlockedItems(selectedNode.getId());
        if (!unlockedItems.isEmpty()) {
            for (int i = 0; i < unlockedItems.size(); i++) {
                int row = i / 4;
                int col = i % 4;
                g.renderFakeItem(unlockedItems.get(i), panelX + (col * 18), currentY + (row * 18));
            }
            currentY += ((unlockedItems.size() - 1) / 4 + 1) * 18;
        }
        this.totalContentHeight = currentY - startY;
        g.disableScissor();
    }

    private void renderInfoItemTooltips(GuiGraphics g, int mx, int my) {
        if (selectedNode == null) return;
        int panelX = leftPos + 212;
        int panelY = topPos + 95;
        if (mx >= panelX && mx <= panelX + infoPanelWidth && my >= panelY && my <= panelY + infoPanelHeight) {
            int currentY = (int) (panelY + infoScrollY);
            currentY += getWrappedTextHeight(selectedNode.getName().getString(), infoPanelWidth, 0.7f) + 4;
            currentY += 10;
            List<String> parents = selectedNode.getParents();
            if (!parents.isEmpty()) {
                currentY += 8;
                var allNodes = SscResearchAndDevelopmentMod.ResearchDataHandler.MANAGER.getNodes();
                for (String parentId : parents) {
                    ResearchNode parentNode = allNodes.get(parentId);
                    if (parentNode != null) {
                        currentY += getWrappedTextHeight("• " + parentNode.getName().getString(), infoPanelWidth, 0.55f);
                        currentY += 2;
                    }
                }
                currentY += 4;
            }
            currentY += 5 + getWrappedTextHeight(selectedNode.getDescription().getString(), infoPanelWidth, 0.6f);
            currentY += 8 + 12;
            List<ItemStack> unlockedItems = getUnlockedItems(selectedNode.getId());
            for (int i = 0; i < unlockedItems.size(); i++) {
                int row = i / 4;
                int col = i % 4;
                int ix = panelX + (col * 18);
                int iy = currentY + (row * 18);

                if (mx >= ix && mx <= ix + 18 && my >= iy && my <= iy + 18) {
                    ItemStack stack = unlockedItems.get(i);
                    List<Component> tooltipLines = new ArrayList<>();
                    tooltipLines.add(stack.getHoverName().copy().withStyle(ChatFormatting.GOLD));

                    var clientComponents = tooltipLines.stream()
                            .map(Component::getVisualOrderText)
                            .map(net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent::create)
                            .toList();

                    g.renderTooltip(font, clientComponents, mx, my, net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE, null);
                }
            }
        }
    }

    private void renderConnections(GuiGraphics g) {
        for (TechButton btn : techButtons) {
            for (String parentId : btn.node.getParents()) {
                TechButton parent = findButtonById(parentId);
                if (parent != null) {
                    int x1 = parent.getX() + 10;
                    int y1 = parent.getY() + 10;
                    int x2 = btn.getX() + 10;
                    int y2 = btn.getY() + 10;
                    g.fill(x1, y1, x2, y1 + 1, 0xFF444444);
                    g.fill(x2, y1, x2 + 1, y2, 0xFF444444);
                }
            }
        }
    }

    private void rebuildTechTree() {
        techButtons.clear();
        var nodes = SscResearchAndDevelopmentMod.ResearchDataHandler.MANAGER.getNodes();
        for (ResearchNode node : nodes.values()) {
            techButtons.add(new TechButton(node));
        }
    }

    private List<ItemStack> getUnlockedItems(String techId) {
        return SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER.getRecipes().values().stream()
                .filter(r -> r.getRequiredTechnologies().contains(techId))
                .map(FabricatorRecipe::getResult)
                .toList();
    }

    @Nullable
    private TechButton findButtonById(String id) {
        return techButtons.stream().filter(btn -> btn.node.getId().equals(id)).findFirst().orElse(null);
    }

    private int getWrappedTextHeight(String text, int maxWidth, float scale) {
        var lines = font.split(Component.literal(text), (int) (maxWidth / scale));
        return (int) (lines.size() * font.lineHeight * scale);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (mx >= leftPos + vX && mx <= leftPos + vX + vW && my >= topPos + vY && my <= topPos + vY + vH) {
            for (TechButton tb : techButtons) {
                if (tb.mouseClicked(mx, my, btn)) return true;
            }
            if (btn == 0) isDragging = true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (isDragging) {
            scrollX += dx;
            scrollY += dy;
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        isDragging = false;
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        int panelX = leftPos + 212;
        int panelY = topPos + 95;

        if (mx >= panelX && mx <= panelX + infoPanelWidth && my >= panelY && my <= panelY + infoPanelHeight) {
            this.infoScrollY += (float) (scrollY * 10);
            float maxScroll = Math.max(0, totalContentHeight - infoPanelHeight);
            if (infoScrollY > 0) infoScrollY = 0;
            if (infoScrollY < -maxScroll) infoScrollY = -maxScroll;
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        g.blit(RenderPipelines.GUI_TEXTURED, BG_TEXTURE, this.leftPos - 10, this.topPos, 0, 0, 300, 200, 300, 200);
    }

    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}

    private class TechButton extends Button {
        private final ResearchNode node;
        private final int origX, origY;

        public TechButton(ResearchNode node) {
            super(0, 0, 20, 20, Component.empty(), b -> {
                selectedNode = ((TechButton) b).node;
                infoScrollY = 0;
            }, DEFAULT_NARRATION);
            this.node = node;
            this.origX = node.getX();
            this.origY = node.getY();
        }

        public void updatePos() {
            setX((int) (leftPos + vX + origX + scrollX));
            setY((int) (topPos + vY + origY + scrollY));
        }

        @Override
        public void renderWidget(GuiGraphics g, int mx, int my, float pt) {
            if (getX() + width < leftPos + vX || getX() > leftPos + vX + vW ||
                    getY() + height < topPos + vY || getY() > topPos + vY + vH) return;

            int borderColor;
            boolean isUnlocked = unlockedTechs.contains(node.getId());

            if (isUnlocked) {
                borderColor = 0xFF55FF55;
            } else if (connectedServer != null) {
                boolean parentsUnlocked = node.getParents().stream().allMatch(unlockedTechs::contains);
                if (parentsUnlocked) {
                    if (serverPoints >= node.getCost()) {
                        borderColor = 0xFFFFAA00;
                    } else {
                        borderColor = 0xFFFF5555;
                    }
                } else {
                    borderColor = 0xFF555555;
                }
            } else {
                borderColor = 0xFF555555;
            }

            g.fill(getX(), getY(), getX() + width, getY() + height, 0xCC000000);
            g.renderOutline(getX(), getY(), width, height, borderColor);

            if (selectedNode == node) {
                g.renderOutline(getX() - 1, getY() - 1, width + 2, height + 2, 0xFFFFFFFF);
            }

            renderScaledIcon(node.getIcon(), g, getX() + 2, getY() + 2, 1.0f, 16, 16);
        }
    }
}