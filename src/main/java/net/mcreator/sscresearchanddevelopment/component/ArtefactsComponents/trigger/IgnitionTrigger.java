package net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.trigger;

import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactContext;
import net.mcreator.sscresearchanddevelopment.component.ArtefactsComponents.ArtefactTrigger;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.ArtefactNode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class IgnitionTrigger extends ArtefactTrigger {

    private static final ResourceLocation WELDER_ID = ResourceLocation.fromNamespaceAndPath("ssc_14", "welding");

    @Override
    public boolean check(ArtefactContext context, ArtefactNode node) {
        Object[] args = context.context();

        if (args.length > 0 && args[0] instanceof String eventType) {
            if ("item_on_fire".equals(eventType) || "player_on_fire".equals(eventType) || "welder".equals(eventType)) {
                return true;
            }
        }
        Entity entity = context.source();
        if (entity instanceof Player player) {
            ItemStack heldItem = player.getMainHandItem();
            if (!heldItem.isEmpty()) {
                ResourceLocation itemLoc = BuiltInRegistries.ITEM.getKey(heldItem.getItem());
                return WELDER_ID.equals(itemLoc);
            }
        }

        return false;
    }
}