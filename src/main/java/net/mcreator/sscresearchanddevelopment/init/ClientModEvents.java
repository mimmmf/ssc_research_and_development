package net.mcreator.sscresearchanddevelopment.init;

import net.mcreator.sscresearchanddevelopment.client.Renderer.ArtefactanalyzerRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                SscResearchAndDevelopmentModBlockEntities.ARTEFACTANALYZER.get(),
                ArtefactanalyzerRenderer::new
        );
    }
}