package net.nezerit6.animated_arsenal.bullet;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.nezerit6.animated_arsenal.AnimatedGuns;
import org.jetbrains.annotations.Nullable;

public class GunDamageSource extends DamageSource
{
    public static final ResourceKey<DamageType> BULLET = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "bullet"));

    private final Item gun;

    public GunDamageSource(Holder<DamageType> type, @Nullable Entity shooter, Item gun)
    {
        super(type, shooter, shooter);
        this.gun = gun;
    }

    public static GunDamageSource create(ServerLevel level, @Nullable Entity shooter, Item gun)
    {
        return new GunDamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(BULLET), shooter, gun);
    }

    public Item getGun()
    {
        return this.gun;
    }
}