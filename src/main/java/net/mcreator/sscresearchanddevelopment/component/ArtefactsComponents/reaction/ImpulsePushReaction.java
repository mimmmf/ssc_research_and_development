package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.reaction;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactReaction;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ImpulsePushReaction extends ArtefactReaction {

    private final double radius;
    private final double force;

    public ImpulsePushReaction(double radius, double force) {
        this.radius = radius;
        this.force = force;
    }

    @Override
    public void execute(ArtefactContext context, ArtefactNode node) {
        if (!(context.level() instanceof ServerLevel serverLevel)) return;

        BlockPos pos = context.pos();
        Vec3 centerPos = Vec3.atCenterOf(pos);
        AABB area = new AABB(pos).inflate(radius);

        List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : targets) {
            Vec3 entityPos = entity.position();
            // Вектор от центра артефакта к сущности
            Vec3 pushDir = entityPos.subtract(centerPos);

            // Если сущность стоит прямо в центре, задаём случайное направление
            if (pushDir.lengthSqr() < 0.0001D) {
                pushDir = new Vec3(1.0D, 0.0D, 0.0D);
            } else {
                pushDir = pushDir.normalize();
            }

            // Рассчитываем горизонтальный и вертикальный импульс
            double vx = pushDir.x * force;
            double vy = 0.35D + (force * 0.2D); // Подбрасываем слегка вверх
            double vz = pushDir.z * force;

            // Применяем вектор физики
            entity.setDeltaMovement(new Vec3(vx, vy, vz));
            entity.hurtMarked = true; // Принудительно отправляем пакет движения клиенту (чтобы не было дерганий)

            // Частицы ветра/удара на сущности
            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    entity.getX(), entity.getY(0.5D), entity.getZ(),
                    8, 0.2D, 0.2D, 0.2D, 0.1D
            );
        }

        // --- ЭФФЕКТЫ ВОЛНЫ ---
        // Кольцо из частиц взрывной волны вокруг артефакта
        serverLevel.sendParticles(
                ParticleTypes.EXPLOSION,
                centerPos.x, centerPos.y, centerPos.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D
        );

        serverLevel.playSound(
                (Entity) null, pos,
                SoundEvents.WIND_CHARGE_BURST.value(),
                SoundSource.BLOCKS,
                1.5F, 0.6F + serverLevel.getRandom().nextFloat() * 0.4F
        );
    }
}