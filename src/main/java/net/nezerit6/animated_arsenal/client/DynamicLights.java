package net.nezerit6.animated_arsenal.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class DynamicLights
{
    public static final double MAX_RADIUS = 7.75;

    private static final int DIRTY_RADIUS = 9;

    private record LightPoint(double x, double y, double z, double luminance) {}

    private static final class Source
    {
        Vec3 pos;
        double baseLuminance;

        long start;
        int duration;
        boolean tracked;
        long lastSeen;

        Vec3 renderedPos;
        double renderedLuminance;
    }

    private static final Map<Object, Source> SOURCES = new HashMap<>();
    private static volatile LightPoint[] snapshot = new LightPoint[0];
    private static ClientLevel sourcesLevel;
    private static long ticks;

    private DynamicLights() {}

    public static void flash(Vec3 pos, double luminance, int durationTicks)
    {
        Source s = new Source();
        s.pos = pos;
        s.baseLuminance = luminance;
        s.start = ticks;
        s.duration = Math.max(1, durationTicks);
        SOURCES.put(new Object(), s);
    }

    public static void track(Entity entity, double luminance)
    {
        Source s = SOURCES.computeIfAbsent(entity.getUUID(), k -> new Source());
        s.pos = entity.position();
        s.baseLuminance = luminance;
        s.tracked = true;
        s.lastSeen = ticks;
    }

    public static void tick()
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != sourcesLevel)
        {
            SOURCES.clear();
            snapshot = new LightPoint[0];
            sourcesLevel = mc.level;
            return;
        }
        if (mc.level == null) return;

        if (!SOURCES.isEmpty() || snapshot.length > 0)
        {
            Set<Long> dirty = new HashSet<>();
            Iterator<Source> it = SOURCES.values().iterator();
            while (it.hasNext())
            {
                Source s = it.next();
                double luminance;
                boolean expired;
                if (s.tracked)
                {
                    expired = s.lastSeen < ticks;
                    luminance = s.baseLuminance;
                }
                else
                {
                    long age = ticks - s.start;
                    expired = age >= s.duration;
                    luminance = s.baseLuminance * (1.0 - (double) age / s.duration);
                }

                if (expired)
                {
                    if (s.renderedPos != null) markDirty(dirty, s.renderedPos);
                    it.remove();
                    continue;
                }

                if (s.renderedPos == null || !s.renderedPos.equals(s.pos) || s.renderedLuminance != luminance)
                {
                    if (s.renderedPos != null) markDirty(dirty, s.renderedPos);
                    markDirty(dirty, s.pos);
                    s.renderedPos = s.pos;
                    s.renderedLuminance = luminance;
                }
            }

            LightPoint[] points = new LightPoint[SOURCES.size()];
            int i = 0;
            for (Source s : SOURCES.values())
            {
                points[i++] = new LightPoint(s.renderedPos.x, s.renderedPos.y, s.renderedPos.z, s.renderedLuminance);
            }
            snapshot = points;

            for (long section : dirty)
            {
                mc.levelRenderer.setSectionDirty(SectionPos.x(section), SectionPos.y(section), SectionPos.z(section));
            }
        }

        ticks++;
    }

    private static void markDirty(Set<Long> dirty, Vec3 pos)
    {
        int minX = SectionPos.blockToSectionCoord(pos.x - DIRTY_RADIUS);
        int maxX = SectionPos.blockToSectionCoord(pos.x + DIRTY_RADIUS);
        int minY = SectionPos.blockToSectionCoord(pos.y - DIRTY_RADIUS);
        int maxY = SectionPos.blockToSectionCoord(pos.y + DIRTY_RADIUS);
        int minZ = SectionPos.blockToSectionCoord(pos.z - DIRTY_RADIUS);
        int maxZ = SectionPos.blockToSectionCoord(pos.z + DIRTY_RADIUS);
        for (int x = minX; x <= maxX; x++)
            for (int y = minY; y <= maxY; y++)
                for (int z = minZ; z <= maxZ; z++)
                    dirty.add(SectionPos.asLong(x, y, z));
    }

    public static double lightAt(BlockPos pos)
    {
        LightPoint[] points = snapshot;
        if (points.length == 0) return 0.0;

        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        double best = 0.0;
        for (LightPoint p : points)
        {
            double dx = p.x - cx, dy = p.y - cy, dz = p.z - cz;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > MAX_RADIUS * MAX_RADIUS) continue;
            double light = p.luminance * (1.0 - Math.sqrt(distSq) / MAX_RADIUS);
            if (light > best) best = light;
        }
        return Math.min(best, 15.0);
    }

    public static boolean isActive()
    {
        return snapshot.length > 0;
    }
}