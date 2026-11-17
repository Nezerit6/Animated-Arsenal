package net.nezerit6.animated_arsenal.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.nezerit6.animated_arsenal.item.GunItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin
{
    @Inject(method = "resetAttackStrengthTicker", at = @At("HEAD"), cancellable = true)
    private void animated_arsenal$dontResetIfGun(CallbackInfo ci)
    {
        if (((Player) (Object) this).getMainHandItem().getItem() instanceof GunItem) ci.cancel();
    }

    @WrapWithCondition(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private boolean animated_arsenal$noSwingWithGun(Player player, InteractionHand hand)
    {
        return !(player.getMainHandItem().getItem() instanceof GunItem);
    }
}