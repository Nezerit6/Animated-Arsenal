package net.nezerit6.animated_arsenal.client.model;

import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.nezerit6.animated_arsenal.item.GunItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GunModel extends GeoModel<GunItem>
{
    @Override
    public ResourceLocation getModelResource(GunItem object)
    {
        return ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "geo/" + object.getID() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GunItem object)
    {
        return ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "textures/item/" + object.getID() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(GunItem animatable)
    {
        return ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "animations/" + animatable.getAnimationID() + ".animation.json");
    }
}