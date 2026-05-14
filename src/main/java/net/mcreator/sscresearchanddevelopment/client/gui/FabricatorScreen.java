package net.mcreator.sscresearchanddevelopment.client.gui;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.client.C2SRequestServerList;
import net.mcreator.sscresearchanddevelopment.client.gui.misc.FlatButton;
import net.mcreator.sscresearchanddevelopment.component.fabricator.*;
import net.mcreator.sscresearchanddevelopment.interfaces.IScreenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
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
import java.util.*;

public class FabricatorScreen extends AbstractContainerScreen<FabricatorMenu> implements IScreenHelper {
    private static final ResourceLocation BG_TEXTURE = ResourceLocation.parse("ssc_research_and_development:textures/screens/autolathe_gui.png");
    private static final ResourceLocation ICON_TEXTURE = ResourceLocation.parse("ssc_research_and_development:textures/ui/fabricator_item_icon.png");
    private RecipeList recipeList;
    private StorageList storageList;
    private final Map<String, Integer> lastMaterials = new HashMap<>();
    private FabricatorRecipe hoveredRecipe = null;
    private final Set<String> lastUnlockedTechs = new HashSet<>();
    public FabricatorScreen(FabricatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 236;
        this.imageHeight = 180;
    }
    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new FlatButton(leftPos + 9, topPos + 160, 99, 10, "Сервера", 0.7f, 0xFF3D3F56, 0xFF633B00, font, b -> {
            if (menu.getBlockEntity() != null)
                ClientPacketDistributor.sendToServer(new C2SRequestServerList(menu.getBlockEntity().getBlockPos()));
        }));

        this.recipeList = addRenderableWidget(new RecipeList(minecraft, 100, 134, topPos + 22, 14));
        this.recipeList.setX(leftPos + 9);

        this.storageList = addRenderableWidget(new StorageList(minecraft, 108, 80, topPos + 89, 14));
        this.storageList.setX(leftPos + 119);

        refreshRecipes();

        if (menu.getBlockEntity() != null) {
            ClientPacketDistributor.sendToServer(new C2SRequestFabricatorSync(menu.getBlockEntity().getBlockPos()));
        }
    }
    @Override
    protected void containerTick() {
        super.containerTick();
        var be = menu.getBlockEntity();
        if (be != null && !be.getMaterials().equals(lastMaterials)) {
            if (!be.getMaterials().equals(lastMaterials)) {
                updateStorageMaterials(be);
            }
            if (!be.getClientUnlockedTechs().equals(lastUnlockedTechs)) {
                lastUnlockedTechs.clear();
                lastUnlockedTechs.addAll(be.getClientUnlockedTechs());
                refreshRecipes();
            }
        }
    }
    private void updateStorageMaterials(AbstractFabricatorBlockEntity be) {
        storageList.publicClear();
        lastMaterials.clear();
        lastMaterials.putAll(be.getMaterials());

        lastMaterials.entrySet().stream()
                .filter(e -> e.getValue() > 0 && be.supportsMaterial(e.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    ItemStack icon = FabricatorManager.getItemFromStorageKey(e.getKey());
                    storageList.publicAdd(new StorageEntry(e.getKey(), e.getKey() + " " + e.getValue(), icon));
                });
    }
    private void refreshRecipes() {
        if (SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER == null) return;
        var be = menu.getBlockEntity();
        if (be == null) return;

        recipeList.publicClear();
        SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER.getRecipes().values().stream()
                .filter(be::canCraftRecipe)
                .sorted(Comparator.comparing(r -> r.getResult().getHoverName().getString()))
                .forEach(r -> recipeList.publicAdd(new RecipeEntry(r.getResult().getHoverName().getString(), r.getResult(), r)));
    }
    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        this.hoveredRecipe = null;
        renderBg(g, pt, mx, my);
        super.render(g, mx, my, pt);

        var be = menu.getBlockEntity();
        renderCraftingStatus(g, be, pt);

        renderScaledText(font, g, be != null ? be.getDisplayName().getString() : "Фабрикатор", leftPos + 30, topPos + 7, 1.0f, -26368, true);
        renderScaledText(font, g, "Очередь", leftPos + 170, topPos + 19, 0.6f, 0xFFFFFFFF, true);
        renderScaledText(font, g, "Материалы", leftPos + 170, topPos + 76, 0.6f, 0xFFFFFFFF, true);

        if (hoveredRecipe != null) renderRecipeTooltip(g, hoveredRecipe, mx, my);
    }
    private void renderCraftingStatus(GuiGraphics g, AbstractFabricatorBlockEntity be, float pt) {
        int ax = leftPos + 150, ay = topPos + 30;
        var recipeId = be != null ? be.getActiveRecipeId() : null;

        renderScaledIcon(ICON_TEXTURE, g, ax, ay, 2f, 16, 16);

        if (recipeId != null) {
            var recipe = SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER.getRecipes().get(recipeId);
            if (recipe != null) {
                int fill = (int) (32 * be.getCraftProgressPercent(pt));
                g.fill(ax, ay + 32 - fill, ax + 32, ay + 32, 0x88FF9800);

                renderScaledItem(g, recipe.getResult(), ax + 2, ay + 2, 1.75f);

                String time = String.format("%.1fs", Math.max(0, (be.getCraftTotalTime() - be.getCraftProgress()) / 20.0f));
                renderScaledText(font, g, time, ax + 16, ay + 12, 1f, 0xFFFFFFFF, true);
                renderScaledText(font, g, recipe.getResult().getHoverName().getString(), ax + 15, ay + 34, 0.7f, -26368, true);
                return;
            }
        }
        renderScaledText(font, g, "Ничего", ax + 15, ay + 34, 0.7f, 0xFFFFFFFF, true);
    }
    private void renderScaledItem(GuiGraphics g, ItemStack stack, int x, int y, float scale) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.renderFakeItem(stack, 0, 0);
        g.pose().popMatrix();
    }
    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        g.blit(RenderPipelines.GUI_TEXTURED, BG_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {

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

            String storageKey = FabricatorManager.getStorageKeyFromItem(item.getDefaultInstance());
            int amountInStorage = storage.getOrDefault(storageKey, 0);
            boolean hasEnough = amountInStorage >= cost.count();

            MutableComponent line = Component.literal(" • ")
                    .append(item.getName())
                    .append(": " + amountInStorage + "/" + cost.count());

            line.withStyle(hasEnough ? ChatFormatting.GREEN : ChatFormatting.RED);
            if (!hasEnough) line.withStyle(ChatFormatting.UNDERLINE);

            tooltipLines.add(line);
        }

        List<ClientTooltipComponent> clientComponents = tooltipLines.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();

        g.renderTooltip(font, clientComponents, mx, my, DefaultTooltipPositioner.INSTANCE, null);
    }
    public abstract class BaseList<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {
        public BaseList(Minecraft mc, int w, int h, int t, int i) { super(mc, w, h, t, i); }
        public void publicAdd(E entry) { addEntry(entry); }
        public void publicClear() { clearEntries(); }
        @Override protected int scrollBarX() { return getX() + width - 6; }
    }
    public class RecipeList extends BaseList<RecipeEntry> {
        public RecipeList(Minecraft mc, int w, int h, int t, int i) { super(mc, w, h, t, i); }
    }
    public class StorageList extends BaseList<StorageEntry> {
        public StorageList(Minecraft mc, int w, int h, int t, int i) { super(mc, w, h, t, i); }
    }
    public class RecipeEntry extends ObjectSelectionList.Entry<RecipeEntry> {
        private final String text;
        private final ItemStack icon;
        private final FabricatorRecipe recipe;
        private Boolean cachedEnough;

        public RecipeEntry(String text, ItemStack icon, FabricatorRecipe recipe) {
            this.text = text; this.icon = icon; this.recipe = recipe;
        }

        private boolean hasEnough() {
            if (cachedEnough != null) return cachedEnough;
            var be = menu.getBlockEntity();
            if (be == null) return false;
            var storage = be.getMaterials();
            for (MaterialCost cost : recipe.getMaterials()) {
                Item item = cost.ingredient().getValues().get(0).value();
                String key = FabricatorManager.getStorageKeyFromItem(item.getDefaultInstance());
                if (storage.getOrDefault(key, 0) < cost.count()) return cachedEnough = false;
            }
            return cachedEnough = true;
        }

        @Override
        public void render(GuiGraphics g, int idx, int top, int left, int w, int h, int mx, int my, boolean hov, float pt) {
            this.cachedEnough = null;
            boolean enough = hasEnough();
            g.fill(left, top - 1, left + 200, top + 10, enough ? 0xFF3D3F56 : 0xFF22222A);
            renderScaledText(font, g, text, left + 72, top + 1, 0.75f, enough ? 0xFFFFFFFF : 0xFFAA0000, false);
            if (!icon.isEmpty()) renderScaledItem(g, icon, left + 58, top - 1, 0.6f);
            if (hov) FabricatorScreen.this.hoveredRecipe = this.recipe;
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn) {
            recipeList.setSelected(this);
            var be = menu.getBlockEntity();
            if (be != null && SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER != null) {
                ResourceLocation id = SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER.getRecipes()
                        .entrySet().stream().filter(e -> e.getValue() == recipe).map(Map.Entry::getKey).findFirst().orElse(null);
                if (id != null) ClientPacketDistributor.sendToServer(new C2SStartCraft(be.getBlockPos(), id));
            }
            return true;
        }

        @Override public boolean keyPressed(int p_252335_, int p_252135_, int p_251011_) { return false; }
        @Override public Component getNarration() { return Component.literal(text); }
    }

    public class StorageEntry extends ObjectSelectionList.Entry<StorageEntry> {
        private final String text, rawType;
        private final ItemStack icon;

        public StorageEntry(String rawType, String text, ItemStack icon) {
            this.rawType = rawType; this.text = text; this.icon = icon;
        }

        @Override
        public void render(GuiGraphics g, int idx, int top, int left, int w, int h, int mx, int my, boolean hov, float pt) {
            g.fill(left, top - 1, left + 200, top + 10, 0xFF3D3F56);
            renderScaledText(font, g, text, left + 70, top + 2, 0.75f, 0xFFFFFFFF, false);
            if (!icon.isEmpty()) renderScaledItem(g, icon, left + 56, top, 0.6f);
        }

        @Override
        public boolean mouseClicked(double mx, double my, int btn) {
            if (menu.getBlockEntity() != null) {
                int count = hasShiftDown() ? 64 : 1;
                ClientPacketDistributor.sendToServer(new C2SWithdrawMaterial(menu.getBlockEntity().getBlockPos(), rawType, count));
            }
            return true;
        }

        @Override public boolean keyPressed(int p_252335_, int p_252135_, int p_251011_) { return false; }
        @Override public Component getNarration() { return Component.literal(text); }
    }
}