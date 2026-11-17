package net.nezerit6.animated_arsenal.client.render;

import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.nezerit6.animated_arsenal.item.AttachmentItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class AttachmentRenderer extends GeoItemRenderer<AttachmentItem>
{
    public AttachmentRenderer()
    {
        super(new GeoModel<>()
        {
            @Override
            public ResourceLocation getModelResource(AttachmentItem item)
            {
                return ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "geo/attach_" + item.attachTypeString() + "_" + item.getId() + ".geo.json");
            }

            @Override
            public ResourceLocation getTextureResource(AttachmentItem item)
            {
                return ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "textures/misc/attach_" + item.attachTypeString() + "_" + item.getId() + ".png");
            }

            @Override
            public ResourceLocation getAnimationResource(AttachmentItem item)
            {
                return ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "animations/attachment.animation.json");
            }
        });
    }
}