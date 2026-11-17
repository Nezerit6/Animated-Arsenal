package net.nezerit6.animated_arsenal;

import net.nezerit6.animated_arsenal.item.ModItems;
import net.nezerit6.animated_arsenal.network.ModNetwork;
import net.nezerit6.animated_arsenal.screen.BlueprintScreenHandler;
import net.nezerit6.animated_arsenal.sound.ModSounds;
import net.nezerit6.animated_arsenal.util.GunData;
import net.nezerit6.animated_arsenal.item.GunItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(AnimatedGuns.MOD_ID)
public class AnimatedGuns
{
	public static final String MOD_ID = "animated_arsenal";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MOD_ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

	public static final DeferredHolder<MenuType<?>, MenuType<BlueprintScreenHandler>> BLUEPRINT_SCREEN_HANDLER_TYPE =
			MENU_TYPES.register("blueprint_screenhandler", () -> new MenuType<>(BlueprintScreenHandler::new, FeatureFlags.DEFAULT_FLAGS));

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GUNS = CREATIVE_TABS.register("guns", () -> CreativeModeTab.builder()
			.title(Component.translatable("guns"))
			.icon(() -> new ItemStack(ModItems.MAGNUM_REVOLVER.get()))
			.displayItems((params, output) -> ModItems.GUNS_TAB_ITEMS.forEach(i -> output.accept(i.get())))
			.build());

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CRAFTING = CREATIVE_TABS.register("crafting", () -> CreativeModeTab.builder()
			.title(Component.translatable("crafting"))
			.icon(() -> new ItemStack(ModItems.MAGNUM_REVOLVER_BLUEPRINT.get()))
			.displayItems((params, output) -> ModItems.CRAFTING_TAB_ITEMS.forEach(i -> output.accept(i.get())))
			.build());

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ATTACHMENTS = CREATIVE_TABS.register("attachments", () -> CreativeModeTab.builder()
			.title(Component.translatable("attachments"))
			.icon(() -> new ItemStack(ModItems.SIGHT_HOLO.get()))
			.displayItems((params, output) -> ModItems.ATTACHMENTS_TAB_ITEMS.forEach(i -> output.accept(i.get())))
			.build());

	public AnimatedGuns(IEventBus modBus, net.neoforged.fml.ModContainer container)
	{
		modBus.addListener(net.neoforged.bus.api.EventPriority.LOWEST, net.neoforged.neoforge.registries.RegisterEvent.class, e ->
		{
			if (!e.getRegistryKey().equals(Registries.ITEM)) return;
			net.nezerit6.animated_arsenal.config.GunConfig.build();
			container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, net.nezerit6.animated_arsenal.config.GunConfig.SERVER_SPEC);
			container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, net.nezerit6.animated_arsenal.config.GunConfig.CLIENT_SPEC);
		});
		ModItems.ITEMS.register(modBus);
		ModSounds.SOUNDS.register(modBus);
		net.nezerit6.animated_arsenal.attribute.GunAttributes.ATTRIBUTES.register(modBus);
		modBus.addListener(net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent.class, net.nezerit6.animated_arsenal.attribute.GunAttributes::addToEntities);
		MENU_TYPES.register(modBus);
		CREATIVE_TABS.register(modBus);
		modBus.addListener(ModNetwork::register);
		net.nezerit6.animated_arsenal.bullet.BulletManager.register();
		net.nezerit6.animated_arsenal.advancement.DragonAdvancements.register();

		NeoForge.EVENT_BUS.addListener(ItemTossEvent.class, event ->
		{
			ItemStack stack = event.getEntity().getItem();
			if (stack.getItem() instanceof GunItem)
			{
				GunData.putInt(stack, "reloadTick", 0);
				GunData.putBoolean(stack, "isReloading", false);
				GunData.putBoolean(stack, "isAiming", false);
			}
		});

		NeoForge.EVENT_BUS.addListener(LivingSwapItemsEvent.Hands.class, event ->
		{
			if (event.getItemSwappedToOffHand().getItem() instanceof GunItem && !(net.nezerit6.animated_arsenal.config.GunConfig.serverLoaded() && net.nezerit6.animated_arsenal.config.GunConfig.ALLOW_OFFHAND.get()))
			{
				event.setCanceled(true);
			}
		});

		NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, event ->
		{
			Player player = event.getEntity();
			if (player.level().isClientSide()) return;

			ItemStack offhand = player.getOffhandItem();
			if (offhand.getItem() instanceof GunItem && !(net.nezerit6.animated_arsenal.config.GunConfig.serverLoaded() && net.nezerit6.animated_arsenal.config.GunConfig.ALLOW_OFFHAND.get()))
			{
				player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
				if (!player.getInventory().add(offhand))
				{
					player.drop(offhand, false);
				}
			}
		});
	}
}