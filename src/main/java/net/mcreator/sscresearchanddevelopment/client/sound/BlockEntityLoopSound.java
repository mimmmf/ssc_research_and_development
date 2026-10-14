package net.mcreator.sscresearchanddevelopment.client.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityLoopSound extends AbstractTickableSoundInstance {
    private final BlockEntity blockEntity;
    public BlockEntityLoopSound(SoundEvent soundEvent, BlockEntity blockEntity, float volume, float pitch) {
        super(soundEvent, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.blockEntity = blockEntity;
        this.looping = true;
        this.delay = 0;
        this.volume = volume;
        this.pitch = pitch;

        BlockPos pos = blockEntity.getBlockPos();
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
    }
    @Override
    public void tick() {
        if (blockEntity.isRemoved() || blockEntity.getLevel() == null) {
            this.stop();
            return;
        }
    }
}
