package net.mcreator.sscresearchanddevelopment.client.Renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mcreator.sscresearchanddevelopment.client.Renderer.entity.AnomalyModel;
import net.mcreator.sscresearchanddevelopment.entity.AnomalyEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class AnomalyRenderer extends EntityRenderer<AnomalyEntity, AnomalyRenderer.AnomalyRenderState> {
    private final ResourceLocation texture;
    private final AnomalyModel model;

    public AnomalyRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
        super(context);
        this.texture = texture;
        this.model = new AnomalyModel(context.bakeLayer(AnomalyModel.LAYER));
    }

    @Override
    public AnomalyRenderState createRenderState() {
        return new AnomalyRenderState();
    }

    @Override
    public void extractRenderState(AnomalyEntity entity, AnomalyRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.ageInTicks = entity.tickCount + partialTick;
    }

    @Override
    public void render(AnomalyRenderState state, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        this.model.setupAnim(state);

        poseStack.pushPose();

        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180.0F));

        float floatOffset = (float) Math.sin(state.ageInTicks * 0.05F) * 0.1F;
        poseStack.translate(0.0F, floatOffset, 0.0F);

        poseStack.scale(1.0F, -1.0F, 1.0F);

        var vertexConsumer = bufferSource.getBuffer(this.model.renderType(this.texture));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
        super.render(state, poseStack, bufferSource, packedLight);
    }
    public static class AnomalyRenderState extends EntityRenderState {
        public float ageInTicks;
    }
}