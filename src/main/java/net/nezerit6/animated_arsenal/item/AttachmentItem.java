package net.nezerit6.animated_arsenal.item;

import net.nezerit6.animated_arsenal.client.render.AttachmentRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class AttachmentItem extends Item implements GeoItem
{
    private final int id;
    private final float recoilMult;
    private final float spreadMult;
    private final AttachType attachType;
    private final boolean silencesGun;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AttachmentItem(Properties settings, int id, float recoilMult, float spreadMult, AttachType attachType, boolean silencesGun)
    {
        super(settings);
        this.id = id;
        this.recoilMult = recoilMult;
        this.spreadMult = spreadMult;
        this.attachType = attachType;
        this.silencesGun = silencesGun;
    }

    public int getId()
    {
        return this.id;
    }

    public float getRecoilMult()
    {
        return this.recoilMult;
    }

    public float getSpreadMult()
    {
        return this.spreadMult;
    }

    public AttachType getAttachType()
    {
        return this.attachType;
    }

    public boolean silencesGun()
    {
        return this.silencesGun;
    }

    public String attachTypeString()
    {
        return this.attachType.shortName();
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer)
    {
        consumer.accept(new GeoRenderProvider()
        {
            private AttachmentRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer()
            {
                if (this.renderer == null)
                    this.renderer = new AttachmentRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache()
    {
        return this.cache;
    }

    @Override
    public double getTick(Object o)
    {
        return 0;
    }

    public enum AttachType
    {
        SIGHT("si", "sightID"),
        GRIP("gr", "gripID"),
        MUZZLE("mz", "muzzleID");

        private final String shortName;
        private final String idKey;

        AttachType(String shortName, String idKey)
        {
            this.shortName = shortName;
            this.idKey = idKey;
        }

        public String shortName()
        {
            return this.shortName;
        }

        public String idKey()
        {
            return this.idKey;
        }
    }
}