package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.reaction;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactReaction;
import net.mcreator.sscresearchanddevelopment.init.ArtefactRegistries;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class EntitySummonReaction extends ArtefactReaction {

    private final List<String> entityIds;
    private final int minCount;
    private final int maxCount;

    public EntitySummonReaction(List<String> entityIds, int minCount, int maxCount) {
        this.entityIds = entityIds;
        this.minCount = minCount;
        this.maxCount = maxCount;
    }

    @Override
    public void execute(ArtefactContext context, ArtefactNode node) {
        Level level = context.level();
        if (!(level instanceof ServerLevel serverLevel) || entityIds.isEmpty()) return;

        RandomSource random = serverLevel.getRandom();

        int totalEntities = minCount >= maxCount
                ? minCount
                : random.nextInt(minCount, maxCount + 1);

        BlockPos pos = context.pos();
        double spawnX = pos.getX() + 0.5D;
        double spawnY = pos.getY() + 0.5D;
        double spawnZ = pos.getZ() + 0.5D;

        for (int i = 0; i < totalEntities; i++) {
            String selectedId = entityIds.get(random.nextInt(entityIds.size()));
            ResourceLocation entityLoc = parseLocation(selectedId);

            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(entityLoc)
                    .map(Holder.Reference::value)
                    .orElse((EntityType) EntityType.PIG);

            if (type == EntityType.PIG && !entityLoc.getPath().equals("pig")) {
                continue;
            }

            // 1. Считаем смещение по кругу вокруг артефакта
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double offset = 0.5D + random.nextDouble() * 0.8D;
            double ex = spawnX + Math.cos(angle) * offset;
            double ez = spawnZ + Math.sin(angle) * offset;

            BlockPos spawnPos = BlockPos.containing(ex, spawnY, ez);

            // 2. Создаем сущность
            Entity entity = type.create(serverLevel, null, spawnPos, EntitySpawnReason.MOB_SUMMONED, true, false);

            if (entity != null) {
                // Исполнение абсолютного позиционирования в координатах мира (без физического сдвига)
                entity.teleportTo(ex, spawnY, ez);

                if (entity instanceof Mob mob) {
                    mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), EntitySpawnReason.MOB_SUMMONED, null);
                }

                // 3. Импульс разлета веером в стороны
                entity.setDeltaMovement(new Vec3(
                        Math.cos(angle) * 0.15D,
                        0.2D,
                        Math.sin(angle) * 0.15D
                ));

                serverLevel.addFreshEntity(entity);

                // Частицы призыва ровно возле моба
                serverLevel.sendParticles(
                        ParticleTypes.WITCH,
                        ex, spawnY + 0.5D, ez,
                        10, 0.2D, 0.4D, 0.2D, 0.05D
                );
            }
        }

        serverLevel.playSound(
                null, pos,
                SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.BLOCKS,
                1.0F, 0.8F + random.nextFloat() * 0.4F
        );
    }

    private ResourceLocation parseLocation(String id) {
        return id.contains(":") ? ResourceLocation.parse(id) : ResourceLocation.fromNamespaceAndPath(ArtefactRegistries.MODID, id);
    }
}