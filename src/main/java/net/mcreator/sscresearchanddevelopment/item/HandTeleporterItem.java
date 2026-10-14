package net.mcreator.sscresearchanddevelopment.item;

import net.mcreator.sscresearchanddevelopment.entity.PortalEntity;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModEntities;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class HandTeleporterItem extends Item {
    private static final int MAX_PORTALS = 2;
    private static final int CHANNEL_TICKS = 40;
    private static final int COOLDOWN_TICKS = 60;
    private static final double MOVE_EPS_SQR = 1.0E-4;
    private static final Map<UUID, Vec3> CHANNELS = new HashMap<>();

    public HandTeleporterItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }

    /**
     * Клик: удаление порталов (мгновенно) или начало установки (канал).
     */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        List<UUID> portals = getPortals(stack);

        // 3-й клик: удаляем оба портала мгновенно
        if (portals.size() >= MAX_PORTALS) {
            if (level instanceof ServerLevel serverLevel) {
                destroyPortals(serverLevel, stack, player.blockPosition());
                startCooldown(player, stack);
            }
            return InteractionResult.SUCCESS;
        }

        // Установка: проверяем зону до начала канала
        if (!isZoneFree(level, player)) {
            if (!level.isClientSide()) {
                player.displayClientMessage(
                        Component.translatable("message.ssc_research_and_development.teleporter.blocked"), true);
            }
            return InteractionResult.FAIL;
        }

        // Канал стартует на обеих сторонах, чтобы клиент показал анимацию
        player.startUsingItem(hand);
        if (level instanceof ServerLevel) {
            CHANNELS.put(player.getUUID(), player.position());
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return CHANNEL_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    /**
     * Серверная проверка движения: если игрок сдвинулся, отменяем установку.
     */
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        Vec3 start = CHANNELS.get(player.getUUID());
        if (start == null) {
            return;
        }
        if (player.position().distanceToSqr(start) > MOVE_EPS_SQR) {
            player.displayClientMessage(
                    Component.translatable("message.ssc_research_and_development.teleporter.moved"), true);
            player.stopUsingItem(); // вызовет releaseUsing
        }
    }

    /**
     * Срабатывает, если игрок отпустил кнопку, сменил предмет/руку или установка отменена.
     */
    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!level.isClientSide() && entity instanceof Player player) {
            CHANNELS.remove(player.getUUID());
        }
        return false;
    }

    /**
     * Срабатывает, когда канал дошёл до конца (сервер).
     */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel && entity instanceof ServerPlayer player) {
            CHANNELS.remove(player.getUUID());
            placePortal(serverLevel, player, stack);
        }
        return stack;
    }

    private static void placePortal(ServerLevel level, ServerPlayer player, ItemStack stack) {
        if (!isZoneFree(level, player)) {
            player.displayClientMessage(
                    Component.translatable("message.ssc_research_and_development.teleporter.blocked"), true);
            return;
        }

        List<UUID> portals = getPortals(stack);
        PortalEntity blue = null;
        if (!portals.isEmpty()) {
            blue = findPortal(level.getServer(), portals.get(0));
            if (blue == null) {
                // Синий портал пропал (удалён другим способом), начинаем заново
                portals = List.of();
            } else if (blue.level() != level) {
                // Межмировые пары не поддерживаются
                player.displayClientMessage(
                        Component.translatable("message.ssc_research_and_development.teleporter.other_dimension"), true);
                return;
            }
        }

        EntityType<PortalEntity> type = portals.isEmpty()
                ? SscResearchAndDevelopmentModEntities.PORTAL_BLUE.get()
                : SscResearchAndDevelopmentModEntities.PORTAL_RED.get();

        BlockPos origin = player.blockPosition();
        PortalEntity portal = new PortalEntity(type, level);
        portal.setPos(origin.getX() + 1.0, origin.getY(), origin.getZ() + 1.0);
        portal.setYRot(player.getYRot());

        if (!level.addFreshEntity(portal)) {
            return;
        }

        portal.captureOccupants();
        if (blue != null) {
            portal.linkTo(blue);
        }

        List<UUID> updated = new ArrayList<>(portals);
        updated.add(portal.getUUID());
        stack.set(SscResearchAndDevelopmentDataComponents.TELEPORTER_PORTALS.get(), updated);
        startCooldown(player, stack);

        level.playSound(null, origin, SscResearchAndDevelopmentSounds.HIGH_TECH_CONFIRM.get(), SoundSource.PLAYERS);
    }
    /**
     * Проверяет зону 2x2x2 блока, начиная с блока игрока. Сущности не учитываются,
     * игрок и другие порталы не блокируют установку, блокируют только твёрдые блоки.
     */
    private static boolean isZoneFree(Level level, Player player) {
        BlockPos origin = player.blockPosition();
        AABB zone = new AABB(
                origin.getX(), origin.getY(), origin.getZ(),
                origin.getX() + 2, origin.getY() + 2, origin.getZ() + 2);
        return !level.getBlockCollisions(null, zone).iterator().hasNext();
    }

    private static void startCooldown(Player player, ItemStack stack) {
        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
    }

    private static void destroyPortals(ServerLevel level, ItemStack stack, BlockPos pos) {
        MinecraftServer server = level.getServer();
        for (UUID id : getPortals(stack)) {
            PortalEntity portal = findPortal(server, id);
            if (portal != null) {
                portal.discard();
            }
        }
        stack.remove(SscResearchAndDevelopmentDataComponents.TELEPORTER_PORTALS.get());
        level.playSound(null, pos, SscResearchAndDevelopmentSounds.BUTTON.get(), SoundSource.PLAYERS);
    }

    private static PortalEntity findPortal(MinecraftServer server, UUID id) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(id) instanceof PortalEntity portal) {
                return portal;
            }
        }
        return null;
    }

    private static List<UUID> getPortals(ItemStack stack) {
        return stack.getOrDefault(SscResearchAndDevelopmentDataComponents.TELEPORTER_PORTALS.get(), List.of());
    }
}