package net.mcreator.sscresearchanddevelopment.entity;

import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentSounds;
import net.mcreator.sscresearchanddevelopment.interfaces.IEntityLoopSoundPlayer;
import net.mcreator.sscresearchanddevelopment.interfaces.ILoopSoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class AnomalyEntity extends Entity implements IEntityLoopSoundPlayer {
    private static final int LIGHT_LEVEL = 12;
    private boolean soundStarted = false;
    public AnomalyEntity(EntityType<? extends AnomalyEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();
        BlockPos pos = this.blockPosition();
        if (this.level().getBlockState(pos).isAir()) {
            this.level().setBlock(pos,
                    Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LIGHT_LEVEL), 3);
        }
        if (this.level().isClientSide) {
            tryPlayLoopSound(this, soundStarted, () -> soundStarted = true);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float v) {
        return false;
    }
    @Override
    public boolean isPickable() { return false; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {

    }

    @Override
    public SoundEvent getLoopSound() {
        return SscResearchAndDevelopmentSounds.ANOMALY_DRONE.get();
    }
    @Override
    public float getSoundVolume() {
        return 0.8f;
    }
    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (this.level().isClientSide) {
            this.soundStarted = false;
        }
    }
}
