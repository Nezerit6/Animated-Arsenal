package net.nezerit6.animated_arsenal.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.nezerit6.animated_arsenal.item.GunItem;
import net.nezerit6.animated_arsenal.util.GunData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin
{
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyExpressionValue(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isScoping()Z"))
    private boolean animated_arsenal$ifScopedGun(boolean original)
    {
        if (original || this.minecraft.player == null) return original;
        ItemStack stack = this.minecraft.player.getMainHandItem();
        return stack.getItem() instanceof GunItem gun && gun.isScoped() && GunData.getBoolean(stack, "isAiming");
    }
}