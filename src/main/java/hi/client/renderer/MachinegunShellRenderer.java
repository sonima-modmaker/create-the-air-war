package hi.client.renderer;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import hi.client.model.MachinegunShellModel;
import hi.entity.MachinegunShellEntity;
import hi.CreateTheAirWarsMod;

@OnlyIn(Dist.CLIENT)
public class MachinegunShellRenderer extends EntityRenderer<MachinegunShellEntity> {
	private static final ResourceLocation TEXTURE = net.minecraft.resources.ResourceLocation.parse("create_the_air_wars:textures/entity/shell.png");
	private final MachinegunShellModel<MachinegunShellEntity> model;

	public MachinegunShellRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new MachinegunShellModel<>(context.bakeLayer(MachinegunShellModel.LAYER_LOCATION));
	}

	@Override
	public void render(MachinegunShellEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(180f - entityYaw));
		poseStack.mulPose(Axis.ZP.rotationDegrees(-entity.getXRot()));
		poseStack.translate(0.0, -0.7, 0.0);
		poseStack.scale(1.0f, 1.0f, 1.0f);
		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
		this.model.renderToBuffer(poseStack, vertexConsumer, 0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		poseStack.popPose();
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(MachinegunShellEntity entity) {
		return TEXTURE;
	}
}
