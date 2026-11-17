package net.nezerit6.animated_arsenal.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.nezerit6.animated_arsenal.client.DynamicLights;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public class LevelRendererLightMixin
{
    @ModifyReturnValue(method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I", at = @At("RETURN"))
    private static int animated_arsenal$dynamicLight(int packed, BlockAndTintGetter level, BlockState state, BlockPos pos)
    {
        if (!DynamicLights.isActive() || packed == 15728880) return packed;

        double dynamic = DynamicLights.lightAt(pos);
        if (dynamic <= 0.0) return packed;

        int blockLight = packed & 0xFFFF;
        int dynamicLight = Math.min(240, (int) Math.ceil(dynamic * 16.0));
        if (dynamicLight <= blockLight) return packed;

        return (packed & 0xFFFF0000) | dynamicLight;
    }
}