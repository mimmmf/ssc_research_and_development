package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.reaction;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.core.BlockPos;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactReaction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class AreaHealReaction extends ArtefactReaction {

    private final float healAmount;
    private final double radius;

    public AreaHealReaction(float healAmount, double radius) {
        this.healAmount = healAmount;
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
            // Мгновенное лечение
            entity.heal(healAmount);

            // Частицы сердец над излеченными
            serverLevel.sendParticles(
                    ParticleTypes.HEART,
                    entity.getX(), entity.getY(1.0D), entity.getZ(),
                    6, 0.3D, 0.4D, 0.3D, 0.02D
            );
        }

        // АоЕ Волна исцеления вокруг артефакта
        serverLevel.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                30, radius / 2.0D, 0.5D, radius / 2.0D, 0.05D
        );

        serverLevel.playSound(
                null, pos,
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS,
                1.2F, 1.2F
        );
    }
}