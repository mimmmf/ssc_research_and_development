package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.reaction;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactReaction;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class GrowTreeReaction extends ArtefactReaction {

    @Override
    public void execute(ArtefactContext context, ArtefactNode node) {
        if (!(context.level() instanceof ServerLevel serverLevel)) return;

        BlockPos center = context.pos();
        RandomSource random = serverLevel.getRandom();

        // --- 1. ПРЕВРАЩАЕМ ПОЛ В ДЁРН С ШЕРОХОВАТОЙ ГРАНИЦЕЙ ---
        int maxRadius = 4; // Максимальный возможный радиус
        double baseRadiusSq = 2.5 * 2.5; // Базовый радиус, где дёрн будет 100%

        for (int x = -maxRadius; x <= maxRadius; x++) {
            for (int z = -maxRadius; z <= maxRadius; z++) {
                BlockPos floorPos = center.offset(x, -1, z);

                // Пропускаем позицию прямо под Анализатором
                if (x == 0 && z == 0) continue;

                // Считаем квадрат расстояния от центра
                double distSq = x * x + z * z;

                // Если слишком далеко - пропускаем
                if (distSq > maxRadius * maxRadius) continue;

                // Если мы на границе (между базовым и максимальным радиусом),
                // добавляем случайность: чем дальше от центра, тем меньше шанс спавна
                if (distSq > baseRadiusSq) {
                    double chance = 1.0 - ((Math.sqrt(distSq) - 2.5) / (maxRadius - 2.5));
                    if (random.nextDouble() > chance) {
                        continue; // Пропускаем этот блок, создавая "рваный" край
                    }
                }

                // Заменяем блок поверхности на GRASS_BLOCK
                serverLevel.setBlockAndUpdate(floorPos, Blocks.GRASS_BLOCK.defaultBlockState());

                // Заполняем цветами и травой (тоже с уменьшением плотности к краям)
                BlockPos aboveFloor = floorPos.above();
                if (serverLevel.isEmptyBlock(aboveFloor) && !aboveFloor.equals(center)) {
                    int chance = random.nextInt(100);
                    if (chance < 40) {
                        serverLevel.setBlockAndUpdate(aboveFloor, Blocks.SHORT_GRASS.defaultBlockState());
                    } else if (chance < 60) {
                        serverLevel.setBlockAndUpdate(aboveFloor, Blocks.POPPY.defaultBlockState());
                    } else if (chance < 75) {
                        serverLevel.setBlockAndUpdate(aboveFloor, Blocks.DANDELION.defaultBlockState());
                    }
                }
            }
        }

        // 2. ВЫБИРАЕМ СМЕЩЕНИЕ ДЛЯ СТВОЛА (на 2 блока в сторону от Анализатора)
        BlockPos treeBase = center.east(2); // По умолчанию 2 блока на восток

        // 3. СТРОИМ ДУБ В ЛОБ (Прямой спавн блоков)
        int height = 4 + random.nextInt(2); // Высота ствола 4-5 блоков

        // А) Строим ствол из дубовых бревен
        for (int h = 0; h < height; h++) {
            serverLevel.setBlockAndUpdate(treeBase.above(h), Blocks.OAK_LOG.defaultBlockState());
        }

        // Б) Генерируем крону из листвы (свойства PERSISTENT чтобы листва не осыпалась)
        BlockState leafState = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        BlockPos leafCenter = treeBase.above(height - 1);

        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    // Формируем скруглённую шапку листвы
                    if (Math.abs(x) == 2 && Math.abs(z) == 2 && (y == -1 || y == 2)) continue;

                    BlockPos leafPos = leafCenter.offset(x, y, z);

                    // Не ломаем ствол листвой и не перекрываем Анализатор
                    if (serverLevel.isEmptyBlock(leafPos)) {
                        serverLevel.setBlockAndUpdate(leafPos, leafState);
                    }
                }
            }
        }

        // 4. ЭФФЕКТЫ
        serverLevel.playSound(null, treeBase, SoundEvents.CHORUS_FLOWER_GROW, SoundSource.BLOCKS, 1.0F, 0.8F);
        serverLevel.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                treeBase.getX() + 0.5D, treeBase.getY() + 1.0D, treeBase.getZ() + 0.5D,
                30, 1.0D, 1.0D, 1.0D, 0.05D
        );
    }
}