package net.mcreator.sscresearchanddevelopment.client.gui;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.client.C2SRequestServerList;
import net.mcreator.sscresearchanddevelopment.component.fabricator.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.mcreator.sscresearchanddevelopment.world.inventory.FabricatorMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class FabricatorScreen extends AbstractContainerScreen<FabricatorMenu> {
    private static final ResourceLocation BG_TEXTURE = ResourceLocation.parse("ssc_research_and_development:textures/screens/autolathe_gui.png");
    private static final ResourceLocation ICON_TEXTURE = ResourceLocation.parse("ssc_research_and_development:textures/ui/fabricator_item_icon.png");
    private Button serversButton;
    private RecipeList recipeList;
    private StorageList storageList;
    private final java.util.Map<String, Integer> lastMaterials = new java.util.HashMap<>();
    @Nullable
    private FabricatorRecipe hoveredRecipeForTooltip = null;

    public FabricatorScreen(FabricatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 236;
        this.imageHeight = 180;
    }

    @Override
    protected void init() {
        super.init();
        this.serversButton = addRenderableWidget(createSideBtn(this.leftPos + 9, this.topPos + 160, 99, 10, "Сервера", b -> {
            var be = this.menu.getBlockEntity();
            if (be != null) {
                ClientPacketDistributor.sendToServer(new C2SRequestServerList(be.getBlockPos()));
            }
        }));

        this.recipeList = new RecipeList(this.minecraft, 100, 134, topPos + 22, 14);
        this.recipeList.setX(leftPos + 9);
        this.addRenderableWidget(this.recipeList);

        this.storageList = new StorageList(this.minecraft, 108, 80, topPos + 89, 14);
        this.storageList.setX(leftPos + 119);
        this.addRenderableWidget(this.storageList);

        if (SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER != null) {
            var recipes = SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER.getRecipes().values().stream().toList();
            this.updateFromManager(recipes);
        }
    }
    private void renderRecipeTooltip(GuiGraphics g, FabricatorRecipe recipe, int mx, int my) {
        var be = menu.getBlockEntity();
        if (be == null) return;

        List<Component> tooltipLines = new ArrayList<>();
        tooltipLines.add(recipe.getResult().getHoverName().copy().withStyle(ChatFormatting.GOLD));
        tooltipLines.add(Component.empty());

        tooltipLines.add(Component.literal("Необходимые ресурсы:").withStyle(ChatFormatting.GRAY));

        var storage = be.getMaterials();

        for (MaterialCost cost : recipe.getMaterials()) {
            var values = cost.ingredient().getValues();
            if (values.size() == 0) continue;

            Item item = values.get(0).value();
            String storageKey = FabricatorManager.getStorageKeyFromItem(new ItemStack(item));
            int amountInStorage = storage.getOrDefault(storageKey, 0);

            boolean hasEnough = amountInStorage >= cost.count();

            MutableComponent line = Component.literal(" • ")
                    .append(item.getName())
                    .append(": " + amountInStorage + "/" + cost.count());

            if (hasEnough) {
                line.withStyle(ChatFormatting.GREEN);
            } else {
                line.withStyle(ChatFormatting.RED).withStyle(ChatFormatting.UNDERLINE);
            }

            tooltipLines.add(line);
        }
        List<net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent> clientComponents =
                tooltipLines.stream()
                        .map(Component::getVisualOrderText)
                        .map(net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent::create)
                        .toList();

        g.renderTooltip(font, clientComponents, mx, my, net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE, null);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.hoveredRecipeForTooltip = null;

        g.blit(RenderPipelines.GUI_TEXTURED, BG_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        var be = this.menu.getBlockEntity();
        if (be != null && be.getActiveRecipeId() != null) {
            var activeRecipe = SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER.getRecipes().get(be.getActiveRecipeId());
            if (activeRecipe != null) {
                ItemStack resultStack = activeRecipe.getResult();
                int activeX = leftPos + 150;
                int activeY = topPos + 30;

                renderScaledIcon(g, activeX, activeY, 2f, 16, 16);

                float progress = be.getCraftProgressPercent(pt);
                int fillHeight = (int) (32 * progress);
                if (fillHeight > 0) {
                    g.fill(activeX, activeY + 32 - fillHeight, activeX + 32, activeY + 32, 0x88FF9800);
                }

                g.pose().pushMatrix();
                g.pose().translate(activeX, activeY);
                g.pose().scale(1.75f, 1.75f);
                g.renderFakeItem(resultStack, 1, 1);
                g.pose().popMatrix();

                float remainingSeconds = (be.getCraftTotalTime() - be.getCraftProgress()) / 20.0f;
                if (remainingSeconds < 0) remainingSeconds = 0;
                String timeText = String.format("%.1fs", remainingSeconds);
                renderScaledText(g, timeText, activeX + 16, activeY + 12, 1f, 0xFFFFFFFF, true);

                renderScaledText(g, resultStack.getHoverName().getString(), leftPos + 165, topPos + 64, 0.7f, -26368, true);
            }
        } else {
            renderScaledIcon(g, leftPos + 150, topPos + 30, 2f, 16, 16);
            renderScaledText(g, "Ничего", leftPos + 165, topPos + 64, 0.7f, 0xFFFFFFFF, true);
        }

        renderScaledText(g, be != null ? be.getDisplayName().getString() : "Фабрикатор", leftPos + 30, topPos + 7, 1.0f, -26368, true);
        renderScaledText(g, "Очередь производства", leftPos + 170, topPos + 19, 0.6f, 0xFFFFFFFF, true);
        renderScaledText(g, "Материалы", leftPos + 170, topPos + 76, 0.6f, 0xFFFFFFFF, true);

        super.render(g, mx, my, pt);

        if (this.hoveredRecipeForTooltip != null) {
            renderRecipeTooltip(g, this.hoveredRecipeForTooltip, mx, my);
        }
    }

    @Override protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {}
    @Override protected void renderLabels(GuiGraphics g, int mx, int my) {}

    private void renderScaledText(GuiGraphics g, String text, float x, float y, float scale, int color, boolean centered) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        if (centered) g.drawCenteredString(font, text, 0, 0, color);
        else g.drawString(font, text, 0, 0, color);
        g.pose().popMatrix();
    }

    private void renderScaledIcon(GuiGraphics g, int x, int y, float scale, int width, int height) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.blit(RenderPipelines.GUI_TEXTURED, ICON_TEXTURE, 0, 0, 0, 0, width, height, width, height);
        g.pose().popMatrix();
    }

    private Button createSideBtn(int x, int y, int width, int height, String label, Button.OnPress press) {
        return new Button(x, y, width, height, Component.literal(""), press, (supplier) -> (MutableComponent) supplier.get()) {
            @Override
            public void renderWidget(GuiGraphics g, int mx, int my, float pt) {
                int color = isHoveredOrFocused() ? new Color(99, 59, 0).getRGB() : new Color(61, 63, 86).getRGB();
                g.fill(getX(), getY(), getX() + width, getY() + height, color);
                renderScaledText(g, label, getX() + width / 2f, getY() + 2, 0.7f, active ? 0xFFFFFFFF : 0xFFA0A0A0, true);
            }
        };
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.recipeList != null && this.recipeList.isMouseOver(mouseX, mouseY)) return this.recipeList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    public void updateFromManager(List<FabricatorRecipe> recipes) {
        this.recipeList.publicClear();
        var be = this.menu.getBlockEntity();
        if (be == null) return;

        recipes.stream()
                .filter(be::canCraftRecipe)
                .sorted((r1, r2) -> {
                    String name1 = r1.getResult().getHoverName().getString();
                    String name2 = r2.getResult().getHoverName().getString();
                    return name1.compareToIgnoreCase(name2);
                })
                .forEach(recipe -> {
                    this.recipeList.publicAdd(new RecipeEntry(
                            recipe.getResult().getHoverName().getString(),
                            recipe.getResult(),
                            recipe
                    ));
                });
    }
    private void updateStorageMaterials() {
        var be = this.menu.getBlockEntity();
        if (be != null) {
            var current = be.getMaterials();

            if (!current.equals(lastMaterials)) {
                this.storageList.publicClear();

                current.entrySet().stream()
                        .filter(entry -> entry.getValue() > 0)
                        .filter(entry -> be.supportsMaterial(entry.getKey()))
                        .sorted(java.util.Map.Entry.comparingByKey())
                        .forEach(entry -> {
                            String name = entry.getKey();
                            int amount = entry.getValue();
                            ItemStack icon = FabricatorManager.getItemFromStorageKey(name);
                            this.storageList.publicAdd(new StorageEntry(name, name + " " + amount, icon));
                        });

                lastMaterials.clear();
                lastMaterials.putAll(current);
            }
        }
    }
    @Override
    protected void containerTick() {
        super.containerTick();
        if (SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER != null) {
            var recipes = SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER.getRecipes().values().stream().toList();
            this.updateFromManager(recipes);
        }
        updateStorageMaterials();
    }

    public class RecipeList extends ObjectSelectionList<RecipeEntry> {
        public RecipeList(Minecraft mc, int w, int h, int t, int ih) { super(mc, w, h, t, ih); }
        public void publicAdd(RecipeEntry entry) { this.addEntry(entry); }
        public void publicClear() { this.clearEntries(); }
        @Override protected int scrollBarX() { return this.getX() + this.width - 6; }
    }

    public class StorageList extends ObjectSelectionList<StorageEntry> {
        public StorageList(Minecraft mc, int w, int h, int t, int ih) { super(mc, w, h, t, ih); }
        public void publicAdd(StorageEntry entry) { this.addEntry(entry); }
        public void publicClear() { this.clearEntries(); }
        @Override protected int scrollBarX() { return this.getX() + this.width - 6; }
    }

    public class RecipeEntry extends ObjectSelectionList.Entry<RecipeEntry> {
        private final String text;
        private final ItemStack icon;
        private final FabricatorRecipe recipe;

        public RecipeEntry(String text, ItemStack icon, FabricatorRecipe recipe) {
            this.text = text;
            this.icon = icon;
            this.recipe = recipe;
        }

        private boolean hasEnoughResources() {
            var be = menu.getBlockEntity();
            if (be == null) return false;
            var storage = be.getMaterials();

            for (MaterialCost cost : recipe.getMaterials()) {
                var values = cost.ingredient().getValues();
                if (values.size() == 0) continue;
                Item item = values.get(0).value();
                String storageKey = FabricatorManager.getStorageKeyFromItem(new ItemStack(item));

                if (storageKey != null) {
                    int amountInStorage = storage.getOrDefault(storageKey, 0);

                    if (amountInStorage < cost.count()) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void render(GuiGraphics g, int index, int top, int left, int width, int height, int mx, int my, boolean hov, float pt) {
            boolean enough = hasEnoughResources();
            int color = enough ? 0xFFFFFFFF : 0xFFAA0000;

            g.fill(left, top - 1,left + 200,top + 10, enough ? 0xFF3d3f56 : 0xFF22222a);
            renderScaledText(g, text, left + 72, top + 1, 0.75f, color, false);

            if (!icon.isEmpty()) {
                g.pose().pushMatrix();
                g.pose().translate(left + 58, top - 1);
                g.pose().scale(0.6f, 0.6f);
                g.renderFakeItem(icon, 0, 0);
                g.pose().popMatrix();
            }
            if (hov) {
                FabricatorScreen.this.hoveredRecipeForTooltip = this.recipe;
            }
        }
        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            recipeList.setSelected(this);
            if (this.recipe != null) {
                var be = menu.getBlockEntity();
                if (be != null) {
                    ResourceLocation id = null;
                    if (SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER != null) {
                        for (var entry : SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER.getRecipes().entrySet()) {
                            if (entry.getValue() == this.recipe) {
                                id = entry.getKey();
                                break;
                            }
                        }
                    }
                    if (id != null) {
                        ClientPacketDistributor.sendToServer(new C2SStartCraft(be.getBlockPos(), id));
                    }
                }
            }
            return true;
        }

        @Override public Component getNarration() { return Component.literal(text); }
    }

    public class StorageEntry extends ObjectSelectionList.Entry<StorageEntry> {
        private final String text;
        private final String rawType;
        private final ItemStack icon;

        public StorageEntry(String rawType, String text, ItemStack icon) {
            this.text = text;
            this.rawType = rawType;
            this.icon = icon;
        }

        @Override
        public void render(GuiGraphics g, int index, int top, int left, int width, int height, int mx, int my, boolean hov, float pt) {
            g.fill(left,top - 1,left + 200,top + 10, 0xFF3D3F56);
            renderScaledText(g, text, left + 70, top + 2, 0.75f, 0xFFFFFFFF, false);
            if (!icon.isEmpty()) {
                g.pose().pushMatrix();
                g.pose().translate(left + 56, top);
                g.pose().scale(0.6f, 0.6f);
                g.renderFakeItem(icon, 0, 0);
                g.pose().popMatrix();
            }
        }
        @Override
        public boolean mouseClicked(double mx, double my, int button) {
            int amountToWithdraw = Screen.hasShiftDown() ? 64 : 1;

            var be = menu.getBlockEntity();
            if (be != null) {
                ClientPacketDistributor.sendToServer(new C2SWithdrawMaterial(be.getBlockPos(), this.rawType, amountToWithdraw));
            }
            return true;
        }

        @Override public Component getNarration() { return Component.literal(text); }
    }
}