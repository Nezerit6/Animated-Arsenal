package net.nezerit6.animated_arsenal.mixin.client;

import net.nezerit6.animated_arsenal.client.GunArmAnimations;
import net.nezerit6.animated_arsenal.item.GunItem;
import net.nezerit6.animated_arsenal.util.GunData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin
{
    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private static void animated_arsenal$gunPose(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !(stack.getItem() instanceof GunItem gun)) return;

        if (GunData.getInt(stack, "reloadTick") > 0)
        {
            cir.setReturnValue(HumanoidModel.ArmPose.CROSSBOW_CHARGE);
        }
        else if (GunItem.isLoaded(stack) && !player.isSprinting())
        {
            cir.setReturnValue(gun.getArmType() == GunItem.ArmType.HANDGUN_ONEHAND ? HumanoidModel.ArmPose.ITEM : HumanoidModel.ArmPose.BOW_AND_ARROW);
        }
    }
}