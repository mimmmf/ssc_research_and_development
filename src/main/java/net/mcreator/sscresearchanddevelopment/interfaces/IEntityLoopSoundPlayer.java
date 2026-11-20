package net.mcreator.sscresearchanddevelopment.interfaces;

import net.mcreator.sscresearchanddevelopment.sound.EntityLoopSound;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

public interface IEntityLoopSoundPlayer {
    SoundEvent getLoopSound();

    default float getSoundVolume() {
        return 1.0f;
    }

    default float getSoundPitch() {
        return 1.0f;
    }
    default void tryPlayLoopSound(Entity entity, boolean isStarted, Runnable markStarted) {
        if (entity.level() != null && entity.level().isClientSide) {
            if (!isStarted) {
                Minecraft.getInstance().getSoundManager().play(
                        new EntityLoopSound(getLoopSound(), entity, getSoundVolume(), getSoundPitch())
                );
                markStarted.run();
            }
        }
    }
}