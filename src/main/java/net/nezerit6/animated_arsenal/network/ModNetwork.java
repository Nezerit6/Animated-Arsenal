package net.nezerit6.animated_arsenal.network;

import io.netty.buffer.ByteBuf;
import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.nezerit6.animated_arsenal.item.BlueprintBundleItem;
import net.nezerit6.animated_arsenal.item.BlueprintItem;
import net.nezerit6.animated_arsenal.item.GunItem;
import net.nezerit6.animated_arsenal.util.GunData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.Consumer;

public final class ModNetwork
{
    private ModNetwork() {}

    public static ClientHandler CLIENT_HANDLER = null;

    public interface ClientHandler
    {
        void recoil(Recoil packet);
        void gunAnim(GunAnim packet);
        void bulletHole(BulletHole packet);

        void onShot(net.minecraft.world.entity.Entity shooter);
        void gunFired(int entityId, java.util.List<org.joml.Vector3f> directions);

        void gunInventoryTick(GunItem gun, ItemStack stack, net.minecraft.world.entity.player.Player player);
    }

    private static ResourceLocation id(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, path);
    }

    public record Reload(boolean reloading) implements CustomPacketPayload
    {
        public static final Type<Reload> TYPE = new Type<>(id("reload"));
        public static final StreamCodec<ByteBuf, Reload> CODEC = ByteBufCodecs.BOOL.map(Reload::new, Reload::reloading);
        @Override public Type<Reload> type() { return TYPE; }
    }

    public record SelectBlueprint(int index) implements CustomPacketPayload
    {
        public static final Type<SelectBlueprint> TYPE = new Type<>(id("select_blueprint"));
        public static final StreamCodec<ByteBuf, SelectBlueprint> CODEC = ByteBufCodecs.VAR_INT.map(SelectBlueprint::new, SelectBlueprint::index);
        @Override public Type<SelectBlueprint> type() { return TYPE; }
    }

    public record MeleeServer(int slot) implements CustomPacketPayload
    {
        public static final Type<MeleeServer> TYPE = new Type<>(id("gun_melee_server"));
        public static final StreamCodec<ByteBuf, MeleeServer> CODEC = ByteBufCodecs.VAR_INT.map(MeleeServer::new, MeleeServer::slot);
        @Override public Type<MeleeServer> type() { return TYPE; }
    }

    public record Aim(boolean aim) implements CustomPacketPayload
    {
        public static final Type<Aim> TYPE = new Type<>(id("aim"));
        public static final StreamCodec<ByteBuf, Aim> CODEC = ByteBufCodecs.BOOL.map(Aim::new, Aim::aim);
        @Override public Type<Aim> type() { return TYPE; }
    }

    public record Recoil(float kick, double hKick) implements CustomPacketPayload
    {
        public static final Type<Recoil> TYPE = new Type<>(id("recoil"));
        public static final StreamCodec<ByteBuf, Recoil> CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, Recoil::kick,
                ByteBufCodecs.DOUBLE, Recoil::hKick,
                Recoil::new);
        @Override public Type<Recoil> type() { return TYPE; }
    }

    public record GunFired(int entityId, java.util.List<org.joml.Vector3f> directions) implements CustomPacketPayload
    {
        public static final Type<GunFired> TYPE = new Type<>(id("gun_fired"));
        public static final StreamCodec<ByteBuf, GunFired> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, GunFired::entityId,
                ByteBufCodecs.VECTOR3F.apply(ByteBufCodecs.list(64)), GunFired::directions,
                GunFired::new);
        @Override public Type<GunFired> type() { return TYPE; }
    }

    public record BulletHole(net.minecraft.world.phys.Vec3 location, net.minecraft.core.Direction face, net.minecraft.core.BlockPos pos) implements CustomPacketPayload
    {
        public static final Type<BulletHole> TYPE = new Type<>(id("bullet_hole"));
        public static final StreamCodec<ByteBuf, BulletHole> CODEC = StreamCodec.composite(
                ByteBufCodecs.VECTOR3F.map(net.minecraft.world.phys.Vec3::new, net.minecraft.world.phys.Vec3::toVector3f), BulletHole::location,
                net.minecraft.core.Direction.STREAM_CODEC, BulletHole::face,
                net.minecraft.core.BlockPos.STREAM_CODEC, BulletHole::pos,
                BulletHole::new);
        @Override public Type<BulletHole> type() { return TYPE; }
    }

    public record GunAnim(ResourceLocation item, long instanceId, String animation, int holderId) implements CustomPacketPayload
    {
        public static final Type<GunAnim> TYPE = new Type<>(id("gun_anim"));
        public static final StreamCodec<ByteBuf, GunAnim> CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, GunAnim::item,
                ByteBufCodecs.VAR_LONG, GunAnim::instanceId,
                ByteBufCodecs.STRING_UTF8, GunAnim::animation,
                ByteBufCodecs.VAR_INT, GunAnim::holderId,
                GunAnim::new);
        @Override public Type<GunAnim> type() { return TYPE; }
    }

    public static void register(RegisterPayloadHandlersEvent event)
    {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(Reload.TYPE, Reload.CODEC, (packet, ctx) -> onServer(ctx, player ->
        {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof GunItem gun)
            {
                GunData.putBoolean(stack, "isReloading", packet.reloading());
                gun.playAnimation(player, stack, GunData.getBoolean(stack, "isAiming") ? "aim_reload_start" : "reload_start");
                GunData.putBoolean(stack, "isAiming", false);
            }
        }));

        registrar.playToServer(SelectBlueprint.TYPE, SelectBlueprint.CODEC, (packet, ctx) -> onServer(ctx, player ->
        {
            int i = packet.index();
            if (i < 0 || i >= BlueprintItem.BLUEPRINT_ITEM_LIST.size()) return;
            Item blueprint = BlueprintItem.BLUEPRINT_ITEM_LIST.get(i);

            if (isBlueprint(player.getMainHandItem()))
            {
                player.getMainHandItem().shrink(1);
            }
            else if (isBlueprint(player.getOffhandItem()))
            {
                player.getOffhandItem().shrink(1);
            }
            else
            {
                return;
            }

            if (player.getInventory().getFreeSlot() > -1)
            {
                player.getInventory().add(new ItemStack(blueprint));
            }
            else
            {
                player.drop(new ItemStack(blueprint), false, true);
            }
        }));

        registrar.playToServer(MeleeServer.TYPE, MeleeServer.CODEC, (packet, ctx) -> onServer(ctx, player ->
        {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof GunItem gun)) return;

            if (player.getCooldowns().isOnCooldown(gun)) return;

            if (GunData.getBoolean(stack, "isReloading") || GunData.getInt(stack, "reloadTick") > 0) return;
            if (packet.slot() != player.getInventory().selected) return;

            if (net.nezerit6.animated_arsenal.compat.CrawlCompat.isCrawling(player)) return;

            GunData.putBoolean(stack, "isAiming", false);
            player.getCooldowns().addCooldown(gun, (net.nezerit6.animated_arsenal.config.GunConfig.serverLoaded() ? net.nezerit6.animated_arsenal.config.GunConfig.MELEE_COOLDOWN.get() : GunItem.MELEE_COOLDOWN));

            gun.playAnimation(player, stack, "melee");
        }));

        registrar.playToServer(Aim.TYPE, Aim.CODEC, (packet, ctx) -> onServer(ctx, player ->
        {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() instanceof GunItem gun)
            {
                player.setSprinting(false);
                gun.aimAnimation(stack, packet.aim(), player);
            }
        }));

        registrar.playToClient(Recoil.TYPE, Recoil.CODEC, (packet, ctx) ->
        {
            if (CLIENT_HANDLER != null) CLIENT_HANDLER.recoil(packet);
        });
        registrar.playToClient(GunFired.TYPE, GunFired.CODEC, (packet, ctx) ->
        {
            if (CLIENT_HANDLER != null) CLIENT_HANDLER.gunFired(packet.entityId(), packet.directions());
        });
        registrar.playToClient(BulletHole.TYPE, BulletHole.CODEC, (packet, ctx) ->
        {
            if (CLIENT_HANDLER != null) CLIENT_HANDLER.bulletHole(packet);
        });
        registrar.playToClient(GunAnim.TYPE, GunAnim.CODEC, (packet, ctx) ->
        {
            if (CLIENT_HANDLER != null) CLIENT_HANDLER.gunAnim(packet);
        });
    }

    private static boolean isBlueprint(ItemStack stack)
    {
        return stack.getItem() instanceof BlueprintItem || stack.getItem() instanceof BlueprintBundleItem;
    }

    private static void onServer(IPayloadContext ctx, Consumer<ServerPlayer> action)
    {
        if (ctx.player() instanceof ServerPlayer player && player.level() instanceof ServerLevel)
        {
            action.accept(player);
        }
    }

    public static ResourceLocation itemId(Item item)
    {
        return BuiltInRegistries.ITEM.getKey(item);
    }
}