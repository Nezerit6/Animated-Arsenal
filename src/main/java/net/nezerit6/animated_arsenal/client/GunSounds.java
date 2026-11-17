package net.nezerit6.animated_arsenal.client;

import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.nezerit6.animated_arsenal.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.keyframe.event.data.SoundKeyframeData;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.loading.object.BakedAnimations;

import java.util.ArrayList;
import java.util.List;

public final class GunSounds
{
    private record Pending(long instanceId, int holderId, SoundEvent sound, double playAt) {}

    private static final List<Pending> PENDING = new ArrayList<>();
    private static double clientTicks = 0;

    private GunSounds() {}

    public static void onAnimation(GunItem gun, long instanceId, String animationName, int holderId)
    {
        PENDING.removeIf(p -> p.instanceId() == instanceId);

        ResourceLocation file = ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "animations/" + gun.getAnimationID() + ".animation.json");
        BakedAnimations animations = GeckoLibCache.getBakedAnimations().get(file);
        if (animations == null) return;

        Animation animation = animations.getAnimation(animationName);
        if (animation == null || animation.keyFrames() == null) return;

        for (SoundKeyframeData keyframe : animation.keyFrames().sounds())
        {
            SoundEvent sound = gun.soundForKeyframe(keyframe.getSound());
            if (sound != null)
            {
                PENDING.add(new Pending(instanceId, holderId, sound, clientTicks + keyframe.getStartTick()));
            }
        }
    }

    public static void tick()
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
        {
            PENDING.clear();
            return;
        }

        clientTicks++;
        if (PENDING.isEmpty()) return;

        PENDING.removeIf(p ->
        {
            if (p.playAt() > clientTicks) return false;

            Entity holder = mc.level.getEntity(p.holderId());
            if (holder != null)
            {
                mc.level.playLocalSound(holder.getX(), holder.getY(), holder.getZ(), p.sound(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
            }
            return true;
        });
    }
}