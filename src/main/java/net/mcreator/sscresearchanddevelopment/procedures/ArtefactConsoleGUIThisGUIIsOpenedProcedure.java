package net.mcreator.sscresearchanddevelopment.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;

import java.util.Comparator;

public class ArtefactConsoleGUIThisGUIIsOpenedProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		Entity temp_entity = null;
		ItemStack found_item = ItemStack.EMPTY;
		{
			final Vec3 _center = new Vec3(x, y, z);
			for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
				if (entityiterator instanceof Display.ItemDisplay) {
					temp_entity = entityiterator;
					SscResearchAndDevelopmentMod.LOGGER.info(temp_entity);
				}
			}
		}
	}
}