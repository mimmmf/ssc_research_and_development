package net.mcreator.sscresearchanddevelopment.libs;

import net.mcreator.ssc.network.Ssc14ModVariables;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.concurrent.atomic.AtomicBoolean;

public class Ssc14Handle {

    /**
     * Универсальный метод отправки радиосообщения от имени какого-либо источника (консоли, NPC и т.д.)
     */
    public static void sendRadioMessage(Level level, String channelName, int colorRgb, Item requiredKey, String senderName, String messageText) {
        // 1. Проверяем состояние частоты в переменных мира
        boolean hasConnection = true;
        try {
            net.mcreator.ssc.network.Ssc14ModVariables.MapVariables vars = Ssc14ModVariables.MapVariables.get(level);
            hasConnection = switch (channelName) {
                case "Общий" -> vars.Base_Radio_Frequency;
                case "Командование" -> vars.Command_Radio_Frequency;
                case "Инженерный" -> vars.Engeneer_Radio_Frequency;
                case "Медицинский" -> vars.Medical_Radio_Frequency;
                case "Научный" -> vars.RND_Radio_Frequency;
                case "Безопасность" -> vars.Sec_Radio_Frequency;
                case "Сервис" -> vars.Serv_Radio_Frequency;
                case "Снабжение" -> vars.Cargo_Radio_Frequency;
                default -> true;
            };
        } catch (Exception e) {
            hasConnection = true;
        }

        // 2. Создаем стилизованный компонент сообщения
        Component radioMessage = Component.literal("[" + channelName + "] " + senderName + " говорит, \"" + messageText + "\"")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(colorRgb)));

        // 3. Отправка игрокам на текущем уровне, у которых есть гарнитура с ключом
        if (hasConnection) {
            for (Player target : level.players()) {
                if (target.level() == level && hasHeadsetWithKeyCustom(target, requiredKey)) {
                    target.displayClientMessage(radioMessage, false);
                }
            }
        }

        // 4. Дублирование сообщения наблюдателям (спектторам) на сервере
        MinecraftServer serverInstance = ServerLifecycleHooks.getCurrentServer();
        if (serverInstance != null) {
            for (Player target : serverInstance.getPlayerList().getPlayers()) {
                if (target.isSpectator()) {
                    target.displayClientMessage(radioMessage, false);
                }
            }
        }
    }

    public static boolean hasHeadsetWithKeyCustom(Player player, Item requiredKey) {
        net.minecraft.tags.TagKey<Item> headsetTag = net.minecraft.tags.ItemTags.create(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ssc14", "headsets")
        );
        net.minecraft.world.entity.player.Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.is(headsetTag) && hasSpecificKeyInsideCustom(stack, requiredKey)) {
                return true;
            }
        }

        try {
            AtomicBoolean atomicFound = new AtomicBoolean(false);
            top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent((invHandler) -> {
                var earHandler = invHandler.getStacksHandler("ear");
                if (earHandler.isPresent()) {
                    IItemHandler handler = earHandler.get().getStacks();
                    for (int i = 0; i < handler.getSlots(); ++i) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (!stack.isEmpty() && stack.is(headsetTag) && hasSpecificKeyInsideCustom(stack, requiredKey)) {
                            atomicFound.set(true);
                        }
                    }
                }
            });
            if (atomicFound.get()) return true;
        } catch (Exception ignored) {}

        return false;
    }

    public static boolean hasSpecificKeyInsideCustom(ItemStack headset, Item requiredKey) {
        IItemHandler container = headset.getCapability(Capabilities.ItemHandler.ITEM);
        if (container == null) {
            return false;
        }
        for (int i = 0; i < container.getSlots(); ++i) {
            ItemStack itemInSlot = container.getStackInSlot(i);
            if (!itemInSlot.isEmpty() && itemInSlot.is(requiredKey)) {
                return true;
            }
        }
        return false;
    }
}