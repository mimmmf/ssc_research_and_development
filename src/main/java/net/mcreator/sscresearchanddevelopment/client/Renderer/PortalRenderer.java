package net.mcreator.sscresearchanddevelopment.client.Renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mcreator.sscresearchanddevelopment.client.Renderer.entity.PortalModel;
import net.mcreator.sscresearchanddevelopment.entity.PortalEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class PortalRenderer extends EntityRenderer<PortalEntity, EntityRenderState> {
    private final ResourceLocation texture;
    private final PortalModel model;

    public PortalRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
        super(context);
        this.texture = texture;
        this.model = new PortalModel(context.bakeLayer(PortalModel.LAYER));
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void render(EntityRenderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float yaw = this.entityRenderDispatcher.camera.getYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.translate(0.0, -1.5, 0.0);

        int fullBright = LightTexture.pack(15, 15);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(texture));
        this.model.root().render(poseStack, consumer, fullBright, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }
}