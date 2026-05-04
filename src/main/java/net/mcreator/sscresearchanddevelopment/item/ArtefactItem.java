package net.mcreator.sscresearchanddevelopment.item;

import net.mcreator.sscresearchanddevelopment.component.ArtifactProperties;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.NodeStatus;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class ArtefactItem extends Item {
    public ArtefactItem(Item.Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(1).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        ArtifactProperties data = stack.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());

        if (data == null) {
            tooltip.accept(Component.literal("Объект не инициализирован").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            return;
        }

        // Основная инфа
        tooltip.accept(Component.literal("Тир артефакта: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(data.tier())).withStyle(ChatFormatting.GOLD)));
        tooltip.accept(Component.literal("Общие очки: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(data.totalPoints())).withStyle(ChatFormatting.AQUA)));
        tooltip.accept(Component.literal("--- Структура узлов ---").withStyle(ChatFormatting.DARK_GRAY));

        // Вывод узлов
        data.nodes().values().stream()
                .sorted(Comparator.comparingInt(ArtefactNode::id))
                .forEach(node -> {
                    ChatFormatting color = switch (node.status()) {
                        case ACTIVE -> ChatFormatting.GREEN;
                        case LOCKED -> ChatFormatting.RED;
                        default -> ChatFormatting.GRAY;
                    };

                    String info = String.format("ID %d [Глубина:%d] - %s (%d pts)",
                            node.id(), node.depth(), node.status().name(), node.points());

                    tooltip.accept(Component.literal(info).withStyle(color));

                    if (!node.childIds().isEmpty()) {
                        tooltip.accept(Component.literal("  └─ Дети: " + node.childIds())
                                .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
                    }
                });
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!level.isClientSide && !stack.has(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get())) {
            int randomTier = level.random.nextInt(3);
            stack.set(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get(),
                    ArtifactProperties.generate(randomTier, level.random));
        }
    }
}