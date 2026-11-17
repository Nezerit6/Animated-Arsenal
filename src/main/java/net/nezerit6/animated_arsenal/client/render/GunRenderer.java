package net.nezerit6.animated_arsenal.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.nezerit6.animated_arsenal.client.model.GunModel;
import net.nezerit6.animated_arsenal.item.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtil;

public class GunRenderer extends GeoItemRenderer<GunItem>
{
    private static final ResourceLocation MUZZLE_FLASH = ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "textures/misc/muzzleflash_01.png");
    private static final ResourceLocation RETICLE = ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "textures/misc/reticle.png");

    private float sightDefaultHeight = 0.0f;
    private float sightAdjust = 0.0f;

    public GunRenderer()
    {
        super(new GunModel());
    }

    public static ModelResourceLocation worldViewModel(ResourceLocation gunId)
    {
        return ModelResourceLocation.standalone(gunId.withPath("item/" + gunId.getPath() + "_wv"));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay)
    {
        if (context.firstPerson())
        {
            super.renderByItem(stack, context, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }

        if (context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
        {
            keepAnimating(stack, context);
        }

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(worldViewModel(BuiltInRegistries.ITEM.getKey(stack.getItem())));
        boolean leftHand = context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

        poseStack.popPose();
        poseStack.pushPose();
        Minecraft.getInstance().getItemRenderer().render(stack, context, leftHand, poseStack, bufferSource, packedLight, packedOverlay, model);
    }

    public void keepAnimating(ItemStack stack, ItemDisplayContext context)
    {
        GunItem gun = (GunItem) stack.getItem();
        this.animatable = gun;
        this.currentItemStack = stack;
        this.renderPerspective = context;
        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);

        getGeoModel().getBakedModel(getGeoModel().getModelResource(gun, this));
        AnimationState<GunItem> state = new AnimationState<>(gun, 0, 0, partialTick, false);
        long id = getInstanceId(gun);
        state.setData(DataTickets.TICK, gun.getTick(stack));
        state.setData(DataTickets.ITEM_RENDER_PERSPECTIVE, context);
        state.setData(DataTickets.ITEMSTACK, stack);
        gun.getAnimatableInstanceCache().getManagerForId(id).setData(DataTickets.ITEM_RENDER_PERSPECTIVE, context);
        getGeoModel().addAdditionalStateData(gun, id, state::setData);
        getGeoModel().handleAnimations(gun, id, state, partialTick);

        this.animatable = null;
    }

    @Override
    public void renderRecursively(PoseStack poseStack, GunItem animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource,
                                  VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour)
    {
        Minecraft client = Minecraft.getInstance();
        ItemStack stack = this.getCurrentItemStack();
        int sightID = animatable.getSightID(stack);
        int gripID = animatable.getGripID(stack);
        int muzzleID = animatable.getMuzzleID(stack);

        RenderType boneType = renderType;
        boolean renderArms = false;

        poseStack.pushPose();

        switch (bone.getName())
        {
            case "leftArm", "rightArm" ->
            {
                bone.setHidden(true);
                bone.setChildrenHidden(false);
                renderArms = true;
            }
            case "gunbody", "magazine2" ->
            {
                if (sightID > 0) poseStack.translate(0, sightAdjust / 16, 0);
            }
            case "muzzleflash" ->
            {
                boneType = RenderType.eyes(MUZZLE_FLASH);
                if (sightID > 0) poseStack.translate(0, sightAdjust / 16, 0);
            }
            case "sight_default" ->
            {
                sightDefaultHeight = bone.getPivotY();
                bone.setHidden(sightID > 0);
            }
            case "sightPos" ->
            {
                if (sightID > 0)
                {
                    BakedGeoModel attachment = GeckoLibCache.getBakedModels().get(ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "geo/attach_si_" + sightID + ".geo.json"));
                    GeoBone sight = attachment == null ? null : attachment.getBone("sight").orElse(null);
                    GeoBone reticlePos = attachment == null ? null : attachment.getBone("reticlePos").orElse(null);
                    if (sight != null)
                    {
                        if (reticlePos != null) sightAdjust = sightDefaultHeight - bone.getPivotY() - reticlePos.getPivotY();

                        poseStack.pushPose();
                        poseStack.translate(bone.getPivotX() / 16, bone.getPivotY() / 16, bone.getPivotZ() / 16);
                        renderCubesOfBone(poseStack, sight, bufferSource.getBuffer(attachmentType("si", sightID)), packedLight, packedOverlay, colour);
                        if (reticlePos != null)
                            renderCubesOfBone(poseStack, reticlePos, bufferSource.getBuffer(RenderType.eyes(RETICLE)), packedLight, packedOverlay, colour);
                        poseStack.popPose();
                    }
                }
            }
            case "gripPos" -> renderAttachment(poseStack, bone, bufferSource, "gr", gripID, "grip", packedLight, packedOverlay, colour);
            case "muzzlePos" -> renderAttachment(poseStack, bone, bufferSource, "mz", muzzleID, "muzzle", packedLight, packedOverlay, colour);
        }

        if (renderArms && client.player != null)
        {
            EntityRenderer<? super AbstractClientPlayer> renderer = client.getEntityRenderDispatcher().getRenderer(client.player);
            if (renderer instanceof PlayerRenderer playerRenderer)
            {
                PlayerModel<AbstractClientPlayer> playerModel = playerRenderer.getModel();

                poseStack.pushPose();
                RenderUtil.prepMatrixForBone(poseStack, bone);

                ResourceLocation playerSkin = client.player.getSkin().texture();
                VertexConsumer arm = bufferSource.getBuffer(RenderType.entitySolid(playerSkin));

                boolean left = bone.getName().equals("leftArm");
                poseStack.scale(0.67f, 1.33f, 0.67f);
                poseStack.translate(left ? -0.25 : 0.25, -0.43625, 0.1625);

                var armPart = left ? playerModel.leftArm : playerModel.rightArm;
                var sleevePart = left ? playerModel.leftSleeve : playerModel.rightSleeve;

                armPart.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
                armPart.setRotation(0, 0, 0);
                armPart.render(poseStack, arm, packedLight, packedOverlay);

                VertexConsumer sleeve = bufferSource.getBuffer(RenderType.entityTranslucent(playerSkin));
                sleevePart.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
                sleevePart.setRotation(0, 0, 0);
                sleevePart.render(poseStack, sleeve, packedLight, packedOverlay);

                poseStack.popPose();
            }
        }

        VertexConsumer boneBuffer = isReRender ? buffer : bufferSource.getBuffer(boneType);
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, boneBuffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        poseStack.popPose();
    }

    private void renderAttachment(PoseStack poseStack, GeoBone slotBone, MultiBufferSource bufferSource, String type, int id, String boneName, int light, int overlay, int colour)
    {
        if (id <= 0) return;
        BakedGeoModel attachment = GeckoLibCache.getBakedModels().get(ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "geo/attach_" + type + "_" + id + ".geo.json"));
        GeoBone part = attachment == null ? null : attachment.getBone(boneName).orElse(null);
        if (part == null) return;

        poseStack.pushPose();
        poseStack.translate(slotBone.getPivotX() / 16, slotBone.getPivotY() / 16, slotBone.getPivotZ() / 16);
        renderCubesOfBone(poseStack, part, bufferSource.getBuffer(attachmentType(type, id)), light, overlay, colour);
        poseStack.popPose();
    }

    private static RenderType attachmentType(String type, int id)
    {
        return RenderType.entityTranslucentCull(ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "textures/misc/attach_" + type + "_" + id + ".png"));
    }
}