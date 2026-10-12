package net.mcreator.sscresearchanddevelopment.event;

import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactanalyzerBlockEntity;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactEngine;
import net.mcreator.sscresearchanddevelopment.init.ArtefactRegistries;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ToolUseEventData;
import net.mcreator.sscresearchanddevelopment.item.ArtefactItem;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = ArtefactRegistries.MODID)
public class ArtefactPlayerEvents {

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof ArtefactItem) {
            Object[] args = new Object[]{"attack", event.getTarget()};
            ArtefactContext context = new ArtefactContext(
                    player.level(),
                    player.blockPosition(),
                    stack,
                    args,
                    player
            );

            ArtefactEngine.tickArtifact(stack, context);
        }
    }

    @SubscribeEvent
    public static void onPlayerDamage(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            for (ItemStack stack : new ItemStack[]{player.getItemInHand(InteractionHand.MAIN_HAND), player.getItemInHand(InteractionHand.OFF_HAND)}) {
                if (stack.getItem() instanceof ArtefactItem) {
                    Object[] args = new Object[]{"take_damage", event.getSource(), event.getNewDamage()};
                    ArtefactContext context = new ArtefactContext(
                            player.level(),
                            player.blockPosition(),
                            stack,
                            args,
                            player
                    );

                    ArtefactEngine.tickArtifact(stack, context);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        ItemStack stack = player.getItemInHand(event.getHand());
        if (stack.getItem() instanceof ArtefactItem) {
            Object[] args = new Object[]{"left_click_block", event.getPos(), event.getFace()};
            ArtefactContext context = new ArtefactContext(
                    player.level(),
                    event.getPos(),
                    stack,
                    args,
                    player
            );

            ArtefactEngine.tickArtifact(stack, context);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        if (player.isOnFire()) {
            ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND).copy();
            ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND).copy();
            for (ItemStack stack : new ItemStack[]{mainHand, offHand}) {
                if (stack.getItem() instanceof ArtefactItem) {
                    Object[] args = new Object[]{"player_on_fire"};
                    ArtefactContext context = new ArtefactContext(
                            player.level(),
                            player.blockPosition(),
                            stack,
                            args,
                            player
                    );
                    ArtefactEngine.tickArtifact(stack, context);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemEntityDamage(EntityInvulnerabilityCheckEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (stack.getItem() instanceof ArtefactItem) {
                if (event.getSource().is(DamageTypeTags.IS_EXPLOSION) || event.getSource().is(DamageTypeTags.IS_LIGHTNING)) {
                    event.setInvulnerable(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemSpawnInWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (stack.getItem() instanceof ArtefactItem) {
                Player thrower = event.getLevel().getNearestPlayer(
                        itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(),
                        2.0D, false
                );
                if (thrower != null) {
                    Object[] args = new Object[]{"item_dropped", itemEntity};
                    ArtefactContext context = new ArtefactContext(
                            event.getLevel(),
                            itemEntity.blockPosition(),
                            stack,
                            args,
                            thrower
                    );
                    ArtefactEngine.tickArtifact(stack, context);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Level level = victim.level();
        if (level.isClientSide) return;
        double radius = 10.0D;
        AABB area = victim.getBoundingBox().inflate(radius);
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, area);
        for (Player player : nearbyPlayers) {
            for (ItemStack stack : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
                if (stack.getItem() instanceof ArtefactItem) {
                    Object[] args = new Object[]{"nearby_death", victim, event.getSource()};
                    ArtefactContext context = new ArtefactContext(
                            level,
                            victim.blockPosition(),
                            stack,
                            args,
                            player
                    );
                    ArtefactEngine.tickArtifact(stack, context);
                }
            }
        }
        List<ItemEntity> nearbyItems = level.getEntitiesOfClass(ItemEntity.class, area);
        for (ItemEntity itemEntity : nearbyItems) {
            ItemStack stack = itemEntity.getItem();
            if (stack.getItem() instanceof ArtefactItem) {
                Object[] args = new Object[]{"nearby_death", victim, event.getSource()};
                ArtefactContext context = new ArtefactContext(
                        level,
                        victim.blockPosition(),
                        stack,
                        args,
                        itemEntity
                );
                ArtefactEngine.tickArtifact(stack, context);
            }
        }
        BlockPos center = victim.blockPosition();
        int bRadius = (int) radius;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-bRadius, -bRadius, -bRadius), center.offset(bRadius, bRadius, bRadius))) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ArtefactanalyzerBlockEntity analyzer) {
                ItemStack stack = analyzer.getItem(0);

                if (stack.getItem() instanceof ArtefactItem) {
                    Object[] args = new Object[]{"nearby_death", victim, event.getSource()};
                    ArtefactContext context = new ArtefactContext(
                            level,
                            pos,
                            stack,
                            args,
                            null
                    );

                    ArtefactEngine.tickArtifact(stack, context);
                    analyzer.setChanged();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
        if (be instanceof ArtefactanalyzerBlockEntity analyzer) {
            ItemStack heldItem = event.getItemStack();
            // Передаем данные об инструменте, только если в руки что-то зажато
            if (!heldItem.isEmpty()) {
                analyzer.onTriggerEvent(new ToolUseEventData(player, heldItem));
            }
        }

        // Вызываем проверку артефактов в руках только один раз за клик (для основной руки)
        if (event.getHand() == InteractionHand.MAIN_HAND) {
            triggerHandArtifacts(player, "right_click_block", event.getPos());
        }
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        if (event.getHand() == InteractionHand.MAIN_HAND) {
            triggerHandArtifacts(player, "right_click_item", player.blockPosition());
        }
    }

    private static void triggerHandArtifacts(Player player, String actionName, BlockPos pos) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof ArtefactItem) {
                ItemStack otherHandStack = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);

                Object[] args = new Object[]{
                        actionName,
                        pos,
                        new ToolUseEventData(player, otherHandStack)
                };

                ArtefactContext context = new ArtefactContext(
                        player.level(),
                        pos,
                        stack,
                        args,
                        player
                );

                ArtefactEngine.tickArtifact(stack, context);
            }
        }
    }
}