package net.nezerit6.animated_arsenal.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BulletUtil
{
    public static Vec3 horiSpread(Player player, float x_spread)
    {
        return rotate(player.getUpVector(1.0f), player.getViewVector(1.0f), x_spread);
    }

    public static Vec3 vertiSpread(Player player, float y_spread)
    {
        return rotate(getOppositeRotationVector(0, player.getViewYRot(1.0f) - 90), player.getViewVector(1.0f), y_spread);
    }

    private static Vec3 rotate(Vec3 axis, Vec3 vec, float degrees)
    {
        Vector3f ax = axis.toVector3f().normalize();
        Quaternionf q = new Quaternionf().fromAxisAngleDeg(ax.x, ax.y, ax.z, degrees);
        Vector3f v = vec.toVector3f().rotate(q);
        return new Vec3(v.x, v.y, v.z);
    }

    protected static Vec3 getOppositeRotationVector(float pitch, float yaw)
    {
        float f = pitch * ((float) Math.PI / 180);
        float g = -yaw * ((float) Math.PI / 180);
        float h = Mth.cos(g);
        float i = Mth.sin(g);
        float j = Mth.cos(f);
        float k = Mth.sin(f);
        return new Vec3(i * j, -k, h * j);
    }
}