package net.nezerit6.animated_arsenal.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class InventoryUtil
{
    public static int itemCountInInventory(Player player, Item item)
    {
        int itemCount = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++)
        {
            ItemStack currentStack = player.getInventory().getItem(i);
            if (!currentStack.isEmpty() && currentStack.is(item))
            {
                itemCount += currentStack.getCount();
            }
        }
        return Math.max(itemCount, 0);
    }

    public static void removeItemFromInventory(Player player, Item item, int count)
    {
        int itemsToRemove = count;

        for (int i = 0; i < player.getInventory().getContainerSize() && itemsToRemove > 0; i++)
        {
            ItemStack currentStack = player.getInventory().getItem(i);
            if (!currentStack.isEmpty() && currentStack.is(item))
            {
                if (currentStack.getCount() > itemsToRemove)
                {
                    currentStack.shrink(itemsToRemove);
                    itemsToRemove = 0;
                }
                else
                {
                    itemsToRemove -= currentStack.getCount();
                    currentStack.setCount(0);
                }
            }
        }
    }
}