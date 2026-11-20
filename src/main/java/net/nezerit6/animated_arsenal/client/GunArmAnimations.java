package net.nezerit6.animated_arsenal.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.HashMap;
import java.util.Map;

public final class GunArmAnimations
{
    public static final float RECOIL_TICKS = 4.0F;

    public static final float FIRING_POSE_TICKS = 12.0F;

    public static final float MUZZLE_FLASH_TICKS = 1.5F;

    public static final float MELEE_TICKS = 10.0F;

    private static final Map<Integer, Integer> LAST_SHOT_TICK = new HashMap<>();
    private static final Map<Integer, Integer> LAST_MELEE_TICK = new HashMap<>();

    private static final Map<Long, Long> LAST_SHOT_BY_GUN = new HashMap<>();

    private GunArmAnimations() {}

    public static final float AIM_RELEASE_TICKS = 5.0F;
    private static final Map<Integer, Float> LAST_AIM = new HashMap<>();

    public static float aimWeight(Entity entity, boolean aiming, float ageInTicks)
    {
        if (ageInTicks <= 0.0F) return aiming ? 1.0F : 0.0F;
        if (aiming)
        {
            LAST_AIM.put(entity.getId(), ageInTicks);
            if (LAST_AIM.size() > 256) LAST_AIM.clear();
            return 1.0F;
        }
        Float last = LAST_AIM.get(entity.getId());
        if (last == null) return 0.0F;
        float since = ageInTicks - last;
        if (since < 0.0F || since >= AIM_RELEASE_TICKS)
        {
            LAST_AIM.remove(entity.getId());
            return 0.0F;
        }
        float t = 1.0F - since / AIM_RELEASE_TICKS;
        return t * t * (3.0F - 2.0F * t);
    }

    public static void onShot(Entity shooter)
    {
        LAST_SHOT_TICK.put(shooter.getId(), shooter.tickCount);
        if (LAST_SHOT_TICK.size() > 256) LAST_SHOT_TICK.clear();

        if (shooter instanceof LivingEntity living)
        {
            long gunId = GeoItem.getId(living.getMainHandItem());
            if (gunId != Long.MAX_VALUE)
            {
                LAST_SHOT_BY_GUN.put(gunId, shooter.level().getGameTime());
                if (LAST_SHOT_BY_GUN.size() > 256) LAST_SHOT_BY_GUN.clear();
            }
        }
    }

    private static float ticksSinceShot(Entity entity, float ageInTicks)
    {
        Integer shotTick = LAST_SHOT_TICK.get(entity.getId());
        return shotTick == null ? Float.MAX_VALUE : ageInTicks - shotTick;
    }

    public static float recoil(Entity entity, float ageInTicks)
    {
        float t = ticksSinceShot(entity, ageInTicks) / RECOIL_TICKS;
        if (t < 0.0F || t >= 1.0F) return 0.0F;

        float f = 1.0F - t;
        return f * f;
    }

    public static boolean isInFiringPose(Entity entity, float ageInTicks)
    {
        float t = ticksSinceShot(entity, ageInTicks);
        return t >= 0.0F && t < FIRING_POSE_TICKS;
    }

    public static boolean showMuzzleFlash(long gunId, long gameTime, float partialTick)
    {
        Long shot = LAST_SHOT_BY_GUN.get(gunId);
        if (shot == null) return false;
        float t = (gameTime - shot) + partialTick;
        return t >= 0.0F && t < MUZZLE_FLASH_TICKS;
    }

    public static boolean isSlungOnBack(LivingEntity entity, float ageInTicks)
    {
        if (!(entity instanceof net.minecraft.world.entity.player.Player)) return false;
        if (!net.nezerit6.animated_arsenal.compat.CrawlCompat.isCrawling(entity)) return false;

        net.minecraft.world.item.ItemStack stack = entity.getMainHandItem();
        if (!(stack.getItem() instanceof net.nezerit6.animated_arsenal.item.GunItem gun) || !gun.isLongGun()) return false;

        return !net.nezerit6.animated_arsenal.util.GunData.getBoolean(stack, "isAiming")
                && !net.nezerit6.animated_arsenal.util.GunData.getBoolean(stack, "isReloading")
                && net.nezerit6.animated_arsenal.util.GunData.getInt(stack, "reloadTick") <= 0
                && !isInFiringPose(entity, ageInTicks)
                && meleeProgress(entity, ageInTicks) <= 0.0F;
    }

    public static void onMelee(Entity entity)
    {
        LAST_MELEE_TICK.put(entity.getId(), entity.tickCount);
        if (LAST_MELEE_TICK.size() > 256) LAST_MELEE_TICK.clear();
    }

    public static float meleeStrike(float progress)
    {
        if (progress <= 0.0F) return 0.0F;
        if (progress < 0.25F)
        {
            float y = progress / 0.25F - 1.0F;
            return 1.0F + 2.70158F * y * y * y + 1.70158F * y * y;
        }
        if (progress < 0.5F) return 1.0F;
        float t = (progress - 0.5F) / 0.5F;
        return 1.0F - t * t * (3.0F - 2.0F * t);
    }

    public static float meleeProgress(Entity entity, float ageInTicks)
    {
        Integer start = LAST_MELEE_TICK.get(entity.getId());
        if (start == null) return 0.0F;

        float t = (ageInTicks - start) / MELEE_TICKS;
        return t <= 0.0F || t >= 1.0F ? 0.0F : t;
    }
}