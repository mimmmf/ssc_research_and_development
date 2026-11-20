package net.mcreator.sscresearchanddevelopment.sound;


import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class EntityLoopSound extends AbstractTickableSoundInstance {
    private final Entity entity;

    public EntityLoopSound(SoundEvent soundEvent, Entity entity, float volume, float pitch) {
        super(soundEvent, SoundSource.NEUTRAL, entity.getRandom());
        this.entity = entity;
        this.looping = true;
        this.delay = 0;
        this.volume = volume;
        this.pitch = pitch;
        this.relative = false;

        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }

    @Override
    public void tick() {
        if (this.entity.isRemoved() || !this.entity.isAlive()) {
            this.stop();
            return;
        }
        this.x = this.entity.getX();
        this.y = this.entity.getY();
        this.z = this.entity.getZ();
    }
}