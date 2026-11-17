package net.nezerit6.animated_arsenal.mixin.client;

import net.nezerit6.animated_arsenal.client.GunArmAnimations;
import net.nezerit6.animated_arsenal.compat.CrawlCompat;
import net.nezerit6.animated_arsenal.item.GunItem;
import net.nezerit6.animated_arsenal.util.GunData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HumanoidModel.class, priority = 1500)
public abstract class HumanoidModelMixin<T extends LivingEntity>
{
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow public float swimAmount;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void animated_arsenal$gunArms(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci)
    {
        if (!(entity instanceof Player player)) return;

        this.rightLeg.x = -1.9F;
        this.leftLeg.x = 1.9F;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem gun)) return;

        boolean sprinting = player.isSprinting();

        boolean shooting = !sprinting && (GunData.getBoolean(stack, "isAiming") || GunArmAnimations.isInFiringPose(player, ageInTicks));
        boolean reloading = GunData.getInt(stack, "reloadTick") > 0 || GunData.getBoolean(stack, "isReloading");

        boolean rightHanded = player.getMainArm() == HumanoidArm.RIGHT;
        ModelPart mainArm = rightHanded ? this.rightArm : this.leftArm;
        ModelPart offArm = rightHanded ? this.leftArm : this.rightArm;

        float inward = rightHanded ? -1.0F : 1.0F;

        if (CrawlCompat.isCrawling(player))
        {
            if (reloading)
            {
                float work = Mth.abs(Mth.sin(ageInTicks * 0.3F));
                float t = Mth.clamp(this.swimAmount, 0.0F, 1.0F);
                animated_arsenal$lerpRot(t, mainArm, -(0.35F * inward), 0.0F, -0.85F - Mth.HALF_PI);
                animated_arsenal$lerpRot(t, offArm, -(-0.55F * inward), 0.0F, -0.65F - 0.35F * work - Mth.HALF_PI);
            }
            else if (shooting)
            {
                animated_arsenal$proneAim(player, netHeadYaw, headPitch);
            }
            return;
        }

        float melee = GunArmAnimations.meleeProgress(player, ageInTicks);
        if (melee > 0.0F)
        {
            animated_arsenal$buttStroke(melee, mainArm, offArm, rightHanded, !gun.isLongGun());
            return;
        }

        if (reloading || sprinting || !GunItem.isLoaded(stack)) return;

        float side = rightHanded ? 1.0F : -1.0F;
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;

        this.body.yRot = this.head.yRot;
        this.rightArm.z = Mth.sin(this.body.yRot) * 5.0F;
        this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0F;
        this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0F;
        this.leftArm.x = Mth.cos(this.body.yRot) * 5.0F;

        float crouch = model.crouching ? 4.0F : 0.0F;
        this.rightLeg.x = -Mth.cos(this.body.yRot) * 2.0F + Mth.sin(this.body.yRot) * crouch;
        this.rightLeg.z = Mth.sin(this.body.yRot) * 2.0F + Mth.cos(this.body.yRot) * crouch;
        this.leftLeg.x = Mth.cos(this.body.yRot) * 2.0F + Mth.sin(this.body.yRot) * crouch;
        this.leftLeg.z = -Mth.sin(this.body.yRot) * 2.0F + Mth.cos(this.body.yRot) * crouch;
        this.rightLeg.yRot = this.body.yRot;
        this.leftLeg.yRot = this.body.yRot;

        switch (gun.getArmType())
        {
            case HANDGUN_ONEHAND ->
            {
                mainArm.xRot = this.head.xRot - Mth.HALF_PI;
                mainArm.yRot = this.head.yRot;
                offArm.yRot = this.head.yRot;
            }
            case HANDGUN_TWOHAND, LONG_GUNS ->
            {
                mainArm.xRot = this.head.xRot - Mth.HALF_PI;
                offArm.xRot = this.head.xRot - Mth.HALF_PI;
                mainArm.yRot = this.head.yRot;
                offArm.yRot = this.head.yRot + 0.4F * side;
            }
            case REVOLVER_FANNING ->
            {
                mainArm.xRot = this.head.xRot - Mth.HALF_PI;
                mainArm.yRot = this.head.yRot;
                offArm.xRot = this.head.xRot - 1.0F;
                offArm.yRot = this.head.yRot + 0.75F * side;
            }
            case MINIGUN ->
            {
                mainArm.xRot = this.head.xRot - 0.5707964F;
                mainArm.yRot = this.head.yRot;
                offArm.xRot = this.head.xRot - 1.0F;
                offArm.yRot = this.head.yRot + 0.75F * side;
            }
        }
        mainArm.zRot = 0.0F;
        offArm.zRot = 0.0F;

        if (GunData.getBoolean(stack, "isAiming"))
        {
            offArm.xRot = this.head.xRot - Mth.HALF_PI;
            offArm.yRot = this.head.yRot + 0.5F * side;
            mainArm.x *= 0.7F;
            mainArm.z *= 0.7F;
            offArm.x *= 0.8F;
            offArm.z *= 0.8F;
        }


        float recoil = GunArmAnimations.recoil(player, ageInTicks);
        if (recoil > 0.0F)
        {
            float kick = recoil * Mth.clamp(gun.getVerticalRecoil() * 0.08F, 0.15F, 0.6F);
            mainArm.xRot -= kick;
            offArm.xRot -= kick;
        }
    }

    @Unique
    private void animated_arsenal$buttStroke(float progress, ModelPart mainArm, ModelPart offArm, boolean rightHanded, boolean oneHanded)
    {
        float side = rightHanded ? 1.0F : -1.0F;
        float inward = -side;

        float strike = GunArmAnimations.meleeStrike(progress);

        float bodyYRot = -0.3F * side * strike;
        this.body.yRot = bodyYRot;
        this.rightArm.z = Mth.sin(bodyYRot) * 5.0F;
        this.rightArm.x = -Mth.cos(bodyYRot) * 5.0F;
        this.leftArm.z = -Mth.sin(bodyYRot) * 5.0F;
        this.leftArm.x = Mth.cos(bodyYRot) * 5.0F;

        mainArm.xRot = Mth.lerp(strike, -0.55F, -1.25F);
        mainArm.yRot = Mth.lerp(strike, 0.2F, 0.35F) * inward + bodyYRot;
        mainArm.zRot = 0.0F;

        if (oneHanded)
        {
            offArm.xRot = Mth.lerp(strike, -0.8F, 0.1F);
            offArm.yRot = Mth.lerp(strike, -0.65F * inward, 0.0F) + bodyYRot;
            offArm.zRot = Mth.lerp(strike, 0.0F, 0.15F * side);
        }
        else
        {
            offArm.xRot = Mth.lerp(strike, -0.8F, -1.25F);
            offArm.yRot = Mth.lerp(strike, -0.65F, -0.35F) * inward + bodyYRot;
            offArm.zRot = 0.0F;
        }
    }

    @Unique
    private void animated_arsenal$proneAim(Player player, float netHeadYaw, float headPitch)
    {
        float headYRot = netHeadYaw * Mth.DEG_TO_RAD;
        float headXRot = headPitch * Mth.DEG_TO_RAD;
        boolean rightHanded = player.getMainArm() == HumanoidArm.RIGHT;
        float rightYRot = -0.1F + headYRot - (rightHanded ? 0.0F : 0.4F);
        float leftYRot = 0.1F + headYRot + (rightHanded ? 0.4F : 0.0F);
        float armXRot = -Mth.HALF_PI + headXRot;

        float t = Mth.clamp(this.swimAmount, 0.0F, 1.0F);
        animated_arsenal$lerpRot(t, this.rightArm, -rightYRot, 0.0F, armXRot - Mth.HALF_PI);
        animated_arsenal$lerpRot(t, this.leftArm, -leftYRot, 0.0F, armXRot - Mth.HALF_PI);
    }

    @Unique
    private static void animated_arsenal$lerpRot(float t, ModelPart part, float zRot, float yRot, float xRot)
    {
        part.zRot = animated_arsenal$rotLerp(t, part.zRot, zRot);
        part.yRot = animated_arsenal$rotLerp(t, part.yRot, yRot);
        part.xRot = animated_arsenal$rotLerp(t, part.xRot, xRot);
    }

    @Unique
    private static float animated_arsenal$rotLerp(float t, float from, float to)
    {
        return Mth.rotLerp(t, from * Mth.RAD_TO_DEG, to * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
    }
}