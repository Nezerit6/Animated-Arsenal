package net.nezerit6.animated_arsenal.client;

import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.animation.layered.modifier.MirrorModifier;
import com.zigythebird.playeranimcore.animation.layered.modifier.SpeedModifier;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.nezerit6.animated_arsenal.item.GunItem;
import net.nezerit6.animated_arsenal.util.GunData;

public final class PlayerAnims
{
    public static final ResourceLocation LAYER = id("player_anims");
    public static final ResourceLocation WAVE = id("wave");

    private static final int FADE_IN_TICKS = 6;
    private static final int FADE_OUT_TICKS = 8;

    private static ResourceLocation id(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, path);
    }

    private PlayerAnims() {}

    public static void registerLayer()
    {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER, 1500,
                player -> new PlayerAnimationController(player, (controller, state, setter) -> PlayState.STOP));
    }

    private static PlayerAnimationController controller(AbstractClientPlayer player)
    {
        return PlayerAnimationAccess.getPlayerAnimationLayer(player, LAYER) instanceof PlayerAnimationController controller ? controller : null;
    }

    public static boolean play(Entity entity, ResourceLocation animation, float speed)
    {
        if (!(entity instanceof AbstractClientPlayer player)) return false;
        RELOADING.remove(player.getId());
        PlayerAnimationController controller = controller(player);
        Animation anim = PlayerAnimResources.getAnimation(animation);
        if (controller == null || anim == null) return false;

        controller.removeModifierIf(m -> m instanceof SpeedModifier || m instanceof MirrorModifier);
        RawAnimation raw = RawAnimation.begin().thenPlay(anim);
        controller.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(FADE_IN_TICKS, EasingType.EASE_IN_OUT_SINE), raw, true);
        if (Math.abs(speed - 1.0F) > 0.01F) controller.addModifierLast(new SpeedModifier(speed));
        if (player.getMainArm() == HumanoidArm.LEFT)
        {
            MirrorModifier mirror = new MirrorModifier();
            mirror.enabled = true;
            controller.addModifierLast(mirror);
        }
        return true;
    }

    public static boolean isPlaying(Entity entity, ResourceLocation animation)
    {
        if (!(entity instanceof AbstractClientPlayer player)) return false;
        PlayerAnimationController controller = controller(player);
        if (controller == null || !controller.isActive()) return false;
        Animation current = controller.getCurrentAnimationInstance();
        return current != null && current == PlayerAnimResources.getAnimation(animation);
    }

    public static void stop(Entity entity)
    {
        if (!(entity instanceof AbstractClientPlayer player)) return;
        PlayerAnimationController controller = controller(player);
        if (controller == null || !controller.isActive()) return;
        controller.removeModifierIf(m -> m instanceof SpeedModifier);
        controller.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(FADE_OUT_TICKS, EasingType.EASE_IN_OUT_SINE),
                RawAnimation.begin().thenWait(FADE_OUT_TICKS + 2), false);
    }

    private static final java.util.Map<String, String> RELOAD_BY_GUN = java.util.Map.ofEntries(
            java.util.Map.entry("pistol_light", "reload_pistol"),
            java.util.Map.entry("pistol_heavy", "reload_pistol"),
            java.util.Map.entry("pistol_service", "reload_pistol"),
            java.util.Map.entry("smg_machinepistol", "reload_pistol"),
            java.util.Map.entry("revolver_magnum", "reload_revolver"),
            java.util.Map.entry("shotgun_doublebarrel", "reload_double"),
            java.util.Map.entry("lmg_m60", "reload_lmg"),
            java.util.Map.entry("lmg_minigun", "reload_minigun"));

    public static ResourceLocation reloadFor(GunItem gun)
    {
        return id(RELOAD_BY_GUN.getOrDefault(gun.getID(), "reload"));
    }

    public static void onReloadStart(Entity holder, GunItem gun)
    {
        if (net.nezerit6.animated_arsenal.compat.CrawlCompat.isCrawling(holder)) return;
        if (gun.getLoadingType() != GunItem.LoadingType.MAGAZINE) return;
        ResourceLocation reload = reloadFor(gun);
        Animation anim = PlayerAnimResources.getAnimation(reload);
        if (anim == null) return;

        float ticks = gun.getLoadingType() == GunItem.LoadingType.PER_CARTRIDGE
                ? Math.max(1, gun.getReloadStage3() - gun.getReloadStage2() + 1)
                : Math.max(1, gun.reloadCooldown());
        float speed = anim.length() / ticks;
        if (play(holder, reload, speed))
            RELOADING.put(holder.getId(), new Reload(reload, holder.level().getGameTime(), speed, anim.loopType() == Animation.LoopType.LOOP));
    }

    private record Reload(ResourceLocation animation, long startedAt, float speed, boolean loop) {}

    private static final int RELOAD_GRACE_TICKS = 6;
    private static final java.util.Map<Integer, Reload> RELOADING = new java.util.HashMap<>();

    public static void tick()
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        long now = mc.level.getGameTime();
        for (AbstractClientPlayer player : mc.level.players())
        {
            Reload reload = RELOADING.get(player.getId());
            if (reload == null || now - reload.startedAt() < RELOAD_GRACE_TICKS) continue;
            if (!isPlaying(player, reload.animation()))
            {
                RELOADING.remove(player.getId());
                continue;
            }
            ItemStack stack = player.getMainHandItem();
            boolean reloading = stack.getItem() instanceof GunItem && GunData.getBoolean(stack, "isReloading");

            boolean ending = false;
            PlayerAnimationController controller = controller(player);
            Animation anim = PlayerAnimResources.getAnimation(reload.animation());
            if (!reload.loop() && controller != null && anim != null)
                ending = (anim.length() - controller.getAnimationTicks()) / reload.speed() <= FADE_OUT_TICKS;

            if (!reloading || ending)
            {
                stop(player);
                RELOADING.remove(player.getId());
            }
        }
        if (RELOADING.size() > 64) RELOADING.clear();
    }
}