package net.nezerit6.animated_arsenal.sound;

import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds
{
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, AnimatedGuns.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_PISTOL_P1 = registerSoundEvent("generic_pistol_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_PISTOL_P2 = registerSoundEvent("generic_pistol_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_PISTOL_P3 = registerSoundEvent("generic_pistol_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> PISTOL_LIGHT = registerSoundEvent("pistol_light");
    public static final DeferredHolder<SoundEvent, SoundEvent> PISTOL_HEAVY = registerSoundEvent("pistol_heavy");
    public static final DeferredHolder<SoundEvent, SoundEvent> PISTOL_SERVICE = registerSoundEvent("pistol_service");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_OLD_ARMY_REVOLVER_P1 = registerSoundEvent("oldarmy_revolver_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_OLD_ARMY_REVOLVER_P2 = registerSoundEvent("oldarmy_revolver_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_OLD_ARMY_REVOLVER_P3 = registerSoundEvent("oldarmy_revolver_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> REVOLVER_COLTARMY = registerSoundEvent("revolver_coltarmy");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_REVOLVER_P0 = registerSoundEvent("generic_revolver_p0");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_REVOLVER_P1 = registerSoundEvent("generic_revolver_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_REVOLVER_P2 = registerSoundEvent("generic_revolver_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_REVOLVER_P3 = registerSoundEvent("generic_revolver_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> REVOLVER_MAGNUM = registerSoundEvent("revolver_magnum");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_SMG_P1 = registerSoundEvent("generic_smg_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_SMG_P2 = registerSoundEvent("generic_smg_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_SMG_P3 = registerSoundEvent("generic_smg_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> SMG_MACHINEPISTOL = registerSoundEvent("smg_machinepistol");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_HEAVY_SMG_P0 = registerSoundEvent("heavy_smg_p0");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_HEAVY_SMG_P1 = registerSoundEvent("heavy_smg_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_HEAVY_SMG_P2 = registerSoundEvent("heavy_smg_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_HEAVY_SMG_P3 = registerSoundEvent("heavy_smg_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> SMG_HEAVY = registerSoundEvent("smg_heavy");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_RAPID_SMG_P1 = registerSoundEvent("rapid_smg_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_RAPID_SMG_P2 = registerSoundEvent("rapid_smg_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_RAPID_SMG_P3 = registerSoundEvent("rapid_smg_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> SMG_RAPID = registerSoundEvent("smg_rapid");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_AR_P1 = registerSoundEvent("generic_ar_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_AR_P2 = registerSoundEvent("generic_ar_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_AR_P3 = registerSoundEvent("generic_ar_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASSAULTRIFLE_LIGHT = registerSoundEvent("assaultrifle_light");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_HEAVY_AR_P1 = registerSoundEvent("heavy_ar_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_HEAVY_AR_P2 = registerSoundEvent("heavy_ar_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_HEAVY_AR_P3 = registerSoundEvent("heavy_ar_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASSAULTRIFLE_HEAVY = registerSoundEvent("assaultrifle_heavy");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_WAR_TORN_AR_P1 = registerSoundEvent("wartorn_ar_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_WAR_TORN_AR_P2 = registerSoundEvent("wartorn_ar_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_WAR_TORN_AR_P3 = registerSoundEvent("wartorn_ar_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASSAULTRIFLE_RUS = registerSoundEvent("assaultrifle_rus");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_DOUBLE_BARREL_SHOTGUN_P1 = registerSoundEvent("doublebarrel_shotgun_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_DOUBLE_BARREL_SHOTGUN_P2 = registerSoundEvent("doublebarrel_shotgun_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_DOUBLE_BARREL_SHOTGUN_P3 = registerSoundEvent("doublebarrel_shotgun_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHOTGUN_DOUBLEBARREL = registerSoundEvent("shotgun_doublebarrel");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_COMBAT_SHOTGUN_P1 = registerSoundEvent("combat_shotgun_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_COMBAT_SHOTGUN_P2 = registerSoundEvent("combat_shotgun_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_COMBAT_SHOTGUN_P3 = registerSoundEvent("combat_shotgun_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHOTGUN_COMBAT = registerSoundEvent("shotgun_combat");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_RIOT_SHOTGUN_P1 = registerSoundEvent("riot_shotgun_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_RIOT_SHOTGUN_P2 = registerSoundEvent("riot_shotgun_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_RIOT_SHOTGUN_P3 = registerSoundEvent("riot_shotgun_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHOTGUN_RIOT = registerSoundEvent("shotgun_riot");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_SNIPER_P0 = registerSoundEvent("generic_sniper_p0");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_SNIPER_P1 = registerSoundEvent("generic_sniper_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_SNIPER_P2 = registerSoundEvent("generic_sniper_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_GENERIC_SNIPER_P3 = registerSoundEvent("generic_sniper_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_CLASSIC_SNIPER_P2 = registerSoundEvent("classic_sniper_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_BRUSH_GUN_P1 = registerSoundEvent("brush_gun_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_BRUSH_GUN_P2 = registerSoundEvent("brush_gun_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_BRUSH_GUN_P3 = registerSoundEvent("brush_gun_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNIPER_CLASSIC = registerSoundEvent("sniper_classic");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNIPER_ARCTIC = registerSoundEvent("sniper_arctic");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNIPER_COWBOY = registerSoundEvent("sniper_cowboy");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_MARKSMAN_RIFLE_P0 = registerSoundEvent("marksman_sniper_p0");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_MARKSMAN_RIFLE_P1 = registerSoundEvent("marksman_sniper_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_MARKSMAN_RIFLE_P2 = registerSoundEvent("marksman_sniper_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_MARKSMAN_RIFLE_P3 = registerSoundEvent("marksman_sniper_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNIPER_MARKSMAN = registerSoundEvent("sniper_marksman");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_M60_P0 = registerSoundEvent("m60_p0");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_M60_P1 = registerSoundEvent("m60_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_M60_P2 = registerSoundEvent("m60_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_M60_P3 = registerSoundEvent("m60_p3");
    public static final DeferredHolder<SoundEvent, SoundEvent> LMG_M60 = registerSoundEvent("lmg_m60");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMR_CLASSIC = registerSoundEvent("amr_classic");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_MINIGUN_P0 = registerSoundEvent("minigun_p0");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_MINIGUN_P1 = registerSoundEvent("minigun_p1");
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_MINIGUN_P2 = registerSoundEvent("minigun_p2");
    public static final DeferredHolder<SoundEvent, SoundEvent> LMG_MINIGUN = registerSoundEvent("lmg_minigun");
    public static final DeferredHolder<SoundEvent, SoundEvent> LMG_MINIGUN_POST = registerSoundEvent("lmg_minigun_post");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name)
    {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, name)));
    }
}