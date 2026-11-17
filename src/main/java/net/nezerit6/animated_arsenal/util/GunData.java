package net.nezerit6.animated_arsenal.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class GunData
{
    private GunData() {}

    public static CompoundTag read(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);

        return data == null ? new CompoundTag() : data.getUnsafe();
    }

    public static int getInt(ItemStack stack, String key)
    {
        return read(stack).getInt(key);
    }

    public static boolean getBoolean(ItemStack stack, String key)
    {
        return read(stack).getBoolean(key);
    }

    public static void putInt(ItemStack stack, String key, int value)
    {
        CompoundTag tag = read(stack);
        if (tag.contains(key) && tag.getInt(key) == value) return;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putInt(key, value));
    }

    public static void putBoolean(ItemStack stack, String key, boolean value)
    {
        CompoundTag tag = read(stack);
        if (tag.contains(key) && tag.getBoolean(key) == value) return;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, t -> t.putBoolean(key, value));
    }

    public static void update(ItemStack stack, java.util.function.Consumer<CompoundTag> updater)
    {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, updater);
    }
}