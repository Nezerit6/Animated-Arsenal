package net.nezerit6.animated_arsenal.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.nezerit6.animated_arsenal.compat.CrawlCompat;
import net.nezerit6.animated_arsenal.item.GunItem;
import net.nezerit6.animated_arsenal.network.ModNetwork;
import net.nezerit6.animated_arsenal.util.GunData;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.nezerit6.animated_arsenal.screen.BlueprintScreen;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.loading.math.value.Variable;

@Mod(value = AnimatedGuns.MOD_ID, dist = Dist.CLIENT)
public class AnimatedGunsClient implements ModNetwork.ClientHandler
{
    public static final KeyMapping reloadToggle = new KeyMapping("key.animated_arsenal.reloadtoggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.animated_arsenal.binds");
    public static final KeyMapping meleeKey = new KeyMapping("key.animated_arsenal.melee", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.animated_arsenal.binds");

    private static final net.neoforged.neoforge.client.settings.IKeyConflictContext HOLDING_GUN = new net.neoforged.neoforge.client.settings.IKeyConflictContext()
    {
        @Override
        public boolean isActive()
        {
            return Minecraft.getInstance().screen == null && holdingGun(Minecraft.getInstance().player);
        }

        @Override
        public boolean conflicts(net.neoforged.neoforge.client.settings.IKeyConflictContext other)
        {
            return other == this;
        }
    };
    public static final KeyMapping shootKey = new KeyMapping("key.animated_arsenal.shoot", HOLDING_GUN, InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_LEFT, "category.animated_arsenal.binds");
    public static final KeyMapping aimKey = new KeyMapping("key.animated_arsenal.aim", HOLDING_GUN, InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_RIGHT, "category.animated_arsenal.binds");

    private static final ResourceLocation GUN_SCOPE = ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "textures/misc/gun_scope.png");
    private float gunScopeScale = 0.5f;

