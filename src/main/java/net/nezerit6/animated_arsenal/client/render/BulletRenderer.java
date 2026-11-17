package net.nezerit6.animated_arsenal.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.nezerit6.animated_arsenal.entity.projectile.BulletProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class BulletRenderer extends EntityRenderer<BulletProjectileEntity>
{
    public BulletRenderer(EntityRendererProvider.Context ctx)
    {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(BulletProjectileEntity entity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(BulletProjectileEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light)
    {
    }
}