package net.mcreator.sscresearchanddevelopment.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

import java.util.Comparator;

public class ArtefactConsoleGUIModelProcedure {
	public static Entity execute(LevelAccessor world, double x, double y, double z) {
		Entity temp_entity = null;
		{
			final Vec3 _center = new Vec3(x, y, z);
			for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
				if (entityiterator instanceof Player) {
					temp_entity = entityiterator;
					break;
				}
			}
		}
		return temp_entity;
	}
}