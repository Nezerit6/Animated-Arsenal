package net.nezerit6.animated_arsenal.compat;

import net.minecraft.world.entity.Entity;

public final class CrawlCompat
{
    private CrawlCompat() {}

    public static boolean isCrawling(Entity entity)
    {
        return entity != null && "CRAWLING".equals(entity.getPose().name());
    }
}