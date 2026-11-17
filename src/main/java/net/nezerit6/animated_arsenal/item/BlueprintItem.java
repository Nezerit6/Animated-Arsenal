package net.nezerit6.animated_arsenal.item;

import net.nezerit6.animated_arsenal.screen.BlueprintScreenHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class BlueprintItem extends Item
{
    public static final List<BlueprintItem> BLUEPRINT_ITEM_LIST = new ArrayList<>();

    public BlueprintItem(Properties settings)
    {
        super(settings);
        BLUEPRINT_ITEM_LIST.add(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        ItemStack itemStack = user.getItemInHand(hand);

        user.openMenu(createScreenHandlerFactory());
        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack)
    {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack)
    {
        return new ItemStack(ModItems.BLUEPRINT_BUNDLE.get());
    }

    static MenuProvider createScreenHandlerFactory()
    {
        return new SimpleMenuProvider((syncId, playerInventory, playerEntity) ->
                new BlueprintScreenHandler(syncId, playerInventory), Component.translatable("container.blueprints"));
    }
}