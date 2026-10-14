package net.mcreator.sscresearchanddevelopment.interfaces;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.component.LinkedBlockComponent;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ILinkable {
    /**
     * Попытка установить связь.
     * Теперь по умолчанию возвращает true, если позиция была успешно записана.
     */
    default boolean tryLink(Player player, ItemStack tool) {
        // Логика получения позиции должна быть в реализации или мультитуле,
        // здесь мы просто проверяем, не занято ли место.
        if (getLinkedPos().isPresent()) return false;
        return true;
    }

    /**
     * Попытка разорвать связь.
     * Возвращает true, если связь была и её успешно удалили.
     */
    default boolean tryUnlink(Player player, ItemStack tool) {
        if (getLinkedPos().isEmpty()) return false;
        setLinkedPos(null);
        return true;
    }

    /**
     * Может ли этот блок в принципе хранить связь.
     * По умолчанию true, так как ты сказал, что блоков без позиции больше нет.
     */
    default boolean canStoreLink() {
        return true;
    }

    /**
     * ГЛАВНЫЙ МЕТОД: Установка позиции.
     * В реализации BlockEntity этот метод должен обнулять поле targetPos,
     * менять флаг connected и вызывать level.sendBlockUpdated.
     */
    void setLinkedPos(@Nullable BlockPos pos);

    /**
     * ГЛАВНЫЙ МЕТОД: Получение позиции.
     */
    Optional<BlockPos> getLinkedPos();

    /**
     * Вызывается, когда удаляется блок, на который мы ссылались.
     */
    default void onLinkedBlockRemoved(BlockPos sourcePos) {
        if (getLinkedPos().isPresent() && getLinkedPos().get().equals(sourcePos)) {
            setLinkedPos(null);
        }
    }

    // --- СТАТИЧЕСКАЯ ЛОГИКА ОЧИСТКИ (без изменений в логике, только порядок) ---

    static void staticNotifyRemoval(Level level, BlockPos pos) {
        if (level.isClientSide) return;

        GlobalPos thisPos = GlobalPos.of(level.dimension(), pos);

        double radius = 64.0;
        List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(radius));
        for (Player player : players) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                cleanupItemStack(player.getInventory().getItem(i), thisPos);
            }
        }

        int r = 16;
        for (BlockPos nearbyPos : BlockPos.betweenClosed(pos.offset(-r, -r, -r), pos.offset(r, r, r))) {
            if (level.getBlockEntity(nearbyPos) instanceof ILinkable linkable) {
                linkable.onLinkedBlockRemoved(pos);
            }
        }
    }

    private static void cleanupItemStack(ItemStack stack, GlobalPos posToRemove) {
        if (stack.isEmpty()) return;

        var multiKey = SscResearchAndDevelopmentDataComponents.LINKED_BLOCKS.get();
        if (stack.has(multiKey)) {
            List<GlobalPos> links = stack.get(multiKey);
            if (links != null && links.contains(posToRemove)) {
                List<GlobalPos> newList = new ArrayList<>(links);
                newList.remove(posToRemove);
                if (newList.isEmpty()) stack.remove(multiKey);
                else stack.set(multiKey, newList);
            }
        }

        var singleKey = SscResearchAndDevelopmentDataComponents.LINKED_BLOCK.get();
        if (stack.has(singleKey)) {
            var component = stack.get(singleKey);
            if (component != null && component.linked_block().isPresent() && component.linked_block().get().equals(posToRemove.pos())) {
                stack.set(singleKey, new LinkedBlockComponent(Optional.empty()));
            }
        }
    }
}