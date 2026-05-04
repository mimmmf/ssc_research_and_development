package net.mcreator.sscresearchanddevelopment.client.Renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactanalyzerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ArtefactanalyzerRenderer implements BlockEntityRenderer<ArtefactanalyzerBlockEntity> {
    public ArtefactanalyzerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ArtefactanalyzerBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, Vec3 vec3) {
        ItemStack item = be.getItem(0);
//        SscResearchAndDevelopmentMod.LOGGER.debug(be);
        if (item.isEmpty()) return;

        poseStack.pushPose();

        // 1. Центрируем и поднимаем предмет над блоком
        // 0.5f - центр блока, 0.6f - высота над поверхностью
        poseStack.translate(0.5f, 1f, 0.5f);

        // 2. Добавляем анимацию вращения и легкого парения
        float time = be.getLevel().getGameTime() + partialTick;
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.0f)); // Вращение
        float bobbing = Mth.sin(time * 0.1f) * 0.05f; // Парение вверх-вниз
        poseStack.translate(0, bobbing, 0);

        // 3. Масштабируем предмет
        poseStack.scale(1f, 1f, 1f);

        // 4. Отрисовываем
        Minecraft.getInstance().getItemRenderer().renderStatic(
                item,
                ItemDisplayContext.FIXED,
                combinedLight,
                combinedOverlay,
                poseStack,
                buffer,
                be.getLevel(),
                0
        );

        poseStack.popPose();
    }
}