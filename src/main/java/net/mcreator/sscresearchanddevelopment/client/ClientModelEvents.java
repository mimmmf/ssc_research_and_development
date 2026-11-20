package net.mcreator.sscresearchanddevelopment.client;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.client.Renderer.AnomalyRenderer;
import net.mcreator.sscresearchanddevelopment.client.Renderer.PortalRenderer;
import net.mcreator.sscresearchanddevelopment.client.Renderer.entity.AnomalyModel;
import net.mcreator.sscresearchanddevelopment.client.Renderer.entity.PortalModel;
import net.mcreator.sscresearchanddevelopment.entity.PortalEntity;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid = SscResearchAndDevelopmentMod.MODID, value = Dist.CLIENT)
public class ClientModelEvents {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PortalModel.LAYER, PortalModel::createBodyLayer);
        event.registerLayerDefinition(AnomalyModel.LAYER, AnomalyModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        registerPortalRenderer(event, SscResearchAndDevelopmentModEntities.PORTAL_BLUE, "portal_blue");
        registerPortalRenderer(event, SscResearchAndDevelopmentModEntities.PORTAL_RED, "portal_red");
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(SscResearchAndDevelopmentMod.MODID, "textures/entity/anom1_texture.png");
        event.registerEntityRenderer(SscResearchAndDevelopmentModEntities.ANOMALY.get(), context -> new AnomalyRenderer(context, texture));
    }

    private static void registerPortalRenderer(EntityRenderersEvent.RegisterRenderers event,
                                               DeferredHolder<EntityType<?>, EntityType<PortalEntity>> type,
                                               String name) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                SscResearchAndDevelopmentMod.MODID, "textures/entity/" + name + ".png");
        event.registerEntityRenderer(type.get(),
                context -> new PortalRenderer(context, texture));
    }
}