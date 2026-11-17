package net.nezerit6.animated_arsenal.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.math.Axis;
import net.nezerit6.animated_arsenal.client.GunArmAnimations;
import net.nezerit6.animated_arsenal.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin
{
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void animated_arsenal$hideSlungGun(LivingEntity entity, ItemStack stack, ItemDisplayContext context, HumanoidArm arm,
                                        PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci)
    {
        if (stack == entity.getMainHandItem() && GunArmAnimations.isSlungOnBack(entity, entity.tickCount))
        {
            ci.cancel();
        }
    }

    @WrapOperation(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void animated_arsenal$bashTurn(ItemInHandRenderer renderer, LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand,
                                    PoseStack poseStack, MultiBufferSource buffer, int light, Operation<Void> original)
    {
        if (stack.getItem() instanceof GunItem gun && stack == entity.getMainHandItem())
        {
            float strike = GunArmAnimations.meleeStrike(GunArmAnimations.meleeProgress(entity, entity.tickCount + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false)));
            if (strike > 0.0F)
            {
                poseStack.pushPose();
                float side = entity.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT ? 1.0F : -1.0F;

                float angle = (!gun.isLongGun() ? 75.0F : 90.0F) * strike * side;
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));
                poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
                original.call(renderer, entity, stack, context, leftHand, poseStack, buffer, light);
                poseStack.popPose();
                return;
            }
        }
        original.call(renderer, entity, stack, context, leftHand, poseStack, buffer, light);
    }
}