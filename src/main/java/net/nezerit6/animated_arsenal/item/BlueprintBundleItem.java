package net.nezerit6.animated_arsenal.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BlueprintBundleItem extends Item
{
    public BlueprintBundleItem(Properties settings)
    {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand)
    {
        ItemStack itemStack = user.getItemInHand(hand);

        user.openMenu(BlueprintItem.createScreenHandlerFactory());
        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
    }
}