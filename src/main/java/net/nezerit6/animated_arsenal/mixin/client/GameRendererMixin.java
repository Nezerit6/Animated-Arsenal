package net.nezerit6.animated_arsenal.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.nezerit6.animated_arsenal.item.GunItem;
import net.nezerit6.animated_arsenal.util.GunData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class GameRendererMixin
{
    @Shadow
    @Final
    Minecraft minecraft;

    @WrapOperation(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private void animated_arsenal$bobViewGun(GameRenderer instance, PoseStack poseStack, float partialTicks, Operation<Void> original)
    {
        Player player = this.minecraft.player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof GunItem))
        {
            original.call(instance, poseStack, partialTicks);
        }
        else if (!GunData.getBoolean(player.getMainHandItem(), "isAiming") && !player.isSprinting())
        {
            animated_arsenal$bobView(poseStack, partialTicks, 0.25f);
        }
    }

    @Unique
    private void animated_arsenal$bobView(PoseStack poseStack, float partialTicks, float multiplier)
    {
        if (!(this.minecraft.getCameraEntity() instanceof Player player)) return;

        float f = player.walkDist - player.walkDistO;
        float g = -(player.walkDist + f * partialTicks);
        float h = Mth.lerp(partialTicks, player.oBob, player.bob) * multiplier;
        poseStack.translate(Mth.sin(g * (float) Math.PI) * h * 0.5F, -Math.abs(Mth.cos(g * (float) Math.PI) * h), 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(g * (float) Math.PI) * h * 3.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(g * (float) Math.PI - 0.2F) * h) * 5.0F));
    }
}