package net.mcreator.sscresearchanddevelopment.procedures;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.network.S2CProgressSyncPacket;
import net.mcreator.sscresearchanddevelopment.enums.ToolType;
import net.mcreator.sscresearchanddevelopment.interfaces.IInteractable;
import net.mcreator.sscresearchanddevelopment.interfaces.ITool;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class UniversalToolUseProcedure {

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (!(entity instanceof Player player)) return;

        ItemStack heldItem = player.getMainHandItem();

        BlockPos pos = BlockPos.containing(x, y, z);
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof IInteractable interactableBlock)) return;

        ToolType toolType = (heldItem.getItem() instanceof ITool tool) ? tool.getToolType() : null;

        if (interactableBlock.onToolInteract(world, pos, player, toolType, heldItem, 0)) {
            Vec3 startPos = player.position();

            runProgressLoop(world, pos, player, startPos, heldItem, toolType, interactableBlock, 0);
        }
    }

    private static void runProgressLoop(LevelAccessor world, BlockPos pos, Player player, Vec3 startPos,
                                        ItemStack toolStack, ToolType toolType, IInteractable target, int elapsedTicks) {

        if (player.getMainHandItem() != toolStack || player.position().distanceToSqr(startPos) > 0.01) {
            updateProgressBar(player, 0, 0);
            return;
        }

        BlockState state = world.getBlockState(pos);
        int totalDuration = target.getDurationTicks(state, toolType, toolStack);

        updateProgressBar(player, elapsedTicks, totalDuration);

        if (elapsedTicks >= totalDuration) {
            target.onToolInteract(world, pos, player, toolType, toolStack, elapsedTicks);
            playToolSound(world, pos, toolType);
            updateProgressBar(player, 0, 0);
            return;
        }

        SscResearchAndDevelopmentMod.queueServerWork(1, () -> {
            runProgressLoop(world, pos, player, startPos, toolStack, toolType, target, elapsedTicks + 1);
        });
    }

    private static void updateProgressBar(Player player, int elapsedTicks, int totalDuration) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        int currentFrame = 0;

        if (elapsedTicks > 0 && totalDuration > 0) {
            int totalGuiFrames = 6;
            double progressRatio = (double) elapsedTicks / totalDuration;
            currentFrame = (int) Math.ceil(progressRatio * totalGuiFrames);
            currentFrame = Math.max(1, Math.min(currentFrame, totalGuiFrames));
        }

        PacketDistributor.sendToPlayer(serverPlayer, new S2CProgressSyncPacket(currentFrame));
    }

    private static void playToolSound(LevelAccessor world, BlockPos pos, ToolType toolType) {
        if (world instanceof Level level) {
            level.playSound(null, pos, SoundEvents.SPYGLASS_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}