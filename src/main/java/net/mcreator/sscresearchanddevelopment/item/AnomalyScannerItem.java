package net.mcreator.sscresearchanddevelopment.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class AnomalyScannerItem extends Item {
    public AnomalyScannerItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }
}
