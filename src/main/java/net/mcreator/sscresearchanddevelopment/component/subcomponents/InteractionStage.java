package net.mcreator.sscresearchanddevelopment.component.subcomponents;

import net.mcreator.sscresearchanddevelopment.enums.ToolType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.Optional;

public class InteractionStage {
    private final ToolType requiredTool;       // Нужен инструмент (может быть null, если нужен материал)
    private final Ingredient requiredMaterial; // Нужен конкретный предмет/материал (может быть null)
    private final int durationTicks;           // Время работы в тиках
    private final ItemStack dropItem;          // Что выпадает игроку (ItemStack.EMPTY, если ничего)

    private final java.util.function.Function<net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.block.state.BlockState> nextStateFunction;

    public InteractionStage(ToolType tool, Ingredient material, int durationTicks, ItemStack dropItem,
                            java.util.function.Function<net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.block.state.BlockState> nextStateFunction) {
        this.requiredTool = tool;
        this.requiredMaterial = material;
        this.durationTicks = durationTicks;
        this.dropItem = dropItem;
        this.nextStateFunction = nextStateFunction;
    }

    public ToolType getRequiredTool() { return requiredTool; }
    public Ingredient getRequiredMaterial() { return requiredMaterial; }
    public int getDurationTicks() { return durationTicks; }
    public ItemStack getDropItem() { return dropItem.copy(); }

    public net.minecraft.world.level.block.state.BlockState getNextState(net.minecraft.world.level.block.state.BlockState currentState) {
        return nextStateFunction.apply(currentState);
    }

    public boolean matches(ToolType tool, ItemStack heldItem) {
        if (this.requiredTool != null) {
            return this.requiredTool == tool;
        }
        if (this.requiredMaterial != null) {
            return this.requiredMaterial.test(heldItem);
        }
        return false;
    }
}