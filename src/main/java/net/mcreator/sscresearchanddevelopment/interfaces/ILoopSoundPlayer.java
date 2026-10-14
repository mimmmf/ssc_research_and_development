package net.mcreator.sscresearchanddevelopment.interfaces;

import net.mcreator.sscresearchanddevelopment.client.sound.BlockEntityLoopSound;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ILoopSoundPlayer {
    SoundEvent getLoopSound();
    default float getSoundVolume() {
        return 1.0f;
    }
    default float getSoundPitch() {
        return 1.0f;
    }
    default void tryPlayLoopSound(BlockEntity be, boolean isStarted, Runnable markStarted) {
        if (be.getLevel() != null && be.getLevel().isClientSide) {
            if (!isStarted) {
                net.minecraft.client.Minecraft.getInstance().getSoundManager().play(
                        new BlockEntityLoopSound(getLoopSound(), (BlockEntity) be, getSoundVolume(), getSoundPitch())
                );
                markStarted.run();
            }
        }
    }
}