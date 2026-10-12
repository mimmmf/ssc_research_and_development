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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ResourceSpawnReaction extends ArtefactReaction {

    private final List<String> itemIds;
    private final int minTotalCount;
    private final int maxTotalCount;

    public ResourceSpawnReaction(List<String> itemIds, int minTotalCount, int maxTotalCount) {
        this.itemIds = itemIds;
        this.minTotalCount = minTotalCount;
        this.maxTotalCount = maxTotalCount;
    }

    @Override
    public void execute(ArtefactContext context, ArtefactNode node) {
        Level level = context.level();
        if (!(level instanceof ServerLevel serverLevel) || itemIds.isEmpty()) return;

        RandomSource random = serverLevel.getRandom();

        // 1. Вычисляем общее количество предметов (целевую сумму), которое вылетит из артефакта
        int totalItems = minTotalCount >= maxTotalCount
                ? minTotalCount
                : random.nextInt(minTotalCount, maxTotalCount + 1);

        BlockPos pos = context.pos();
        double spawnX = pos.getX() + 0.5D;
        double spawnY = pos.getY() + 0.5D;
        double spawnZ = pos.getZ() + 0.5D;

        // 2. Циклом выбираем случайный предмет на каждый "шаг бюджета"
        for (int i = 0; i < totalItems; i++) {
            String selectedId = itemIds.get(random.nextInt(itemIds.size()));
            ResourceLocation itemLoc = parseLocation(selectedId);
            Item item = BuiltInRegistries.ITEM.get(itemLoc)
                    .map(Holder.Reference::value)
                    .orElse(Items.AIR);

            if (item == Items.AIR) continue;

            // Спавним предметы по 1 штуке (каждый со своей дугой вылета)
            ItemStack resultStack = new ItemStack(item, 1);
            ItemEntity itemEntity = new ItemEntity(serverLevel, spawnX, spawnY, spawnZ, resultStack);

            // --- Индивидуальный вектор вылета для каждого предмета ---
            double speed = 0.2D + random.nextDouble() * 0.15D;
            double angle = random.nextDouble() * Math.PI * 2.0D; // Случайный угол вокруг артефакта

            double vx = Math.cos(angle) * speed;
            double vy = 0.25D + random.nextDouble() * 0.25D; // Импульс вверх
            double vz = Math.sin(angle) * speed;

            itemEntity.setDeltaMovement(new Vec3(vx, vy, vz));
            itemEntity.setDefaultPickUpDelay(); // Задержка перед подбором (2 сек)

            serverLevel.addFreshEntity(itemEntity);
        }

        // --- Эффекты для всей вспышки ресурсов ---
        serverLevel.playSound(
                null, pos,
                SoundEvents.ITEM_PICKUP,
                SoundSource.BLOCKS,
                1.0F,
                0.6F + random.nextFloat() * 0.4F
        );

        serverLevel.sendParticles(
                ParticleTypes.POOF,
                spawnX, spawnY, spawnZ,
                12,
                0.15D, 0.15D, 0.15D,
                0.05D
        );
    }

    private ResourceLocation parseLocation(String id) {
        return id.contains(":") ? ResourceLocation.parse(id) : ResourceLocation.fromNamespaceAndPath(ArtefactRegistries.MODID, id);
    }
}