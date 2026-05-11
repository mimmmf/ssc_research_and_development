package net.mcreator.sscresearchanddevelopment.client.gui;

import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactconsoleBlockEntity;
import net.mcreator.sscresearchanddevelopment.client.C2SRequestServerList;
import net.mcreator.sscresearchanddevelopment.component.ArtifactProperties;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ExtractPointsPayload;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.NodeStatus;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents;
import net.mcreator.sscresearchanddevelopment.interfaces.ILinkable;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.mcreator.sscresearchanddevelopment.world.inventory.ArtefactConsoleMenu;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModScreens;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.*;

public class ArtefactConsoleScreen extends AbstractContainerScreen<ArtefactConsoleMenu> implements SscResearchAndDevelopmentModScreens.ScreenAccessor {
    private static final ResourceLocation BG_TEXTURE = ResourceLocation.parse("ssc_research_and_development:textures/screens/artefact-console-gui.png");

    private final int x, y, z;
    private final List<NodeButton> nodeButtons = new ArrayList<>();
    private final int vX = 45, vY = 20, vW = 165, vH = 135;

    private boolean hasLinked, hasArtefact, isDragging;
    private float scrollX, scrollY;
    private ItemStack lastStack = ItemStack.EMPTY;
    private ArtefactNode selectedNode;
    private Button extractButton, serversButton;
    private UUID connectedServer;

    public ArtefactConsoleScreen(ArtefactConsoleMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.x = menu.x; this.y = menu.y; this.z = menu.z;
        this.imageWidth = 200; this.imageHeight = 150;
        this.connectedServer = menu.getConnectedServer();
    }

    @Override
    protected void init() {
        super.init();
        nodeButtons.clear();
        selectedNode = null;

        if (minecraft.level.getBlockEntity(new BlockPos(x, y, z)) instanceof ILinkable linkable) {
            hasLinked = linkable.getLinkedPos().isPresent();
        }

        this.serversButton = addRenderableWidget(createSideBtn(0, "Сервер", b -> {
            BlockPos pos = new BlockPos(this.x, this.y, this.z);
            ClientPacketDistributor.sendToServer(new C2SRequestServerList(pos));
        }));
        this.extractButton = addRenderableWidget(createSideBtn(9, "Извлечь", b -> {
            if (selectedNode != null && selectedNode.status() == NodeStatus.READY_TO_EXTRACT) {
                BlockPos analyzerPos = new BlockPos(this.x, this.y, this.z);
                ClientPacketDistributor.sendToServer(new ExtractPointsPayload(analyzerPos, selectedNode.id()));
            }
        }));
    }

    private Button createSideBtn(int yOffset, String label, Button.OnPress press) {
        return new Button(width / 2 - 120, height / 2 + 62 + yOffset, 58, 8, Component.literal(""), press, (supplier) -> (MutableComponent) supplier.get()) {
            @Override
            public void renderWidget(GuiGraphics g, int mx, int my, float pt) {
                int color = isHoveredOrFocused() ? new Color(99, 59, 0).getRGB() : new Color(61, 63, 86).getRGB();
                g.fill(getX(), getY(), getX() + width, getY() + height, color);
                renderScaledText(g, label, getX() + width / 2f, getY() + 1, 0.7f, active ? 0xFFFFFFFF : 0xFFA0A0A0);
            }
        };
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        //renderBackground(g, mx, my, pt);
        renderBg(g, pt, mx, my);

        ItemStack current = getStackFromAnalyzer();
        ArtifactProperties currentData = current.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());

        if (!ItemStack.isSameItemSameComponents(current, lastStack)) {
            this.lastStack = current.copy();
            this.hasArtefact = !current.isEmpty();

            ArtifactProperties data = current.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());
            rebuildNodes(data);