    public AnimatedGunsClient(IEventBus modBus)
    {
        ModNetwork.CLIENT_HANDLER = this;

        MathParser.registerVariable(new Variable("h_recoil", 0));

        modBus.addListener(RegisterKeyMappingsEvent.class, e ->
        {
            e.register(reloadToggle);
            e.register(meleeKey);
            e.register(shootKey);
            e.register(aimKey);
        });
        modBus.addListener(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent.class, e -> e.enqueueWork(PlayerAnims::registerLayer));
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> PlayerAnims.tick());
        modBus.addListener(RegisterMenuScreensEvent.class, e ->
                e.register(AnimatedGuns.BLUEPRINT_SCREEN_HANDLER_TYPE.get(), BlueprintScreen::new));
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.class, BulletHoles::render);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.class, BulletTracers::render);
        modBus.addListener(EntityRenderersEvent.AddLayers.class, e ->
        {
            for (net.minecraft.client.resources.PlayerSkin.Model skin : e.getSkins())
            {
                if (e.getSkin(skin) instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer renderer)
                {
                    renderer.addLayer(new net.nezerit6.animated_arsenal.client.render.BackGunLayer(renderer));
                }
            }
        });
        modBus.addListener(ModelEvent.RegisterAdditional.class, e ->
        {
            for (var gun : net.nezerit6.animated_arsenal.item.ModItems.GUN_ITEMS)
            {
                e.register(net.nezerit6.animated_arsenal.client.render.GunRenderer.worldViewModel(gun.getId()));
            }
        });
        modBus.addListener(RegisterGuiLayersEvent.class, e ->
                e.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "gun_scope"), this::renderScopeLayer));

        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Pre.class, e -> this.onClientTickPre());
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.client.event.RenderFrameEvent.Pre.class, e -> this.keepHeldGunAnimating());
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> this.onClientTickPost());
        NeoForge.EVENT_BUS.addListener(InputEvent.InteractionKeyMappingTriggered.class, this::onInteractionKey);
        NeoForge.EVENT_BUS.addListener(ComputeFovModifierEvent.class, this::onFov);
        NeoForge.EVENT_BUS.addListener(MovementInputUpdateEvent.class, this::onMovementInput);
        NeoForge.EVENT_BUS.addListener(RenderHandEvent.class, this::onRenderHand);
        NeoForge.EVENT_BUS.addListener(RenderGuiLayerEvent.Pre.class, this::onRenderGuiLayer);
    }

    public static boolean holdingGun(Player player)
    {
        return player != null && player.getMainHandItem().getItem() instanceof GunItem;
    }

    public static boolean isAiming(Player player)
    {
        return holdingGun(player) && GunData.getBoolean(player.getMainHandItem(), "isAiming");
    }

    public static boolean isAimingScoped(Player player)
    {
        return isAiming(player) && GunData.getBoolean(player.getMainHandItem(), "isScoped");
    }

    private void onClientTickPre()
    {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        boolean bashPressed = false;
        while (meleeKey.consumeClick()) bashPressed = true;

        if (player == null || mc.gameMode == null || !holdingGun(player)) return;

        ItemStack gun = player.getMainHandItem();
        GunItem gunItem = (GunItem) gun.getItem();

        if (bashPressed && mc.screen == null) tryBash(player, gun, gunItem);

        if (mc.screen != null) return;

        boolean framed = false;
        while (aimKey.consumeClick())
        {
            if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof net.minecraft.world.entity.decoration.ItemFrame frame)
            {
                framed = true;
                if (mc.level.getWorldBorder().isWithinBounds(frame.blockPosition())
                        && !mc.gameMode.interactAt(player, frame, hit, InteractionHand.MAIN_HAND).consumesAction())
                {
                    mc.gameMode.interact(player, frame, InteractionHand.MAIN_HAND);
                }
            }
        }

        if (this.aimRequestCooldown > 0) this.aimRequestCooldown--;
        boolean wantAim = !framed
                && aimKey.isDown()
                && !GunData.getBoolean(gun, "isReloading")
                && !(gunItem.shouldUnscopeAfterShot() && player.getCooldowns().isOnCooldown(gunItem));
        if (wantAim != GunData.getBoolean(gun, "isAiming") && this.aimRequestCooldown <= 0)
        {
            if (wantAim) player.setSprinting(false);
            PacketDistributor.sendToServer(new ModNetwork.Aim(wantAim));
            this.aimRequestCooldown = 5;
        }

        boolean fire = false;
        while (shootKey.consumeClick()) fire = true;
        if (gunItem.getFiringType() == GunItem.FiringType.AUTO && shootKey.isDown()) fire = true;

        if (fire && !player.isUsingItem())
        {
            mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
        }
    }

    private int aimRequestCooldown = 0;

    private void keepHeldGunAnimating()
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null || mc.player == null || !holdingGun(mc.player)) return;

        ItemStack stack = mc.player.getMainHandItem();
        if (software.bernie.geckolib.animatable.client.GeoRenderProvider.of(stack).getGeoItemRenderer() instanceof net.nezerit6.animated_arsenal.client.render.GunRenderer renderer)
        {
            renderer.keepAnimating(stack, net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
        }
    }

    private void tryBash(LocalPlayer player, ItemStack gun, GunItem gunItem)
    {
        if (player.getCooldowns().isOnCooldown(gunItem)) return;
        if (GunData.getBoolean(gun, "isReloading") || GunData.getInt(gun, "reloadTick") > 0) return;
        if (CrawlCompat.isCrawling(player)) return;

        GunArmAnimations.onMelee(player);
        PacketDistributor.sendToServer(new ModNetwork.MeleeServer(player.getInventory().selected));
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null && mc.hitResult instanceof EntityHitResult hit)
        {
            mc.gameMode.attack(player, hit.getEntity());
        }

        player.getCooldowns().addCooldown(gunItem, (net.nezerit6.animated_arsenal.config.GunConfig.serverLoaded() ? net.nezerit6.animated_arsenal.config.GunConfig.MELEE_COOLDOWN.get() : GunItem.MELEE_COOLDOWN));
    }

    private void onMovementInput(MovementInputUpdateEvent event)
    {
        Player player = event.getEntity();
        if (player != Minecraft.getInstance().player
                || !CrawlCompat.isCrawling(player)
                || !holdingGun(player)
                || !(isAiming(player) || GunArmAnimations.isInFiringPose(player, player.tickCount))) return;

        Input input = event.getInput();
        input.forwardImpulse = 0.0F;
        input.leftImpulse = 0.0F;
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
    }

    private void onClientTickPost()
    {
        DynamicLights.tick();
        GunSounds.tick();
        BulletHoles.tick();
        BulletTracers.tick();

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && isAiming(player) && player.isSprinting())
        {
            player.setSprinting(false);
        }
    }

    private void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event)
    {
        Player player = Minecraft.getInstance().player;
        if (holdingGun(player) && (event.isAttack() || event.isUseItem()))
        {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @Override
    public void gunInventoryTick(GunItem gunItem, ItemStack stack, Player entity)
    {
        Minecraft mc = Minecraft.getInstance();
        if (entity != mc.player) return;

        boolean isSprinting = entity.isSprinting();
        ItemStack mainHandGun = entity.getMainHandItem();

        if (mainHandGun == stack
                && reloadToggle.isDown()
                && GunItem.remainingAmmo(stack) < gunItem.getMagSize()
                && GunItem.reserveAmmoCount(entity, gunItem.getAmmoType()) > 0
                && !GunData.getBoolean(stack, "isReloading")
                && !isSprinting)
        {
            PacketDistributor.sendToServer(new ModNetwork.Reload(true));
        }

        AnimationController<?> controller = controller(gunItem, GeoItem.getId(stack));
        if (controller == null || controller.getCurrentAnimation() == null || controller.getAnimationState() == AnimationController.State.STOPPED) return;

        String current = controller.getCurrentAnimation().animation().name();
        boolean meleeDone = !current.equals("melee") || controller.getAnimationState() == AnimationController.State.PAUSED;
        boolean playingSprint = controller.isPlayingTriggeredAnimation() && current.equals("sprinting");

        if (isSprinting
                && mainHandGun == stack
                && !GunData.getBoolean(mainHandGun, "isAiming")
                && !GunData.getBoolean(mainHandGun, "isReloading")
                && !playingSprint
                && meleeDone)
        {
            controller.tryTriggerAnimation("sprinting");
        }
        else if ((!isSprinting || mainHandGun != stack) && playingSprint && meleeDone)
        {
            controller.tryTriggerAnimation(GunData.getBoolean(mainHandGun, "isAiming") ? "aim" : "idle");
        }
    }

    private static AnimationController<?> controller(GunItem gun, long id)
    {
        if (id == Long.MAX_VALUE) return null;
        return gun.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get(GunItem.controllerName);
    }

    @Override
    public void recoil(ModNetwork.Recoil packet)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null)
        {
            if (!net.nezerit6.animated_arsenal.config.GunConfig.clientLoaded() || net.nezerit6.animated_arsenal.config.GunConfig.SCREEN_RECOIL.get()) player.turn(packet.hKick() * 5, -packet.kick() * 5);
        }
    }

    @Override
    public void gunAnim(ModNetwork.GunAnim packet)
    {
        if (!(BuiltInRegistries.ITEM.get(packet.item()) instanceof GunItem gun)) return;
        String animation = resolveAnimation(gun, packet.animation());
        GunSounds.onAnimation(gun, packet.instanceId(), animation, packet.holderId());
        if ("melee".equals(packet.animation()) && Minecraft.getInstance().level != null)
        {
            net.minecraft.world.entity.Entity holder = Minecraft.getInstance().level.getEntity(packet.holderId());
            if (holder != null && holder != Minecraft.getInstance().player) GunArmAnimations.onMelee(holder);
        }
        if (("reload_start".equals(packet.animation()) || "aim_reload_start".equals(packet.animation())) && Minecraft.getInstance().level != null)
        {
            net.minecraft.world.entity.Entity holder = Minecraft.getInstance().level.getEntity(packet.holderId());
            if (holder != null) PlayerAnims.onReloadStart(holder, gun);
        }
        AnimationController<?> controller = controller(gun, packet.instanceId());
        if (controller == null) return;
        controller.forceAnimationReset();
        controller.tryTriggerAnimation(animation);
    }

    private static String resolveAnimation(GunItem gun, String name)
    {
        if (!name.startsWith("aim_")) return name;
        ResourceLocation file = ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "animations/" + gun.getAnimationID() + ".animation.json");
        software.bernie.geckolib.loading.object.BakedAnimations baked = software.bernie.geckolib.cache.GeckoLibCache.getBakedAnimations().get(file);
        if (baked == null || baked.getAnimation(name) != null) return name;
        String plain = name.substring("aim_".length());
        return baked.getAnimation(plain) != null ? plain : name;
    }

    @Override
    public void bulletHole(ModNetwork.BulletHole packet)
    {
        if (!net.nezerit6.animated_arsenal.config.GunConfig.clientLoaded() || net.nezerit6.animated_arsenal.config.GunConfig.BULLET_HOLES.get()) BulletHoles.add(packet.location(), packet.face(), packet.pos());
    }

    @Override
    public void onShot(net.minecraft.world.entity.Entity shooter)
    {
        GunArmAnimations.onShot(shooter);

        if (!net.nezerit6.animated_arsenal.config.GunConfig.clientLoaded() || net.nezerit6.animated_arsenal.config.GunConfig.DYNAMIC_LIGHTS.get()) DynamicLights.flash(shooter.getEyePosition().add(shooter.getLookAngle()), 15.0, 4);
    }

    @Override
    public void gunFired(int entityId, java.util.List<org.joml.Vector3f> directions)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        net.minecraft.world.entity.Entity shooter = mc.level.getEntity(entityId);
        if (shooter == null) return;
        if (shooter != mc.player)
        {
            onShot(shooter);
        }
        if (!net.nezerit6.animated_arsenal.config.GunConfig.clientLoaded() || net.nezerit6.animated_arsenal.config.GunConfig.BULLET_TRACERS.get())
        {
            BulletTracers.spawn(shooter, directions);
        }
    }

    private void onFov(ComputeFovModifierEvent event)
    {
        if (isAiming(event.getPlayer()))
        {
            event.setNewFovModifier(isAimingScoped(event.getPlayer()) ? 0.125f : 0.75f);
        }
    }

    private void onRenderHand(RenderHandEvent event)
    {
        LocalPlayer self = Minecraft.getInstance().player;
        if (self != null && holdingGun(self) && event.getHand() == InteractionHand.MAIN_HAND
                && self.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT && !isAimingScoped(self))
        {
            event.setCanceled(true);
            com.mojang.blaze3d.vertex.PoseStack pose = event.getPoseStack();
            pose.pushPose();
            pose.translate(0.56F, -0.52F + event.getEquipProgress() * -0.6F, -0.72F);
            Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(self, event.getItemStack(),
                    net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, false, pose, event.getMultiBufferSource(), event.getPackedLight());
            pose.popPose();
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (!holdingGun(player)) return;

        if (event.getHand() == InteractionHand.OFF_HAND || isAimingScoped(player))
        {
            event.setCanceled(true);
        }
    }

    private void onRenderGuiLayer(RenderGuiLayerEvent.Pre event)
    {
        Player player = Minecraft.getInstance().player;
        if (isAimingScoped(player) && Minecraft.getInstance().options.getCameraType().isFirstPerson()
                && !event.getName().equals(ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "gun_scope")))
        {
            event.setCanceled(true);
            return;
        }
        if (event.getName().equals(VanillaGuiLayers.CROSSHAIR) && isAiming(player))
        {
            event.setCanceled(true);
        }
    }

    private void renderScopeLayer(GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        this.gunScopeScale = Mth.lerp(0.5f * deltaTracker.getGameTimeDeltaTicks(), this.gunScopeScale, 1.125f);

        if (mc.options.getCameraType().isFirstPerson() && isAimingScoped(mc.player))
        {
            renderGunScopeOverlay(guiGraphics, this.gunScopeScale);
        }
        else
        {
            this.gunScopeScale = 0.5f;
        }
    }

    private static void renderGunScopeOverlay(GuiGraphics guiGraphics, float scale)
    {
        float f = (float) Math.min(guiGraphics.guiWidth(), guiGraphics.guiHeight());
        float h = Math.min((float) guiGraphics.guiWidth() / f, (float) guiGraphics.guiHeight() / f) * scale;
        int i = Mth.floor(f * h);
        int j = Mth.floor(f * h);
        int k = (guiGraphics.guiWidth() - i) / 2;
        int l = (guiGraphics.guiHeight() - j) / 2;
        int m = k + i;
        int n = l + j;
        RenderSystem.enableBlend();
        guiGraphics.blit(GUN_SCOPE, k, l, -90, 0.0F, 0.0F, i, j, i, j);
        RenderSystem.disableBlend();
        guiGraphics.fill(RenderType.guiOverlay(), 0, n, guiGraphics.guiWidth(), guiGraphics.guiHeight(), -90, 0xFF000000);
        guiGraphics.fill(RenderType.guiOverlay(), 0, 0, guiGraphics.guiWidth(), l, -90, 0xFF000000);
        guiGraphics.fill(RenderType.guiOverlay(), 0, l, k, n, -90, 0xFF000000);
        guiGraphics.fill(RenderType.guiOverlay(), m, l, guiGraphics.guiWidth(), n, -90, 0xFF000000);
    }
}