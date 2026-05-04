package net.mcreator.sscresearchanddevelopment.component;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class ArtefactLogic {
    public static final Map<String, ArtefactContext.IArtefactTrigger> TRIGGERS = new HashMap<>();
    public static final Map<String, ArtefactContext.IArtefactReaction> REACTIONS = new HashMap<>();

    public static void init() {
        // РЕАКЦИИ
        REACTIONS.put("none", (ctx, node) -> {});
        REACTIONS.put("explosion", (ctx, node) -> {
            ctx.level().explode(null, ctx.pos().getX(), ctx.pos().getY(), ctx.pos().getZ(), 2.0F, Level.ExplosionInteraction.BLOCK);
        });
        REACTIONS.put("spawn_mob", (ctx, node) -> {
            EntityType.ZOMBIE.spawn((ServerLevel)ctx.level(), ctx.pos(), EntitySpawnReason.EVENT);
        });

        // ТРИГГЕРЫ
        TRIGGERS.put("timer", (ctx, node, args) -> ctx.level().getGameTime() % 200 == 0);
        TRIGGERS.put("hit", (ctx, node, args) -> args.length > 0 && args[0] instanceof String s && s.equals("attack"));
        TRIGGERS.put("heat", (ctx, node, args) -> ctx.level().getBiome(ctx.pos()).value().getBaseTemperature() > 1.0f);
    }
}