            if (selectedNode != null && data != null) {
                this.selectedNode = data.nodes().get(selectedNode.id());
            }
        }

        if (hasArtefact) {
            g.enableScissor(leftPos + vX, topPos + vY, leftPos + vX + vW, topPos + vY + vH);
            renderConnections(g, current.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get()));
            nodeButtons.forEach(btn -> {
                if (currentData != null) btn.updateNodeReference(currentData.nodes().get(btn.node.id()));
                btn.render(g, mx, my, pt);
            });
            g.disableScissor();
        }

        extractButton.visible = hasArtefact;
        extractButton.active = selectedNode != null && selectedNode.status() == NodeStatus.READY_TO_EXTRACT && connectedServer != null;

        super.render(g, mx, my, pt);
        renderArtifactPreview(g);
        renderTooltip(g, mx, my);
    }

    private void rebuildNodes(@Nullable ArtifactProperties data) {
        nodeButtons.forEach(this::removeWidget);
        nodeButtons.clear();
        if (data == null) return;

        Map<Integer, List<ArtefactNode>> layers = new HashMap<>();
        data.nodes().values().forEach(n -> layers.computeIfAbsent(n.depth(), k -> new ArrayList<>()).add(n));

        layers.forEach((depth, nodes) -> {
            for (int i = 0; i < nodes.size(); i++) {
                int bx = leftPos + 65 + (130 / (nodes.size() + 1)) * (i + 1) - 10;
                int by = topPos + 20 + (depth * 30);
                NodeButton btn = new NodeButton(bx, by, nodes.get(i));
                nodeButtons.add(btn);
                addWidget(btn);
            }
        });
        updateNodesPos();
    }

    private void updateNodesPos() {
        nodeButtons.forEach(b -> {
            b.setX((int) (b.origX + leftPos + scrollX));
            b.setY((int) (b.origY + topPos + scrollY));
        });
    }

    private void renderConnections(GuiGraphics g, ArtifactProperties data) {
        if (data == null) return;
        for (NodeButton p : nodeButtons) {
            for (int cid : p.node.childIds()) {
                nodeButtons.stream().filter(b -> b.node.id() == cid).findFirst().ifPresent(c -> {
                    int x1 = p.getX() + 10, y1 = p.getY() + 10, x2 = c.getX() + 10, y2 = c.getY() + 10;
                    int midY = y1 + (y2 - y1) / 2;
                    g.fill(x1, y1, x1 + 1, midY, 0xFF444444);
                    g.fill(Math.min(x1, x2), midY, Math.max(x1, x2), midY + 1, 0xFF444444);
                    g.fill(x2, midY, x2 + 1, y2, 0xFF444444);
                });
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(font, "Аналитическая консоль", -16, 5, -26368, false);

        ArtifactProperties props = lastStack.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());
        if (props != null) {
            g.pose().pushMatrix();
            String chargeText = "Доступных ОИ: " + props.totalPoints();
            int color = props.totalPoints() > 0 ? 0xFF55FF55 : 0xFF555555;

            g.pose().scale(0.8f, 0.8f);
            g.drawString(font, chargeText, (int)(110 / 0.8f), (int)(145 / 0.8f), color, true);
            g.pose().popMatrix();
        }

        g.pose().pushMatrix();
        g.pose().scale(0.6f, 0.6f);
        int tx = (int) (-20 / 0.6f), ty = (int) (82 / 0.6f);

        if (!hasLinked) g.drawString(font, "Анализатора нет!", tx, ty, 0xFFFF3737, false);
        else if (!hasArtefact) g.drawString(font, "Артефакта нет!", tx, ty, 0xFFFF3737, false);
        else if (selectedNode != null) {
            String statusName = switch(selectedNode.status()) {
                case ACTIVE -> "ОЖИДАНИЕ";
                case PRIMED -> "ЗАРЯДКА (" + String.format("%.1f", selectedNode.timer()/20f) + "с)";
                case EXHAUSTED -> "ИСПОЛЬЗОВАННО";
                case COOLING_DOWN -> "ОСТЫВАЕТ";
                case READY_TO_EXTRACT -> "ГОТОВО";
                case SOLVED -> "ИЗВЛЕЧЕНО";
                case LOCKED -> "ЗАБЛОКИРОВАНО";
            };

            String[] info = {
                    "ID: " + selectedNode.id(),
                    "Статус: " + statusName,
                    "Очков: " + selectedNode.points(),
                    "Глубина: " + selectedNode.depth(),
                    "Триггер: " + selectedNode.triggerId(),
                    "Реакция: " + selectedNode.reactionId()
            };
            int[] colors = {-3355444, 0xFF35DCBD, 0xFFAAAAAA, 0xFFAAAAAA, 0xFF7BDC35, 0xFFB22222};
            for (int i = 0; i < info.length; i++) g.drawString(font, info[i], tx, ty + (i * 10), colors[i], false);
        }
        if (connectedServer != null) {
            g.drawString(font,
                    "Подключён",
                    290,
                    12,
                    0xFF55FF55,
                    false);
        } else {
            g.drawString(font,
                    "Нет подключения",
                    255,
                    12,
                    0xFFFF5555,
                    false);
        }
        g.pose().popMatrix();
    }

    private void renderScaledText(GuiGraphics g, String text, float x, float y, float scale, int color) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.drawCenteredString(font, text, 0, 0, color);
        g.pose().popMatrix();
    }

    private void renderArtifactPreview(GuiGraphics g) {
        ItemStack stack = getStackFromAnalyzer();
        if (!stack.isEmpty()) {
            g.pose().pushMatrix();
            g.pose().translate(g.guiWidth() / 2f - 115, g.guiHeight() / 2f - 55);
            g.pose().scale(3.0f, 3.0f);
            g.renderFakeItem(stack, 0, 0);
            g.pose().popMatrix();
        }
    }

    @Override

    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BG_TEXTURE, this.leftPos + -25, this.topPos + -1, 0, 0, 240, 160, 240, 160);

    }

    private ItemStack getStackFromAnalyzer() {
        return (minecraft.level.getBlockEntity(new BlockPos(x, y, z)) instanceof ArtefactconsoleBlockEntity be) ? be.getItemFromLinkedAnalyzer() : ItemStack.EMPTY;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (serversButton.mouseClicked(mx, my, btn) || extractButton.mouseClicked(mx, my, btn)) return true;
        if (mx >= leftPos + vX && mx <= leftPos + vX + vW && my >= topPos + vY && my <= topPos + vY + vH) {
            for (NodeButton nb : nodeButtons) if (nb.mouseClicked(mx, my, btn)) return true;
            if (btn == 0) return isDragging = true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (isDragging) {
            scrollX += dx; scrollY += dy;
            updateNodesPos();
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override public boolean mouseReleased(double mx, double my, int btn) { isDragging = false; return super.mouseReleased(mx, my, btn); }
    @Override public void updateMenuState(int type, String name, Object state) {}

    private class NodeButton extends Button {
        private ArtefactNode node;
        private final int origX, origY;

        public NodeButton(int x, int y, ArtefactNode node) {
            super(x, y, 20, 20, Component.literal(String.valueOf(node.id())), b -> selectedNode = ((NodeButton)b).node, DEFAULT_NARRATION);
            this.node = node;
            this.origX = x - leftPos; this.origY = y - topPos;
        }

        @Override
        public void renderWidget(GuiGraphics g, int mx, int my, float pt) {
            boolean inV = mx >= leftPos + vX && mx <= leftPos + vX + vW && my >= topPos + vY && my <= topPos + vY + vH;
            boolean h = inV && mx >= getX() && my >= getY() && mx < getX() + width && my < getY() + height;

            int color = switch (node.status()) {
                case ACTIVE   -> 0xFF32B71D; // Зеленый
                case PRIMED   -> 0xFFFFA500; // Оранжевый
                case READY_TO_EXTRACT -> 0xFFFFFF00; // Желтый
                case LOCKED   -> 0xFFB02424; // Красный
                default       -> 0xFF444444; // Серый (SOLVED)
            };

            if (h || (selectedNode != null && selectedNode.id() == node.id())) color = 0xFFFFFFFF;

            g.fill(getX(), getY(), getX() + width, getY() + height, 0xFF000000);

            if (node.status() == NodeStatus.PRIMED && node.maxTimer() > 0) {
                float progress = 1.0f - ((float)node.timer() / (float)node.maxTimer());
                int fillWidth = (int)((width - 4) * progress);
                g.fill(getX() + 2, getY() + 2, getX() + 2 + fillWidth, getY() + height - 2, 0x66FFA500);
            }

            g.renderOutline(getX(), getY(), width, height, color);

            Component label = Component.literal(String.valueOf(node.id()));
            if (node.status() == NodeStatus.PRIMED) {
                label = Component.literal(Math.round(node.timer() / 20f) + "s");
            }

            g.drawCenteredString(font, label, getX() + width / 2, getY() + (height - 8) / 2, color);
        }
        public void updateNodeReference(ArtefactNode newNode) {
            if (newNode != null) this.node = newNode;
        }
    }
}