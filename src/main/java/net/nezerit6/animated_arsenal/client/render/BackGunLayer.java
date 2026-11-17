package net.nezerit6.animated_arsenal.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.nezerit6.animated_arsenal.client.GunArmAnimations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix3f;
import org.joml.Quaternionf;

public class BackGunLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
    private static final float INV_SQRT2 = (float) (1.0 / Math.sqrt(2.0));

    private static final Quaternionf SLUNG = new Quaternionf().setFromNormalized(new Matrix3f(
            0.0F, 0.0F, 1.0F,
            INV_SQRT2, INV_SQRT2, 0.0F,
            -INV_SQRT2, INV_SQRT2, 0.0F));

    public BackGunLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent)
    {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if (!GunArmAnimations.isSlungOnBack(player, ageInTicks)) return;

        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);

        poseStack.translate(0.0F, 0.4F, 0.19F);
        poseStack.mulPose(SLUNG);
        Minecraft.getInstance().getItemRenderer().renderStatic(player, player.getMainHandItem(), ItemDisplayContext.NONE, false,
                poseStack, buffer, player.level(), packedLight, OverlayTexture.NO_OVERLAY, player.getId());
        poseStack.popPose();
    }
}