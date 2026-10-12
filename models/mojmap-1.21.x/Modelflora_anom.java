// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

public class Modelflora_anom<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation("modid", "flora_anom"), "main");
	private final ModelPart Leaf;
	private final ModelPart Leaf2;
	private final ModelPart Leaf3;
	private final ModelPart Leaf4;
	private final ModelPart Leaf5;
	private final ModelPart Leaf6;
	private final ModelPart Leaf7;
	private final ModelPart Leaf8;
	private final ModelPart bb_main;

	public Modelflora_anom(ModelPart root) {
		this.Leaf = root.getChild("Leaf");
		this.Leaf2 = root.getChild("Leaf2");
		this.Leaf3 = root.getChild("Leaf3");
		this.Leaf4 = root.getChild("Leaf4");
		this.Leaf5 = root.getChild("Leaf5");
		this.Leaf6 = root.getChild("Leaf6");
		this.Leaf7 = root.getChild("Leaf7");
		this.Leaf8 = root.getChild("Leaf8");
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Leaf = partdefinition.addOrReplaceChild("Leaf", CubeListBuilder.create().texOffs(24, 26).addBox(
				-2.0F, -1.0F, -5.0F, 4.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 5.0F));

		PartDefinition cube_r1 = Leaf
				.addOrReplaceChild("cube_r1",
						CubeListBuilder.create().texOffs(16, 28).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 2.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, -2.0F, 5.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r2 = Leaf
				.addOrReplaceChild("cube_r2",
						CubeListBuilder.create().texOffs(24, 20).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 1.0F, 5.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

		PartDefinition Leaf2 = partdefinition.addOrReplaceChild("Leaf2",
				CubeListBuilder.create().texOffs(24, 26).addBox(-2.0F, -1.0F, -5.0F, 4.0F, 1.0F, 5.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-5.0F, 24.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r3 = Leaf2
				.addOrReplaceChild("cube_r3",
						CubeListBuilder.create().texOffs(16, 28).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 2.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, -2.0F, 5.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r4 = Leaf2
				.addOrReplaceChild("cube_r4",
						CubeListBuilder.create().texOffs(24, 20).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 1.0F, 5.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

		PartDefinition Leaf3 = partdefinition.addOrReplaceChild("Leaf3",
				CubeListBuilder.create().texOffs(24, 26).addBox(-2.0F, -1.0F, -5.0F, 4.0F, 1.0F, 5.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 24.0F, -5.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r5 = Leaf3
				.addOrReplaceChild("cube_r5",
						CubeListBuilder.create().texOffs(16, 28).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 2.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, -2.0F, 5.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r6 = Leaf3
				.addOrReplaceChild("cube_r6",
						CubeListBuilder.create().texOffs(24, 20).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 1.0F, 5.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

		PartDefinition Leaf4 = partdefinition
				.addOrReplaceChild("Leaf4",
						CubeListBuilder.create().texOffs(24, 26).addBox(-2.0F, -1.0F, -5.0F, 4.0F, 1.0F, 5.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(5.0F, 24.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r7 = Leaf4
				.addOrReplaceChild("cube_r7",
						CubeListBuilder.create().texOffs(16, 28).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 2.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, -2.0F, 5.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r8 = Leaf4
				.addOrReplaceChild("cube_r8",
						CubeListBuilder.create().texOffs(24, 20).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 1.0F, 5.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

		PartDefinition Leaf5 = partdefinition.addOrReplaceChild("Leaf5", CubeListBuilder.create(),
				PartPose.offset(-4.0F, 22.0F, 4.0F));

		PartDefinition cube_r9 = Leaf5.addOrReplaceChild("cube_r9",
				CubeListBuilder.create().texOffs(24, 14).addBox(-2.0F, -1.0F, -3.0F, 5.0F, 1.0F, 5.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.3927F));

		PartDefinition Leaf6 = partdefinition.addOrReplaceChild("Leaf6", CubeListBuilder.create(),
				PartPose.offsetAndRotation(-4.0F, 22.0F, -4.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r10 = Leaf6.addOrReplaceChild("cube_r10",
				CubeListBuilder.create().texOffs(24, 14).addBox(-2.0F, -1.0F, -3.0F, 5.0F, 1.0F, 5.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.3927F));

		PartDefinition Leaf7 = partdefinition.addOrReplaceChild("Leaf7", CubeListBuilder.create(),
				PartPose.offsetAndRotation(4.0F, 22.0F, -4.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cube_r11 = Leaf7.addOrReplaceChild("cube_r11",
				CubeListBuilder.create().texOffs(24, 14).addBox(-2.0F, -1.0F, -3.0F, 5.0F, 1.0F, 5.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.3927F));

		PartDefinition Leaf8 = partdefinition.addOrReplaceChild("Leaf8", CubeListBuilder.create(),
				PartPose.offsetAndRotation(4.0F, 22.0F, 4.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r12 = Leaf8.addOrReplaceChild("cube_r12",
				CubeListBuilder.create().texOffs(24, 14).addBox(-2.0F, -1.0F, -3.0F, 5.0F, 1.0F, 5.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.3927F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main",
				CubeListBuilder.create().texOffs(0, 14)
						.addBox(-3.0F, -2.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(0, 34)
						.addBox(-3.0F, -9.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 21)
						.addBox(-3.0F, -11.0F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 28)
						.addBox(-2.0F, -10.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		Leaf.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Leaf2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Leaf3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Leaf4.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Leaf5.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Leaf6.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Leaf7.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		Leaf8.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
	}
}