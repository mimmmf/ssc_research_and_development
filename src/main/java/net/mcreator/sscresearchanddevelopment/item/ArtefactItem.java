package net.mcreator.sscresearchanddevelopment.item;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactEngine;
import net.mcreator.sscresearchanddevelopment.component.ArtifactProperties;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;

public class ArtefactItem extends Item {
    public ArtefactItem(Item.Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON).stacksTo(1).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (!flag.isAdvanced()) {
            return;
        }

        ArtifactProperties data = stack.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());

        if (data == null) {
            tooltip.accept(Component.literal("Объект не инициализирован").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            return;
        }

        tooltip.accept(Component.literal("Тир артефакта: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(data.tier())).withStyle(ChatFormatting.GOLD)));
        tooltip.accept(Component.literal("Общие очки: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(data.totalPoints())).withStyle(ChatFormatting.AQUA)));
        tooltip.accept(Component.literal("--- Структура узлов ---").withStyle(ChatFormatting.DARK_GRAY));

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

    public static boolean ensureInitialized(ItemStack stack, Level level) {
        if (stack.isEmpty() || level.isClientSide) return false;
        if (!stack.has(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get())) {
            int randomTier = level.random.nextInt(3);

            stack.set(
                    SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get(),
                    ArtifactProperties.generate(randomTier, level.random)
            );
            return true;
        }
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (level.isClientSide) return;

        ensureInitialized(stack, level);

        ArtefactContext context = new ArtefactContext(
                level,
                entity.blockPosition(),
                stack,
                new Object[0],
                entity
        );

        ArtifactProperties beforeProps = stack.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());

        // Метод вернет false, если предмет сейчас перетаскивается в курсоре игрока
        boolean modified = ArtefactEngine.tickArtifact(stack, context);

        if (modified && entity instanceof ServerPlayer serverPlayer) {
            ArtifactProperties afterProps = stack.get(SscResearchAndDevelopmentDataComponents.ARTIFACT_DATA.get());
            if (!Objects.equals(beforeProps, afterProps)) {
                serverPlayer.containerMenu.broadcastChanges();
            }
        }
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        Level level = entity.level();
        if (level.isClientSide) return false;
        ensureInitialized(stack, level);
        BlockPos pos = entity.blockPosition();
        boolean isBurningInWorld = entity.isOnFire()
                || entity.isInLava()
                || level.getBlockState(pos).is(Blocks.FIRE)
                || level.getBlockState(pos).is(Blocks.SOUL_FIRE)
                || level.getBlockState(pos.below()).is(Blocks.FIRE);
        Object[] args = isBurningInWorld ? new Object[]{"item_on_fire"} : new Object[0];
        ArtefactContext context = new ArtefactContext(
                level,
                pos,
                stack,
                args,
                entity
        );
        ArtefactEngine.tickArtifact(stack, context);
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (!level.isClientSide()) {
            Player player = ctx.getPlayer();
            ItemStack stack = ctx.getItemInHand();

            Object[] args = new Object[]{"right_click_block", ctx.getClickedPos(), ctx.getClickedFace()};
            ArtefactContext context = new ArtefactContext(
                    level,
                    ctx.getClickedPos(),
                    stack,
                    args,
                    player
            );

            ArtefactEngine.tickArtifact(stack, context);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        if (!level.isClientSide()) {
            Object[] args = new Object[]{"using_tick", count};
            ArtefactContext context = new ArtefactContext(
                    level,
                    entity.blockPosition(),
                    stack,
                    args,
                    entity
            );

            ArtefactEngine.tickArtifact(stack, context);
        }
        super.onUseTick(level, entity, stack, count);
    }
}