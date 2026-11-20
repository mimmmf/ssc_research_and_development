package net.mcreator.sscresearchanddevelopment.init;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.entity.AnomalyEntity;
import net.mcreator.sscresearchanddevelopment.entity.PortalEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SscResearchAndDevelopmentModEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY =
            DeferredRegister.create(Registries.ENTITY_TYPE, SscResearchAndDevelopmentMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<PortalEntity>> PORTAL_BLUE =
            registerPortal("portal_blue");

    public static final DeferredHolder<EntityType<?>, EntityType<PortalEntity>> PORTAL_RED =
            registerPortal("portal_red");
    public static final DeferredHolder<EntityType<?>, EntityType<AnomalyEntity>> ANOMALY = REGISTRY.register("anomaly", () ->
            EntityType.Builder.<AnomalyEntity>of(AnomalyEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            ResourceLocation.fromNamespaceAndPath(SscResearchAndDevelopmentMod.MODID, "anomaly"))));

    private static DeferredHolder<EntityType<?>, EntityType<PortalEntity>> registerPortal(String name) {
        return REGISTRY.register(name, () ->
                EntityType.Builder.<PortalEntity>of(PortalEntity::new, MobCategory.MISC)
                        .sized(1.0F, 2.0F)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE,
                                ResourceLocation.fromNamespaceAndPath(SscResearchAndDevelopmentMod.MODID, name))));
    }
}