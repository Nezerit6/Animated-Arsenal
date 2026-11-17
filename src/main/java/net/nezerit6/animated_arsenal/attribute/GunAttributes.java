package net.nezerit6.animated_arsenal.attribute;

import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GunAttributes
{
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, AnimatedGuns.MOD_ID);

    public static final DeferredHolder<Attribute, Attribute> GUN_DAMAGE = ATTRIBUTES.register("gun_damage",
            () -> new RangedAttribute("attribute.name.animated_arsenal.gun_damage", 0.0, 0.0, 1024.0).setSyncable(true));

    public static void addToEntities(EntityAttributeModificationEvent event)
    {
        for (EntityType<? extends net.minecraft.world.entity.LivingEntity> type : event.getTypes())
        {
            if (!event.has(type, GUN_DAMAGE)) event.add(type, GUN_DAMAGE);
        }
    }
}