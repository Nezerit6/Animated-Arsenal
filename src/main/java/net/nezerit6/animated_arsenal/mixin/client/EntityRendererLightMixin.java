package net.nezerit6.animated_arsenal.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.nezerit6.animated_arsenal.client.DynamicLights;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class EntityRendererLightMixin<T extends Entity>
{
    @ModifyReturnValue(method = "getPackedLightCoords", at = @At("RETURN"))
    private int animated_arsenal$dynamicLight(int packed, T entity, float partialTicks)
    {
        if (!DynamicLights.isActive()) return packed;

        int blockLight = LightTexture.block(packed);
        if (blockLight >= 15) return packed;

        BlockPos pos = BlockPos.containing(entity.getLightProbePosition(partialTicks));
        int dynamic = (int) Math.round(DynamicLights.lightAt(pos));
        return dynamic > blockLight ? LightTexture.pack(dynamic, LightTexture.sky(packed)) : packed;
    }
}