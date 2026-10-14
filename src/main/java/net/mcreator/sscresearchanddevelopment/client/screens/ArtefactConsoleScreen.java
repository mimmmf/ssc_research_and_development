package net.mcreator.sscresearchanddevelopment.client.screens;

import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactconsoleBlockEntity;
import net.mcreator.sscresearchanddevelopment.network.C2SRequestServerList;
import net.mcreator.sscresearchanddevelopment.client.gui.FlatButton;
import net.mcreator.sscresearchanddevelopment.component.ArtifactProperties;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.mcreator.sscresearchanddevelopment.network.ExtractPointsPayload;
import net.mcreator.sscresearchanddevelopment.enums.NodeStatus;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents;
import net.mcreator.sscresearchanddevelopment.interfaces.ILinkable;
import net.mcreator.sscresearchanddevelopment.interfaces.IScreenHelper;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.mcreator.sscresearchanddevelopment.world.inventory.ArtefactConsoleMenu;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModScreens;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import javax.annotation.Nullable;
import java.util.*;

public class ArtefactConsoleScreen extends AbstractContainerScreen<ArtefactConsoleMenu> implements SscResearchAndDevelopmentModScreens.ScreenAccessor, IScreenHelper {
    private static final ResourceLocation BG_TEXTURE = ResourceLocation.parse("ssc_research_and_development:textures/screens/artefact-console-gui.png");

    private static final int COLOR_BTN_NORMAL = 0xFF3D3F56;
    private static final int COLOR_BTN_HOVER = 0xFF633B00;

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

        this.serversButton = addRenderableWidget(new FlatButton(width / 2 - 120, height / 2 + 62, 58, 8, Component.translatable("gui.ssc_research_and_development.artefact_console_gui.server").getString(), 0.7f, COLOR_BTN_NORMAL, COLOR_BTN_HOVER, font, b -> {
            ClientPacketDistributor.sendToServer(new C2SRequestServerList(new BlockPos(x, y, z)));
        }));

        this.extractButton = addRenderableWidget(new FlatButton(width / 2 - 120, height / 2 + 71, 58, 8, Component.translatable("gui.ssc_research_and_development.artefact_console_gui.extract").getString(), 0.7f, COLOR_BTN_NORMAL, COLOR_BTN_HOVER, font, b -> {
            if (selectedNode != null && selectedNode.status() == NodeStatus.READY_TO_EXTRACT) {
                ClientPacketDistributor.sendToServer(new ExtractPointsPayload(new BlockPos(x, y, z), selectedNode));
            }
        }));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        ItemStack current = getStackFromAnalyzer();

