package net.mcreator.sscresearchanddevelopment.client.Renderer.entity;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceLocation;

public class PortalModel extends EntityModel<EntityRenderState> {
	public static final ModelLayerLocation LAYER =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(
					SscResearchAndDevelopmentMod.MODID, "portal"), "main");

	public PortalModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition partdefinition = mesh.getRoot();
		partdefinition.addOrReplaceChild("main",
				CubeListBuilder.create().texOffs(0, 0)
						.addBox(-16.0F, -32.0F, 0.0F, 32.0F, 32.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		return LayerDefinition.create(mesh, 32, 32);
	}
}