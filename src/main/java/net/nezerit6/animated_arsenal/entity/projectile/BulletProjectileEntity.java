package net.nezerit6.animated_arsenal.entity.projectile;

import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.nezerit6.animated_arsenal.network.ModNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.EnderDragonPart;

public class BulletProjectileEntity extends AbstractArrow
{
    public static final TagKey<Block> BULLET_BREAKABLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "bullet_breakable"));

    private float bulletDamage;
    private int pelletGroupCount = 1;
    private Item gun = Items.AIR;
    private Vec3 vel;
    private int maxLife = 10;
    private int lifeTicks;

    public BulletProjectileEntity(EntityType<? extends BulletProjectileEntity> type, Level world)
    {
        super(type, world);
    }

    public BulletProjectileEntity(LivingEntity owner, Level world, float bulletDamage, int pelletGroupCount)
    {
        super(AnimatedGuns.BULLET_ENTITY_TYPE.get(), owner, world, new ItemStack(Items.ARROW), null);
        this.pickup = Pickup.DISALLOWED;
        this.maxLife = 10;
        this.lifeTicks = 0;
        this.bulletDamage = bulletDamage;
        this.pelletGroupCount = pelletGroupCount;
        this.setNoGravity(true);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (this.vel != null)
        {
            this.setDeltaMovement(this.vel);
        }

        if (this.lifeTicks++ >= this.maxLife)
        {
            this.discard();
        }
    }

    @Override
    public boolean isNoGravity()
    {
        return !this.isInWater();
    }

    @Override
    protected ItemStack getDefaultPickupItem()
    {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public boolean isCritArrow()
    {
        return false;
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent()
    {
        return SoundEvents.EMPTY;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult)
    {
        Entity entity = entityHitResult.getEntity();
        Entity owner = this.getOwner();
        if (entity.hurt(this.damageSources().arrow(this, owner != null ? owner : this), this.bulletDamage))
        {
            entity.invulnerableTime = 0;
            if (entity instanceof EnderDragonPart part) part.parentMob.invulnerableTime = 0;
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult)
    {
        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = this.level().getBlockState(pos);

        if (this.level() instanceof ServerLevel serverLevel)
        {
            if (state.is(BULLET_BREAKABLE))
            {
                if (this.mayBreak(serverLevel, pos, state)) serverLevel.destroyBlock(pos, true, this.getOwner());
            }
            else
            {
                float volume = this.pelletGroupCount > 1 ? Mth.clamp(1.0f / (float) this.pelletGroupCount, 0.2f, 1.0f) : 1.0f;
                serverLevel.playSound(null, pos, state.getSoundType(serverLevel, pos, this).getBreakSound(), SoundSource.BLOCKS, volume, 1.0f);

                Vec3 hit = blockHitResult.getLocation();
                PacketDistributor.sendToPlayersNear(serverLevel, null, hit.x, hit.y, hit.z, 96,
                        new ModNetwork.BulletHole(hit, blockHitResult.getDirection(), pos));
            }
            Vec3 hit = blockHitResult.getLocation();
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), hit.x, hit.y, hit.z, 5, 0.0, 0.0, 0.0, 0.5);
        }

        state.onProjectileHit(this.level(), state, blockHitResult, this);
        this.discard();
    }

    private boolean mayBreak(ServerLevel level, BlockPos pos, BlockState state)
    {
        if (!(this.getOwner() instanceof ServerPlayer player)) return true;
        if (player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer())) return false;
        if (!level.mayInteract(player, pos)) return false;
        return !NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player)).isCanceled();
    }

    @Override
    protected boolean tryPickup(net.minecraft.world.entity.player.Player player)
    {
        return false;
    }

    public void setGun(Item gun)
    {
        this.gun = gun;
    }

    public Item getGun()
    {
        return this.gun;
    }

    public void setBaseVel(Vec3 vel)
    {
        this.vel = vel;
    }
}