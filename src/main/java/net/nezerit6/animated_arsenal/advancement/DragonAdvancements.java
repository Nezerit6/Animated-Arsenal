package net.nezerit6.animated_arsenal.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.nezerit6.animated_arsenal.entity.projectile.BulletProjectileEntity;
import net.nezerit6.animated_arsenal.item.GunItem;
import net.nezerit6.animated_arsenal.item.ModItems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DragonAdvancements
{
    private static final int PILLAR_CRYSTALS = 10;
    private static final ResourceLocation PEACEFUL_GUNS = ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "dragon_crystals_intact");
    private static final ResourceLocation REVOLVER_ONLY = ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "dragon_revolver_only");

    private static final Map<UUID, Map<UUID, Set<String>>> DAMAGE_BY_DRAGON = new HashMap<>();

    private DragonAdvancements() {}

    public static void register()
    {
        NeoForge.EVENT_BUS.addListener(LivingDamageEvent.Post.class, DragonAdvancements::onDamage);
        NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, DragonAdvancements::onDeath);
    }

    private static String weaponKind(Object direct)
    {
        if (direct instanceof BulletProjectileEntity bullet)
        {
            Item gun = bullet.getGun();
            if (gun == ModItems.MAGNUM_REVOLVER.get() || gun == ModItems.OLD_ARMY_REVOLVER.get()) return "revolver";
            if (gun instanceof GunItem) return "gun";
        }
        return "other";
    }

    private static void onDamage(LivingDamageEvent.Post event)
    {
        if (!(event.getEntity() instanceof EnderDragon dragon) || dragon.level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        DAMAGE_BY_DRAGON.computeIfAbsent(dragon.getUUID(), k -> new HashMap<>())
                .computeIfAbsent(player.getUUID(), k -> new HashSet<>())
                .add(weaponKind(event.getSource().getDirectEntity()));
    }

    private static void onDeath(LivingDeathEvent event)
    {
        if (!(event.getEntity() instanceof EnderDragon dragon) || dragon.level().isClientSide()) return;
        Map<UUID, Set<String>> damage = DAMAGE_BY_DRAGON.remove(dragon.getUUID());

        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
        if (weaponKind(event.getSource().getDirectEntity()).equals("other")) return;

        EndDragonFight fight = dragon.getDragonFight();
        if (fight == null || fight.getCrystalsAlive() < PILLAR_CRYSTALS) return;

        award(killer, PEACEFUL_GUNS);

        Set<String> used = damage == null ? null : damage.get(killer.getUUID());
        if (used != null && used.size() == 1 && used.contains("revolver"))
        {
            award(killer, REVOLVER_ONLY);
        }
    }

    private static void award(ServerPlayer player, ResourceLocation id)
    {
        AdvancementHolder holder = player.server.getAdvancements().get(id);
        if (holder == null) return;
        for (String criterion : player.getAdvancements().getOrStartProgress(holder).getRemainingCriteria())
        {
            player.getAdvancements().award(holder, criterion);
        }
    }
}