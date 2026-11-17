package net.nezerit6.animated_arsenal.bullet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.nezerit6.animated_arsenal.config.GunConfig;
import net.nezerit6.animated_arsenal.network.ModNetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class BulletManager
{
    public static final TagKey<Block> BULLET_BREAKABLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "bullet_breakable"));

    private static final class Bullet
    {
        final LivingEntity owner;
        final Item gun;
        final float damage;
        final int pelletGroup;
        Vec3 pos;
        final Vec3 velocity;
        int life;
        boolean firstStep = true;

        Bullet(LivingEntity owner, Item gun, float damage, int pelletGroup, Vec3 pos, Vec3 velocity, int life)
        {
            this.owner = owner;
            this.gun = gun;
            this.damage = damage;
            this.pelletGroup = pelletGroup;
            this.pos = pos;
            this.velocity = velocity;
            this.life = life;
        }
    }

    private static final Map<ResourceKey<Level>, List<Bullet>> BULLETS = new HashMap<>();

    private BulletManager() {}

    public static void register()
    {
        NeoForge.EVENT_BUS.addListener(LevelTickEvent.Post.class, e ->
        {
            if (e.getLevel() instanceof ServerLevel level) tick(level);
        });
    }

    public static void fire(ServerLevel level, LivingEntity owner, Item gun, float damage, int pelletGroup, Vec3 start, Vec3 direction)
    {
        double speed = GunConfig.serverLoaded() ? GunConfig.BULLET_SPEED.get() : 20.0;
        int life = GunConfig.serverLoaded() ? GunConfig.BULLET_LIFETIME.get() : 10;
        Bullet bullet = new Bullet(owner, gun, damage, pelletGroup, start, direction.normalize().scale(speed), life);
        if (step(level, bullet))
        {
            BULLETS.computeIfAbsent(level.dimension(), k -> new ArrayList<>()).add(bullet);
        }
    }

    private static void tick(ServerLevel level)
    {
        List<Bullet> list = BULLETS.get(level.dimension());
        if (list == null || list.isEmpty()) return;
        Iterator<Bullet> it = list.iterator();
        while (it.hasNext())
        {
            if (!step(level, it.next())) it.remove();
        }
    }

    private static boolean step(ServerLevel level, Bullet b)
    {
        if (b.life-- <= 0 || b.owner.isRemoved() && b.owner.level() != level) return false;

        Vec3 start = b.pos;
        Vec3 end = start.add(b.velocity);
        if (!level.isLoaded(BlockPos.containing(end))) return false;

        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, b.owner));
        Vec3 limit = blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation() : end;

        AABB area = new AABB(start, limit).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, b.owner, start, limit, area,
                e -> !e.isSpectator() && e.isPickable() && e != b.owner && !(b.firstStep && b.owner.isPassengerOfSameVehicle(e)));
        b.firstStep = false;

        if (entityHit != null)
        {
            hitEntity(level, b, entityHit.getEntity());
            return false;
        }
        if (blockHit.getType() != HitResult.Type.MISS)
        {
            hitBlock(level, b, blockHit);
            return false;
        }

        b.pos = end;
        return true;
    }

    private static void hitEntity(ServerLevel level, Bullet b, Entity entity)
    {
        if (entity.hurt(GunDamageSource.create(level, b.owner, b.gun), b.damage))
        {
            entity.invulnerableTime = 0;
            if (entity instanceof EnderDragonPart part) part.parentMob.invulnerableTime = 0;
        }
    }

    private static void hitBlock(ServerLevel level, Bullet b, BlockHitResult hit)
    {
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        Vec3 at = hit.getLocation();

        if (state.is(BULLET_BREAKABLE) && (!GunConfig.serverLoaded() || GunConfig.BREAK_GLASS.get()))
        {
            if (mayBreak(level, b.owner, pos, state)) level.destroyBlock(pos, true, b.owner);
        }
        else
        {
            if (!GunConfig.serverLoaded() || GunConfig.IMPACT_SOUNDS.get())
            {
                float volume = b.pelletGroup > 1 ? Mth.clamp(1.0f / (float) b.pelletGroup, 0.2f, 1.0f) : 1.0f;
                level.playSound(null, pos, state.getSoundType(level, pos, b.owner).getBreakSound(), SoundSource.BLOCKS, volume, 1.0f);
            }
            PacketDistributor.sendToPlayersNear(level, null, at.x, at.y, at.z, 96, new ModNetwork.BulletHole(at, hit.getDirection(), pos));
        }
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), at.x, at.y, at.z, 5, 0.0, 0.0, 0.0, 0.5);

        Snowball proxy = new Snowball(level, b.owner);
        proxy.setPos(at);
        proxy.setDeltaMovement(b.velocity);
        state.onProjectileHit(level, state, hit, proxy);
    }

    private static boolean mayBreak(ServerLevel level, LivingEntity owner, BlockPos pos, BlockState state)
    {
        if (!(owner instanceof ServerPlayer player)) return true;
        if (player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer())) return false;
        if (!level.mayInteract(player, pos)) return false;
        return !NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player)).isCanceled();
    }
}