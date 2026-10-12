package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.reaction;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactReaction;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class AreaDamageReaction extends ArtefactReaction {

    private final float damageAmount;
    private final double radius;

    public AreaDamageReaction(float damageAmount, double radius) {
        this.damageAmount = damageAmount;
        this.radius = radius;
    }

    @Override
    public void execute(ArtefactContext context, ArtefactNode node) {
        if (!(context.level() instanceof ServerLevel serverLevel)) return;

        BlockPos pos = context.pos();
        AABB area = new AABB(pos).inflate(radius);

        // Находим всех живых существ в радиусе
        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : targets) {
            // Наносим магический урон (игнорирует обычную броню)
            entity.hurt(serverLevel.damageSources().magic(), damageAmount);

            // Частицы урона прямо на жертве
            serverLevel.sendParticles(
                    ParticleTypes.DAMAGE_INDICATOR,
                    entity.getX(), entity.getY(0.5D), entity.getZ(),
                    5, 0.2D, 0.2D, 0.2D, 0.1D
            );
        }

        // АоЕ Визуал (красно-черная вспышка)
        serverLevel.sendParticles(
                ParticleTypes.SWEEP_ATTACK,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                15, radius / 2.0D, 0.5D, radius / 2.0D, 0.0D
        );

        serverLevel.playSound(
                null, pos,
                SoundEvents.WITHER_HURT,
                SoundSource.BLOCKS,
                1.0F, 0.5F
        );
    }
}