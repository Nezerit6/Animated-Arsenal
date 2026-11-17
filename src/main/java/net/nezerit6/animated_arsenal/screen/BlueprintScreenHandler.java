package net.nezerit6.animated_arsenal.screen;

import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.nezerit6.animated_arsenal.item.BlueprintBundleItem;
import net.nezerit6.animated_arsenal.item.BlueprintItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class BlueprintScreenHandler extends AbstractContainerMenu
{
    public BlueprintScreenHandler(int syncId, Inventory playerInventory)
    {
        super(AnimatedGuns.BLUEPRINT_SCREEN_HANDLER_TYPE.get(), syncId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player)
    {
        return player.getMainHandItem().getItem() instanceof BlueprintItem
                || player.getOffhandItem().getItem() instanceof BlueprintItem
                || player.getMainHandItem().getItem() instanceof BlueprintBundleItem
                || player.getOffhandItem().getItem() instanceof BlueprintBundleItem;
    }
}