        if (!ItemStack.isSameItemSameComponents(current, lastStack)) {
            this.lastStack = current.copy();
            this.hasArtefact = !current.isEmpty();

            ArtifactProperties data = current.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());
            rebuildNodes(data);
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBg(g, pt, mx, my);
        ArtifactProperties currentData = lastStack.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());

        // Актуализируем выбранный узел в каждом кадре рендера
        if (selectedNode != null && currentData != null) {
            this.selectedNode = currentData.nodes().get(selectedNode.id());
        }

        if (hasArtefact) {
            g.enableScissor(leftPos + vX, topPos + vY, leftPos + vX + vW, topPos + vY + vH);
            renderConnections(g, currentData);
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
            int size = nodes.size();
            for (int i = 0; i < size; i++) {
                int bx = leftPos + 65 + (130 / (size + 1)) * (i + 1) - 10;
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
        g.drawString(font, Component.translatable("gui.ssc_research_and_development.artefact_console_gui.title").getString(), -16, 5, -26368, false);

        ArtifactProperties props = lastStack.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());
        if (props != null) {
            String chargeText = Component.translatable("gui.ssc_research_and_development.artefact_console_gui.available_rp", props.totalPoints()).getString();
            int color = props.totalPoints() > 0 ? 0xFF55FF55 : 0xFF555555;
            renderScaledText(font, g, chargeText, 45, 145, 0.8f, color, false);
        }

        g.pose().pushMatrix();
        g.pose().scale(0.6f, 0.6f);
        int tx = (int) (-20 / 0.6f), ty = (int) (82 / 0.6f);

        if (!hasLinked) g.drawString(font, Component.translatable("gui.ssc_research_and_development.artefact_console_gui.no_analyzer").getString(), tx, ty, 0xFFFF3737, false);
        else if (!hasArtefact) g.drawString(font, Component.translatable("gui.ssc_research_and_development.artefact_console_gui.no_artefact").getString(), tx, ty, 0xFFFF3737, false);
        else if (selectedNode != null) {
            String statusName = getStatusString(selectedNode);
            String triggerName = Component.translatable(selectedNode.triggerId()).getString();
            String reactionName = Component.translatable(selectedNode.reactionId()).getString();

            String[] info = {
                    Component.translatable("gui.ssc_research_and_development.artefact_console_gui.node_id", selectedNode.id()).getString(),
                    Component.translatable("gui.ssc_research_and_development.artefact_console_gui.node_status", statusName).getString(),
                    Component.translatable("gui.ssc_research_and_development.artefact_console_gui.node_points", selectedNode.points()).getString(),
                    Component.translatable("gui.ssc_research_and_development.artefact_console_gui.node_depth", selectedNode.depth()).getString(),
                    Component.translatable("gui.ssc_research_and_development.artefact_console_gui.node_trigger", triggerName).getString(),
                    Component.translatable("gui.ssc_research_and_development.artefact_console_gui.node_reaction", reactionName).getString()
            };
            int[] colors = {-3355444, 0xFF35DCBD, 0xFFAAAAAA, 0xFFAAAAAA, 0xFF7BDC35, 0xFFB22222};
            for (int i = 0; i < info.length; i++) g.drawString(font, info[i], tx, ty + (i * 10), colors[i], false);
        }

        String connText = connectedServer != null
                ? Component.translatable("gui.ssc_research_and_development.artefact_console_gui.connected").getString()
                : Component.translatable("gui.ssc_research_and_development.artefact_console_gui.disconnected").getString();
        int connColor = connectedServer != null ? 0xFF55FF55 : 0xFFFF5555;
        g.drawString(font, connText, connectedServer != null ? 290 : 255, 12, connColor, false);

        g.pose().popMatrix();
    }

    private String getStatusString(ArtefactNode node) {
        return switch(node.status()) {
            case ACTIVE -> Component.translatable("gui.ssc_research_and_development.status.active").getString();
            case PRIMED -> {
                long remainingTicks = (minecraft != null && minecraft.level != null)
                        ? Math.max(0, node.timer() - minecraft.level.getGameTime())
                        : 0;
                yield Component.translatable("gui.ssc_research_and_development.status.primed", String.format("%.1f", remainingTicks / 20f)).getString();
            }
            case EXHAUSTED -> Component.translatable("gui.ssc_research_and_development.status.exhausted").getString();
            case COOLING_DOWN -> Component.translatable("gui.ssc_research_and_development.status.cooling_down").getString();
            case READY_TO_EXTRACT -> Component.translatable("gui.ssc_research_and_development.status.ready_to_extract").getString();
            case SOLVED -> Component.translatable("gui.ssc_research_and_development.status.solved").getString();
            case LOCKED -> Component.translatable("gui.ssc_research_and_development.status.locked").getString();
        };
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
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BG_TEXTURE, this.leftPos - 25, this.topPos - 1, 0, 0, 240, 160, 240, 160);
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
                case ACTIVE   -> 0xFF32B71D;
                case PRIMED   -> 0xFFFFA500;
                case READY_TO_EXTRACT -> 0xFFFFFF00;
                case LOCKED   -> 0xFFB02424;
                default       -> 0xFF444444;
            };

            if (h || (selectedNode != null && selectedNode.id() == node.id())) color = 0xFFFFFFFF;

            g.fill(getX(), getY(), getX() + width, getY() + height, 0xFF000000);

            long remainingTicks = (minecraft != null && minecraft.level != null)
                    ? Math.max(0, node.timer() - minecraft.level.getGameTime())
                    : 0;

            if (node.status() == NodeStatus.PRIMED && node.maxTimer() > 0) {
                float progress = 1.0f - Math.min(1.0f, (float) remainingTicks / (float) node.maxTimer());
                int fillWidth = Math.max(0, Math.min(width - 4, (int) ((width - 4) * progress)));
                g.fill(getX() + 2, getY() + 2, getX() + 2 + fillWidth, getY() + height - 2, 0x66FFA500);
            }

            g.renderOutline(getX(), getY(), width, height, color);

            Component label = node.status() == NodeStatus.PRIMED
                    ? Component.literal(Math.round(remainingTicks / 20f) + "s")
                    : Component.literal(String.valueOf(node.id()));

            g.drawCenteredString(font, label, getX() + width / 2, getY() + (height - 8) / 2, color);
        }

        public void updateNodeReference(ArtefactNode newNode) {
            if (newNode != null) this.node = newNode;
        }
    }
}