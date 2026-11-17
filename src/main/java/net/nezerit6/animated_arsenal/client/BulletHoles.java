package net.nezerit6.animated_arsenal.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.nezerit6.animated_arsenal.AnimatedGuns;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public final class BulletHoles
{
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AnimatedGuns.MOD_ID, "textures/misc/bullet_hole.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);

    private static final int MAX_HOLES = 300;
    private static final int LIFETIME_TICKS = 20 * 30;
    private static final int FADE_TICKS = 40;

    private static final float HALF_SIZE = 0.0625F;

    private static final double OFFSET = 0.004;

    private record Hole(Vec3 center, Direction face, BlockPos pos, BlockState state, float rotation, long createdAt) {}

    private static final Deque<Hole> HOLES = new ArrayDeque<>();
    private static final RandomSource RANDOM = RandomSource.create();
    private static ClientLevel holesLevel;
    private static long ticks;

    private BulletHoles() {}

    public static void add(Vec3 location, Direction face, BlockPos pos)
    {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        if (level != holesLevel)
        {
            HOLES.clear();
            holesLevel = level;
        }

        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;

        if (state.getCollisionShape(level, pos).isEmpty()) return;

        Vec3 center = location.add(face.getStepX() * OFFSET, face.getStepY() * OFFSET, face.getStepZ() * OFFSET);
        HOLES.addLast(new Hole(center, face, pos, state, 0.0F, ticks));
        while (HOLES.size() > MAX_HOLES) HOLES.removeFirst();
    }

    private static int lifetime()
    {
        return net.nezerit6.animated_arsenal.config.GunConfig.clientLoaded() ? net.nezerit6.animated_arsenal.config.GunConfig.BULLET_HOLE_SECONDS.get() * 20 : LIFETIME_TICKS;
    }

    public static void tick()
    {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != holesLevel)
        {
            HOLES.clear();
            holesLevel = level;
        }
        ticks++;
        if (level == null || HOLES.isEmpty()) return;

        Iterator<Hole> it = HOLES.iterator();
        while (it.hasNext())
        {
            Hole hole = it.next();
            if (ticks - hole.createdAt() > lifetime() || level.getBlockState(hole.pos()) != hole.state())
            {
                it.remove();
            }
        }
    }

    public static void render(RenderLevelStageEvent event)
    {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES || HOLES.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || level != holesLevel) return;

        Vec3 cam = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        PoseStack.Pose pose = poseStack.last();

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RENDER_TYPE);

        for (Hole hole : HOLES)
        {
            if (hole.center().distanceToSqr(cam) > 64 * 64) continue;

            long age = ticks - hole.createdAt();
            float alpha = age > lifetime() - FADE_TICKS ? (lifetime() - age) / (float) FADE_TICKS : 1.0F;
            int a = Math.max(0, Math.min(255, (int) (alpha * 255)));
            int light = LevelRenderer.getLightColor(level, hole.pos().relative(hole.face()));

            Vector3f normal = hole.face().step();

            Vector3f u = Math.abs(normal.y()) > 0.5F ? new Vector3f(1, 0, 0) : new Vector3f(0, 1, 0);
            Vector3f v = new Vector3f(normal).cross(u);
            u = new Vector3f(v).cross(normal);
            float cos = (float) Math.cos(hole.rotation()), sin = (float) Math.sin(hole.rotation());
            Vector3f ru = new Vector3f(u).mul(cos).add(new Vector3f(v).mul(sin)).mul(HALF_SIZE);
            Vector3f rv = new Vector3f(v).mul(cos).sub(new Vector3f(u).mul(sin)).mul(HALF_SIZE);

            float cx = (float) hole.center().x, cy = (float) hole.center().y, cz = (float) hole.center().z;
            vertex(consumer, pose, cx - ru.x - rv.x, cy - ru.y - rv.y, cz - ru.z - rv.z, 0, 1, a, light, normal);
            vertex(consumer, pose, cx + ru.x - rv.x, cy + ru.y - rv.y, cz + ru.z - rv.z, 1, 1, a, light, normal);
            vertex(consumer, pose, cx + ru.x + rv.x, cy + ru.y + rv.y, cz + ru.z + rv.z, 1, 0, a, light, normal);
            vertex(consumer, pose, cx - ru.x + rv.x, cy - ru.y + rv.y, cz - ru.z + rv.z, 0, 0, a, light, normal);
        }

        buffers.endBatch(RENDER_TYPE);
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int alpha, int light, Vector3f normal)
    {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, normal.x(), normal.y(), normal.z());
    }
}