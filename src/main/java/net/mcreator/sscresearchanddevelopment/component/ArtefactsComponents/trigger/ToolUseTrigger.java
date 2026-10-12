package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.trigger;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.init.ArtefactRegistries;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactTrigger;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ToolUseEventData;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ToolUseTrigger extends ArtefactTrigger {

    private final List<String> targetItemIds;

    public ToolUseTrigger(List<String> targetItemIds) {
        this.targetItemIds = targetItemIds;
    }

    @Override
    public boolean check(ArtefactContext context, ArtefactNode node) {
        Object[] args = context.context();
        if (args == null || args.length == 0) {
            return false;
        }

        boolean isClickEvent = false;
        ToolUseEventData explicitToolData = null;

        for (Object arg : args) {
            if (arg instanceof String action) {
                if (action.contains("click") || action.contains("use")) {
                    isClickEvent = true;
                }
            } else if (arg instanceof ToolUseEventData toolData) {
                explicitToolData = toolData;
            }
        }

        // ИСПРАВЛЕНИЕ 1: Наличие ToolUseEventData означает, что инструмент БЫЛ применен (клик совершон)
        if (explicitToolData != null) {
            isClickEvent = true;
        }

        if (!isClickEvent) {
            return false;
        }

        // 1. Проверка инструмента, переданного явно (например, из Анализатора блоков)
        if (explicitToolData != null && isTargetItem(explicitToolData.usedTool())) {
            return true;
        }

        // 2. Проверка инструмента в другой руке (если артефакт держится в руках игрока)
        if (context.source() instanceof Player player) {
            ItemStack artifactStack = context.stack();

            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            ItemStack otherHandItem = ItemStack.EMPTY;

            // ИСПРАВЛЕНИЕ 2: Безопасное сравнение объектов в руках
            if (artifactStack == mainHand || ItemStack.isSameItemSameComponents(artifactStack, mainHand)) {
                otherHandItem = offHand;
            } else if (artifactStack == offHand || ItemStack.isSameItemSameComponents(artifactStack, offHand)) {
                otherHandItem = mainHand;
            }

            if (isTargetItem(otherHandItem)) {
                return true;
            }
        }

        return false;
    }

    private boolean isTargetItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation itemLoc = BuiltInRegistries.ITEM.getKey(stack.getItem());

        for (String target : targetItemIds) {
            ResourceLocation targetLoc = parseLocation(target);
            if (itemLoc.equals(targetLoc)) return true;
        }
        return false;
    }

    private ResourceLocation parseLocation(String id) {
        return id.contains(":") ? ResourceLocation.parse(id) : ResourceLocation.fromNamespaceAndPath(ArtefactRegistries.MODID, id);
    }
}