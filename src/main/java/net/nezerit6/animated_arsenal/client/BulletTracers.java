package net.nezerit6.animated_arsenal.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.nezerit6.animated_arsenal.config.GunConfig;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class BulletTracers
{
    private static final double SPEED = 20.0;
    private static final double LENGTH = 3.5;
    private static final float WIDTH = 0.025F;

    private record Tracer(Vec3 start, Vec3 dir, double distance, double bornAt) {}

    private static final List<Tracer> TRACERS = new ArrayList<>();
    private static ClientLevel tracersLevel;
    private static long ticks;

    private BulletTracers() {}

    public static void spawn(Entity shooter, List<Vector3f> directions)
    {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        if (level != tracersLevel)
        {
            TRACERS.clear();
            tracersLevel = level;
        }

        double range = (GunConfig.serverLoaded() ? GunConfig.BULLET_SPEED.get() * GunConfig.BULLET_LIFETIME.get() : 200.0);
        Vec3 eyes = shooter.getEyePosition();
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);

        for (Vector3f d : directions)
        {
            Vec3 dir = new Vec3(d.x(), d.y(), d.z()).normalize();
            Vec3 start = eyes.add(dir.scale(1.0)).add(0, -0.15, 0);
            Vec3 end = start.add(dir.scale(range));

            HitResult block = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
            if (block.getType() != HitResult.Type.MISS) end = block.getLocation();

            EntityHitResult entity = ProjectileUtil.getEntityHitResult(level, shooter, start, end,
                    shooter.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0), e -> !e.isSpectator() && e.isPickable());
            if (entity != null) end = entity.getLocation();

            TRACERS.add(new Tracer(start, dir, start.distanceTo(end), ticks + partial));
        }
        while (TRACERS.size() > 256) TRACERS.remove(0);
    }

    private static double speed()
    {
        return GunConfig.serverLoaded() ? GunConfig.BULLET_SPEED.get() : SPEED;
    }

    public static void tick()
    {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != tracersLevel)
        {
            TRACERS.clear();
            tracersLevel = level;
        }
        ticks++;
        TRACERS.removeIf(t -> (ticks - t.bornAt()) * speed() - LENGTH > t.distance());
    }

    public static void render(RenderLevelStageEvent event)
    {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || TRACERS.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        double now = ticks + partial;
        Vec3 cam = event.getCamera().getPosition();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        PoseStack.Pose pose = poseStack.last();

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lightning());

        Iterator<Tracer> it = TRACERS.iterator();
        while (it.hasNext())
        {
            Tracer t = it.next();
            double travelled = (now - t.bornAt()) * speed();
            double head = Math.min(travelled, t.distance());
            double tail = Math.max(0.0, travelled - LENGTH);
            if (tail >= head) continue;

            Vec3 a = t.start().add(t.dir().scale(tail));
            Vec3 b = t.start().add(t.dir().scale(head));
            Vec3 toCam = cam.subtract(a.add(b).scale(0.5));
            Vec3 side = t.dir().cross(toCam);
            if (side.lengthSqr() < 1.0E-8) continue;
            side = side.normalize().scale(WIDTH);

            quad(consumer, pose, a, b, side, 255, 220, 150, 0, 230);
            quad(consumer, pose, a, b, side.scale(2.5), 255, 170, 80, 0, 90);
        }

        buffers.endBatch(RenderType.lightning());
        poseStack.popPose();
    }

    private static void quad(VertexConsumer c, PoseStack.Pose pose, Vec3 a, Vec3 b, Vec3 side, int r, int g, int bl, int tailAlpha, int headAlpha)
    {
        c.addVertex(pose, (float) (a.x - side.x), (float) (a.y - side.y), (float) (a.z - side.z)).setColor(r, g, bl, tailAlpha);
        c.addVertex(pose, (float) (a.x + side.x), (float) (a.y + side.y), (float) (a.z + side.z)).setColor(r, g, bl, tailAlpha);
        c.addVertex(pose, (float) (b.x + side.x), (float) (b.y + side.y), (float) (b.z + side.z)).setColor(r, g, bl, headAlpha);
        c.addVertex(pose, (float) (b.x - side.x), (float) (b.y - side.y), (float) (b.z - side.z)).setColor(r, g, bl, headAlpha);
    }
}