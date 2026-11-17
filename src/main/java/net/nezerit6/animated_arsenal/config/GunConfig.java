package net.nezerit6.animated_arsenal.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.nezerit6.animated_arsenal.item.GunItem;
import net.nezerit6.animated_arsenal.item.ModItems;

import java.util.HashMap;
import java.util.Map;

public final class GunConfig
{
    public static ModConfigSpec SERVER_SPEC;
    public static ModConfigSpec CLIENT_SPEC;

    public static ModConfigSpec.DoubleValue DAMAGE_MULTIPLIER;
    public static ModConfigSpec.DoubleValue RECOIL_MULTIPLIER;
    public static ModConfigSpec.DoubleValue SPREAD_MULTIPLIER;
    public static ModConfigSpec.DoubleValue FIRE_RATE_MULTIPLIER;
    public static ModConfigSpec.DoubleValue RELOAD_TIME_MULTIPLIER;
    public static ModConfigSpec.DoubleValue BULLET_SPEED;
    public static ModConfigSpec.IntValue BULLET_LIFETIME;
    public static ModConfigSpec.BooleanValue INFINITE_AMMO;
    public static ModConfigSpec.BooleanValue BREAK_GLASS;
    public static ModConfigSpec.BooleanValue IMPACT_SOUNDS;
    public static ModConfigSpec.IntValue MELEE_COOLDOWN;
    public static ModConfigSpec.BooleanValue ALLOW_OFFHAND;

    public static ModConfigSpec.BooleanValue DYNAMIC_LIGHTS;
    public static ModConfigSpec.BooleanValue BULLET_HOLES;
    public static ModConfigSpec.IntValue BULLET_HOLE_SECONDS;
    public static ModConfigSpec.BooleanValue SCREEN_RECOIL;
    public static ModConfigSpec.BooleanValue BULLET_TRACERS;

    private static final Map<GunItem, GunValues> GUNS = new HashMap<>();

    public record GunValues(ModConfigSpec.BooleanValue enabled, ModConfigSpec.DoubleValue damage, ModConfigSpec.IntValue fireRate,
                            ModConfigSpec.IntValue magazine, ModConfigSpec.IntValue reloadTime, ModConfigSpec.IntValue pellets,
                            ModConfigSpec.DoubleValue spreadVertical, ModConfigSpec.DoubleValue spreadHorizontal,
                            ModConfigSpec.DoubleValue recoilHorizontal, ModConfigSpec.DoubleValue recoilVertical,
                            ModConfigSpec.EnumValue<GunItem.FiringType> firingMode) {}

    private GunConfig() {}

    public static void build()
    {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Settings for every gun at once").push("global");
        DAMAGE_MULTIPLIER = b.comment("Multiplies the damage of every gun").defineInRange("damageMultiplier", 1.0, 0.0, 100.0);
        RECOIL_MULTIPLIER = b.comment("Multiplies the camera recoil of every gun").defineInRange("recoilMultiplier", 1.0, 0.0, 100.0);
        SPREAD_MULTIPLIER = b.comment("Multiplies the bullet spread of every gun").defineInRange("spreadMultiplier", 1.0, 0.0, 100.0);
        FIRE_RATE_MULTIPLIER = b.comment("Multiplies the delay between shots (higher = slower)").defineInRange("fireDelayMultiplier", 1.0, 0.05, 100.0);
        RELOAD_TIME_MULTIPLIER = b.comment("Multiplies reload times (higher = slower)").defineInRange("reloadTimeMultiplier", 1.0, 0.1, 100.0);
        BULLET_SPEED = b.comment("Bullet speed in blocks per tick").defineInRange("bulletSpeed", 20.0, 1.0, 100.0);
        BULLET_LIFETIME = b.comment("Ticks before a bullet disappears (range = speed x lifetime)").defineInRange("bulletLifetime", 10, 1, 200);
        INFINITE_AMMO = b.comment("Guns never use up ammo").define("infiniteAmmo", false);
        BREAK_GLASS = b.comment("Bullets break blocks in the tag animated_arsenal:bullet_breakable").define("breakGlass", true);
        IMPACT_SOUNDS = b.comment("Bullets play the block's break sound on impact").define("impactSounds", true);
        MELEE_COOLDOWN = b.comment("Ticks a gun can neither fire nor bash after a bash").defineInRange("meleeCooldown", 10, 0, 200);
        ALLOW_OFFHAND = b.comment("Allow putting guns in the offhand").define("allowOffhand", false);
        b.pop();

        b.comment("Per-gun settings. Times are in ticks (20 ticks = 1 second).").push("guns");
        for (var holder : ModItems.GUN_ITEMS)
        {
            GunItem gun = holder.get();
            String name = BuiltInRegistries.ITEM.getKey(gun).getPath();
            b.push(name);
            GUNS.put(gun, new GunValues(
                    b.comment("Can be fired at all").define("enabled", true),
                    b.defineInRange("damage", (double) gun.baseDamage(), 0.0, 10000.0),
                    b.comment("Ticks between shots").defineInRange("fireDelay", gun.baseRateOfFire(), 1, 1200),
                    b.defineInRange("magazineSize", gun.baseMagSize(), 1, 10000),
                    b.comment("Ticks for a full reload").defineInRange("reloadTime", gun.baseReloadCooldown(), 1, 1200),
                    b.comment("Bullets per shot").defineInRange("pellets", gun.basePelletCount(), 1, 100),
                    b.defineInRange("spreadVertical", (double) gun.baseSpread()[0], 0.0, 100.0),
                    b.defineInRange("spreadHorizontal", (double) gun.baseSpread()[1], 0.0, 100.0),
                    b.defineInRange("recoilHorizontal", (double) gun.baseRecoil()[0], 0.0, 100.0),
                    b.defineInRange("recoilVertical", (double) gun.baseRecoil()[1], 0.0, 100.0),
                    b.defineEnum("firingMode", gun.getBaseFiringType())));
            b.pop();
        }
        b.pop();
        SERVER_SPEC = b.build();

        ModConfigSpec.Builder c = new ModConfigSpec.Builder();
        DYNAMIC_LIGHTS = c.comment("Muzzle flash lighting").define("dynamicLights", true);
        BULLET_HOLES = c.comment("Bullet hole decals on blocks").define("bulletHoles", true);
        BULLET_HOLE_SECONDS = c.comment("How long bullet holes stay").defineInRange("bulletHoleSeconds", 30, 1, 600);
        SCREEN_RECOIL = c.comment("Camera kick when shooting").define("screenRecoil", true);
        BULLET_TRACERS = c.comment("Visible bullet tracers").define("bulletTracers", true);
        CLIENT_SPEC = c.build();
    }

    public static GunValues of(GunItem gun)
    {
        return SERVER_SPEC != null && SERVER_SPEC.isLoaded() ? GUNS.get(gun) : null;
    }

    public static boolean serverLoaded()
    {
        return SERVER_SPEC != null && SERVER_SPEC.isLoaded();
    }

    public static boolean clientLoaded()
    {
        return CLIENT_SPEC != null && CLIENT_SPEC.isLoaded();
    }
}