package net.nezerit6.animated_arsenal.item;

import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.nezerit6.animated_arsenal.attribute.GunAttributes;
import net.nezerit6.animated_arsenal.client.render.GunRenderer;
import net.nezerit6.animated_arsenal.entity.projectile.BulletProjectileEntity;
import net.nezerit6.animated_arsenal.network.ModNetwork;
import net.nezerit6.animated_arsenal.util.BulletUtil;
import net.nezerit6.animated_arsenal.util.GunData;
import net.nezerit6.animated_arsenal.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GunItem extends Item implements GeoItem
{
    public static final String controllerName = "controller";
    public static final int MELEE_COOLDOWN = 10;
    public static final int MAX_ATTACHMENTS = 3;
    private static final ResourceLocation GUN_DAMAGE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "gun_damage");

    public static final Map<String, RawAnimation> ANIMATIONS = Map.ofEntries(
            Map.entry("idle", RawAnimation.begin().thenLoop("idle")),
            Map.entry("firing", RawAnimation.begin().then("firing", Animation.LoopType.HOLD_ON_LAST_FRAME)),
            Map.entry("reload_start", RawAnimation.begin().then("reload_start", Animation.LoopType.PLAY_ONCE)),
            Map.entry("reload_magout", RawAnimation.begin().then("reload_magout", Animation.LoopType.PLAY_ONCE)),
            Map.entry("reload_magin", RawAnimation.begin().then("reload_magin", Animation.LoopType.PLAY_ONCE)),
            Map.entry("reload_end", RawAnimation.begin().then("reload_end", Animation.LoopType.HOLD_ON_LAST_FRAME)),
            Map.entry("aim", RawAnimation.begin().thenLoop("aim")),
            Map.entry("aim_firing", RawAnimation.begin().then("aim_firing", Animation.LoopType.HOLD_ON_LAST_FRAME)),
            Map.entry("aim_reload_start", RawAnimation.begin().then("aim_reload_start", Animation.LoopType.PLAY_ONCE)),
            Map.entry("melee", RawAnimation.begin().then("melee", Animation.LoopType.HOLD_ON_LAST_FRAME)),
            Map.entry("sprinting", RawAnimation.begin().thenLoop("sprinting")));

    public final Random random;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String gunID;
    private final String animationID;
    private final float gunDamage;
    private final int rateOfFire;
    private final int magSize;
    public final Supplier<? extends Item> ammoType;
    private final int reloadCooldown;
    private final float[] bulletSpread;
    private final float[] gunRecoil;
    private final int pelletCount;
    private final LoadingType loadingType;
    private final Supplier<SoundEvent> reloadSoundStart;
    private final Supplier<SoundEvent> reloadSoundMagOut;
    private final Supplier<SoundEvent> reloadSoundMagIn;
    private final Supplier<SoundEvent> reloadSoundEnd;
    private final Supplier<SoundEvent> shootSound;
    private final Supplier<SoundEvent> postShootSound;
    private final int reloadCycles;
    private final boolean isScoped;
    private final boolean unscopeAfterShot;
    private final int reloadStage1;
    private final int reloadStage2;
    private final int reloadStage3;
    public final FiringType firingType;
    public final ArmType armType;
    private final AttachmentItem.AttachType[] acceptedAttachmentTypes;

    public GunItem(Properties settings, String gunID, String animationID,
                   float gunDamage, int rateOfFire, int magSize,
                   Supplier<? extends Item> ammoType, int reloadCooldown, float[] bulletSpread,
                   float[] gunRecoil, int pelletCount, LoadingType loadingType,
                   @Nullable Supplier<SoundEvent> reloadSoundStart, @Nullable Supplier<SoundEvent> reloadSoundMagOut,
                   @Nullable Supplier<SoundEvent> reloadSoundMagIn, @Nullable Supplier<SoundEvent> reloadSoundEnd,
                   Supplier<SoundEvent> shootSound, @Nullable Supplier<SoundEvent> postShootSound, int reloadCycles, boolean isScoped, boolean unscopeAfterShot,
                   int reloadStage1, int reloadStage2, int reloadStage3, FiringType firingType, ArmType armType, AttachmentItem.AttachType[] acceptedAttachmentTypes)
    {
        super(settings.durability((magSize * 10) + 1)
                .attributes(ItemAttributeModifiers.builder()
                        .add(GunAttributes.GUN_DAMAGE, new AttributeModifier(GUN_DAMAGE_MODIFIER_ID, gunDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .build()));

        this.random = new Random();
        this.gunID = gunID;
        this.animationID = animationID;
        this.gunDamage = gunDamage;
        this.rateOfFire = rateOfFire;
        this.magSize = magSize;
        this.ammoType = ammoType;
        this.reloadCooldown = reloadCooldown;
        this.bulletSpread = bulletSpread;
        this.gunRecoil = gunRecoil;
        this.pelletCount = pelletCount;
        this.loadingType = loadingType;
        this.reloadSoundStart = reloadSoundStart;
        this.reloadSoundMagOut = reloadSoundMagOut;
        this.reloadSoundMagIn = reloadSoundMagIn;
        this.reloadSoundEnd = reloadSoundEnd;
        this.shootSound = shootSound;
        this.postShootSound = postShootSound;
        this.reloadCycles = reloadCycles;
        this.isScoped = isScoped;
        this.unscopeAfterShot = unscopeAfterShot;
        this.reloadStage1 = reloadStage1;
        this.reloadStage2 = reloadStage2;
        this.reloadStage3 = reloadStage3;
        this.firingType = firingType;
        this.armType = armType;
        this.acceptedAttachmentTypes = acceptedAttachmentTypes;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        ItemStack stack = user.getItemInHand(hand);

        if (hand == InteractionHand.MAIN_HAND)
        {
            if (!user.getCooldowns().isOnCooldown(this) && !user.isSprinting() && isLoaded(stack))
            {
                this.shoot(world, user, stack);
                user.getCooldowns().addCooldown(this, this.rateOfFire);
            }
        }
        return InteractionResultHolder.fail(stack);
    }

    private float randomSpread(float spread)
    {
        float s = Math.abs(spread * 5);
        return s == 0 ? 0 : (this.random.nextFloat() * 2 - 1) * s;
    }

    public void shoot(Level world, Player user, ItemStack itemStack)
    {
        GunData.putInt(itemStack, "reloadTick", 0);
        GunData.putBoolean(itemStack, "isReloading", false);

        double h_kick = getRecoilX(itemStack);
        float v_kick = getRecoilY(itemStack);
        boolean wasAiming = GunData.getBoolean(itemStack, "isAiming");

        if (shouldUnscopeAfterShot() && wasAiming)
        {
            GunData.putBoolean(itemStack, "isAiming", false);
        }

        if (!world.isClientSide())
        {
            float damage = (float) user.getAttributeValue(GunAttributes.GUN_DAMAGE);
            float spreadMult = getSpreadMult(itemStack);

            for (int i = 0; i < this.pelletCount; i++)
            {
                BulletProjectileEntity bullet = new BulletProjectileEntity(user, world, damage, this.pelletCount);
                bullet.setPos(user.getX(), user.getEyeY(), user.getZ());

                Vec3 vertiSpread = BulletUtil.vertiSpread(user, randomSpread(bulletSpread[0]) * spreadMult);
                Vec3 horiSpread = BulletUtil.horiSpread(user, randomSpread(bulletSpread[1]) * spreadMult);

                Vec3 result = user.getLookAngle().add(vertiSpread).add(horiSpread);

                bullet.shoot(result.x(), result.y(), result.z(), 20, 0);
                bullet.setBaseVel(bullet.getDeltaMovement());
                bullet.setGun(this);

                world.addFreshEntity(bullet);
            }

            playAnimation(user, itemStack, wasAiming ? "aim_firing" : "firing");

            if (user instanceof ServerPlayer serverPlayer)
            {
                PacketDistributor.sendToPlayer(serverPlayer, new ModNetwork.Recoil(v_kick, h_kick));
            }

            PacketDistributor.sendToPlayersTrackingEntity(user, new ModNetwork.GunFired(user.getId()));
        }
        else if (ModNetwork.CLIENT_HANDLER != null)
        {
            ModNetwork.CLIENT_HANDLER.onShot(user);
        }

        if (!user.getAbilities().instabuild)
        {
            GunData.putInt(itemStack, "Clip", GunData.getInt(itemStack, "Clip") - 1);
            itemStack.setDamageValue(this.getMaxDamage(itemStack) - ((GunData.getInt(itemStack, "Clip") * 10) + 1));
        }

        world.playSound(null, user.getX(), user.getY(), user.getZ(), shootSound.get(), SoundSource.MASTER, 1.0f, 1.0f);

        if (this.reloadCycles > 1)
        {
            GunData.putInt(itemStack, "currentCycle", GunData.getInt(itemStack, "Clip"));
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        AnimationController<GunItem> controller = new AnimationController<>(this, controllerName, 0, state ->
        {
            if (state.getController().getCurrentAnimation() == null || state.getController().getAnimationState() == AnimationController.State.STOPPED)
            {
                state.getController().tryTriggerAnimation("idle");
            }
            return PlayState.CONTINUE;
        });
        ANIMATIONS.forEach(controller::triggerableAnim);
        controllers.add(controller);
    }

    @Nullable
    public SoundEvent soundForKeyframe(String keyframe)
    {
        Supplier<SoundEvent> sound = switch (keyframe)
        {
            case "reload_start" -> this.reloadSoundStart;
            case "reload_magout" -> this.reloadSoundMagOut;
            case "reload_magin" -> this.reloadSoundMagIn;
            case "reload_end" -> this.reloadSoundEnd;
            case "post_shoot" -> this.postShootSound;
            case "melee" -> () -> SoundEvents.PLAYER_ATTACK_STRONG;
            default -> null;
        };
        if (sound != null) return sound.get();
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, keyframe));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache()
    {
        return this.cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer)
    {
        consumer.accept(new GeoRenderProvider()
        {
            private GunRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer()
            {
                if (this.renderer == null)
                    this.renderer = new GunRenderer();

                return this.renderer;
            }
        });
    }

    public void playAnimation(Player player, ItemStack stack, String animation)
    {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        long id = GeoItem.getOrAssignId(stack, serverLevel);
        sendAnimation(player, id, animation);
    }

    private void sendAnimation(Player player, long id, String animation)
    {
        ModNetwork.GunAnim packet = new ModNetwork.GunAnim(ModNetwork.itemId(this), id, animation, player.getId());
        if (player instanceof ServerPlayer serverPlayer)
        {
            PacketDistributor.sendToPlayer(serverPlayer, packet);
        }
        PacketDistributor.sendToPlayersTrackingEntity(player, packet);
    }

    private void setDefaultNBT(ItemStack stack)
    {
        GunData.putInt(stack, "reloadTick", 0);
        GunData.putInt(stack, "currentCycle", 1);
        GunData.putInt(stack, "Clip", 0);

        GunData.putBoolean(stack, "isScoped", this.isScoped);
        GunData.putBoolean(stack, "isReloading", false);
        GunData.putBoolean(stack, "isAiming", false);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level world, Player player)
    {
        setDefaultNBT(stack);
        if (world instanceof ServerLevel)
        {
            playAnimation(player, stack, "idle");
        }
        super.onCraftedBy(stack, world, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.translatable("tooltip.animated_arsenal.ammo", GunData.getInt(stack, "Clip"), this.magSize).withStyle(ChatFormatting.WHITE));
        if (flag.hasShiftDown())
        {
            double recoil = Math.sqrt((this.gunRecoil[0] * this.gunRecoil[0]) + (this.gunRecoil[1] * this.gunRecoil[1]));
            tooltip.add(Component.translatable("tooltip.animated_arsenal.damage", String.format(java.util.Locale.ROOT, "%.1f", this.gunDamage)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.animated_arsenal.recoil", String.format(java.util.Locale.ROOT, "%.2f", recoil)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.animated_arsenal.rpm", (int) (((float) 20 / this.rateOfFire) * 60)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.animated_arsenal.reload_time", String.format(java.util.Locale.ROOT, "%.2f", this.reloadCooldown / 20.0f)).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("tooltip.animated_arsenal.uses").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(this.ammoType.get().getDescriptionId()).withStyle(ChatFormatting.YELLOW));
        if (!flag.hasShiftDown())
        {
            tooltip.add(Component.translatable("tooltip.animated_arsenal.shift").withStyle(ChatFormatting.AQUA));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected)
    {
        if (!(entity instanceof Player player)) return;

        boolean isSprinting = player.isSprinting();
        ItemStack mainHandGun = player.getMainHandItem();

        if (world instanceof ServerLevel serverLevel) GeoItem.getOrAssignId(stack, serverLevel);

        if (world.isClientSide() && ModNetwork.CLIENT_HANDLER != null)
        {
            ModNetwork.CLIENT_HANDLER.gunInventoryTick(this, stack, player);
        }

        if (mainHandGun != stack && GunData.getBoolean(stack, "isAiming"))
        {
            GunData.putBoolean(stack, "isAiming", false);
            if (world.isClientSide())
            {
                AnimationController<?> controller = controller(stack);
                if (controller != null) controller.tryTriggerAnimation("idle");
            }
        }

        if (GunData.getBoolean(stack, "isReloading") && !isSprinting)
        {
            if (mainHandGun != stack
                    || (reserveAmmoCount(player, this.ammoType.get()) <= 0 && this.reloadCycles <= 1)
                    || (GunData.getInt(stack, "reloadTick") >= this.reloadCooldown)
                    || (remainingAmmo(stack) >= this.magSize && this.reloadCycles <= 1))
                GunData.putBoolean(stack, "isReloading", false);

            this.doReloadTick(world, player, stack);
        }
        else
        {
            int tick = GunData.getInt(stack, "reloadTick");
            if (tick > this.reloadStage3 && tick <= this.reloadCooldown) finishReload(player, stack);

            GunData.putBoolean(stack, "isReloading", false);
            GunData.putInt(stack, "reloadTick", 0);
        }
    }

    @Nullable
    public AnimationController<?> controller(ItemStack stack)
    {
        long id = GeoItem.getId(stack);
        if (id == Long.MAX_VALUE) return null;
        return getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get(controllerName);
    }

    private void doReloadTick(Level world, Player player, ItemStack stack)
    {
        int rTick = GunData.getInt(stack, "reloadTick");

        if (world instanceof ServerLevel)
        {
            if (rTick == this.reloadStage1) playAnimation(player, stack, "reload_magout");
            else if (rTick == this.reloadStage2) playAnimation(player, stack, "reload_magin");
            else if (rTick == this.reloadStage3) playAnimation(player, stack, "reload_end");
        }

        GunData.putInt(stack, "reloadTick", rTick + 1);

        switch (this.loadingType)
        {
            case MAGAZINE ->
            {
                if (rTick >= this.reloadCooldown && reserveAmmoCount(player, this.ammoType.get()) > 0)
                {
                    GunData.putInt(stack, "currentCycle", 1);
                    finishReload(player, stack);
                    GunData.putInt(stack, "reloadTick", 0);
                }
            }
            case PER_CARTRIDGE ->
            {
                if (rTick >= this.reloadStage3
                        && GunData.getInt(stack, "currentCycle") < this.reloadCycles
                        && reserveAmmoCount(player, this.ammoType.get()) > 0)
                {
                    GunData.putInt(stack, "Clip", GunData.getInt(stack, "Clip") + 1);
                    InventoryUtil.removeItemFromInventory(player, this.ammoType.get(), 1);
                    if (remainingAmmo(stack) < this.magSize && reserveAmmoCount(player, this.ammoType.get()) > 0)
                    {
                        GunData.putInt(stack, "reloadTick", this.reloadStage2);
                    }
                    GunData.putInt(stack, "currentCycle", GunData.getInt(stack, "Clip"));
                    stack.setDamageValue(this.getMaxDamage(stack) - ((GunData.getInt(stack, "Clip") * 10) + 1));
                }
            }
        }
    }

    public float getRecoilX(ItemStack stack)
    {
        boolean rd = this.random.nextBoolean();
        return (GunData.getBoolean(stack, "isAiming") ?
                (rd ? this.gunRecoil[0] : -this.gunRecoil[0]) / 2 :
                (rd ? this.gunRecoil[0] : -this.gunRecoil[0])) * getRecoilMult(stack);
    }

    public float getRecoilY(ItemStack stack)
    {
        return (GunData.getBoolean(stack, "isAiming") ? this.gunRecoil[1] / 2 : this.gunRecoil[1]) * getRecoilMult(stack);
    }

    public float getRecoilMult(ItemStack stack)
    {
        float mult = 1.0f;
        for (AttachmentItem attachment : getAttachments(stack)) mult *= attachment.getRecoilMult();
        return mult;
    }

    public float getSpreadMult(ItemStack stack)
    {
        float mult = 1.0f;
        for (AttachmentItem attachment : getAttachments(stack)) mult *= attachment.getSpreadMult();
        return mult;
    }

    public static boolean isLoaded(ItemStack stack)
    {
        return remainingAmmo(stack) > 0;
    }

    public void finishReload(Player player, ItemStack stack)
    {
        int clip = GunData.getInt(stack, "Clip");
        int ammoToLoad = this.magSize - clip;
        int reserve = reserveAmmoCount(player, this.ammoType.get());

        if (reserve >= ammoToLoad)
        {
            GunData.putInt(stack, "Clip", clip + ammoToLoad);
            InventoryUtil.removeItemFromInventory(player, this.ammoType.get(), ammoToLoad);
        }
        else
        {
            GunData.putInt(stack, "Clip", clip + reserve);
            InventoryUtil.removeItemFromInventory(player, this.ammoType.get(), reserve);
        }

        stack.setDamageValue(this.getMaxDamage(stack) - ((GunData.getInt(stack, "Clip") * 10) + 1));
    }

    public void aimAnimation(ItemStack stack, boolean aim, Player player)
    {
        GunData.putBoolean(stack, "isAiming", aim);
        GunData.putBoolean(stack, "isScoped", this.isScoped);

        playAnimation(player, stack, aim ? "aim" : "idle");
    }

    public static int remainingAmmo(ItemStack stack)
    {
        return GunData.getInt(stack, "Clip");
    }

    public static int reserveAmmoCount(Player player, Item item)
    {
        return InventoryUtil.itemCountInInventory(player, item);
    }

    public List<AttachmentItem> getAttachments(ItemStack gun)
    {
        List<AttachmentItem> list = new ArrayList<>();
        ListTag tags = GunData.read(gun).getList("Attachments", Tag.TAG_STRING);
        for (int i = 0; i < tags.size(); i++)
        {
            ResourceLocation id = ResourceLocation.tryParse(tags.getString(i));
            if (id != null && BuiltInRegistries.ITEM.get(id) instanceof AttachmentItem attachment) list.add(attachment);
        }
        return list;
    }

    private boolean acceptsAttachment(AttachmentItem.AttachType type)
    {
        for (AttachmentItem.AttachType accepted : this.acceptedAttachmentTypes)
        {
            if (accepted == type) return true;
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack gun, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access)
    {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) return false;

        if (other.isEmpty())
        {
            List<AttachmentItem> attachments = getAttachments(gun);
            if (attachments.isEmpty()) return true;

            AttachmentItem removed = attachments.get(0);
            GunData.update(gun, tag ->
            {
                ListTag list = tag.getList("Attachments", Tag.TAG_STRING);
                list.remove(0);
                if (list.isEmpty()) tag.remove("Attachments");
                else tag.put("Attachments", list);
                tag.putInt(removed.getAttachType().idKey(), 0);
            });
            access.set(new ItemStack(removed));
            player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8f, 0.8f + player.level().getRandom().nextFloat() * 0.4f);
        }
        else if (other.getItem() instanceof AttachmentItem attachment)
        {
            List<AttachmentItem> attachments = getAttachments(gun);
            if (attachments.size() >= MAX_ATTACHMENTS || !acceptsAttachment(attachment.getAttachType())) return true;
            for (AttachmentItem existing : attachments)
            {
                if (existing.getAttachType() == attachment.getAttachType()) return true;
            }

            GunData.update(gun, tag ->
            {
                ListTag list = tag.getList("Attachments", Tag.TAG_STRING);
                list.add(0, StringTag.valueOf(BuiltInRegistries.ITEM.getKey(attachment).toString()));
                tag.put("Attachments", list);
                tag.putInt(attachment.getAttachType().idKey(), attachment.getId());
            });
            other.shrink(1);
            player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 0.8f + player.level().getRandom().nextFloat() * 0.4f);
        }
        else
        {
            return false;
        }
        return true;
    }

    public int getSightID(ItemStack gun)
    {
        return GunData.getInt(gun, "sightID");
    }

    public int getGripID(ItemStack gun)
    {
        return GunData.getInt(gun, "gripID");
    }

    public int getMuzzleID(ItemStack gun)
    {
        return GunData.getInt(gun, "muzzleID");
    }

    public int getMagSize()
    {
        return this.magSize;
    }

    public Item getAmmoType()
    {
        return this.ammoType.get();
    }

    public boolean isLongGun()
    {
        return this.armType == ArmType.LONG_GUNS || this.armType == ArmType.MINIGUN;
    }

    public float getVerticalRecoil()
    {
        return this.gunRecoil[1];
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability)
    {
        return ability == ItemAbilities.SWORD_SWEEP || super.canPerformAction(stack, ability);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack)
    {
        return false;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner)
    {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return false;
    }

    @Override
    public int getBarColor(ItemStack stack)
    {
        return Mth.color(0.0f, 1f, 1f);
    }

    public int getRateOfFire()
    {
        return this.rateOfFire;
    }

    public boolean shouldUnscopeAfterShot()
    {
        return this.unscopeAfterShot;
    }

    public boolean isScoped()
    {
        return this.isScoped;
    }

    public FiringType getFiringType()
    {
        return this.firingType;
    }

    public ArmType getArmType()
    {
        return this.armType;
    }

    public String getID()
    {
        return this.gunID;
    }

    public String getAnimationID()
    {
        return this.animationID;
    }

    public enum LoadingType
    {
        MAGAZINE,
        PER_CARTRIDGE
    }

    public enum FiringType
    {
        SEMI_AUTO,
        AUTO
    }

    public enum ArmType
    {
        HANDGUN_ONEHAND,
        HANDGUN_TWOHAND,
        REVOLVER_FANNING,
        LONG_GUNS,
        MINIGUN
    }
}