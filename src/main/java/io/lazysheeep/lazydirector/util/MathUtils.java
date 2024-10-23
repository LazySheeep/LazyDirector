package io.lazysheeep.lazydirector.util;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MathUtils
{
    public static int Clamp(int value, int min, int max)
    {
        return Math.max(min, Math.min(max, value));
    }

    public static float Clamp(float value, float min, float max)
    {
        return Math.max(min, Math.min(max, value));
    }

    public static float Lerp(float start, float end, float t)
    {
        return (1.0f - t) * start + t * end;
    }

    public static @NotNull Location Lerp(@NotNull Location start, @NotNull Location end, float t)
    {
        Location result = start.clone();

        final double minDistance = 0.1;
        final double maxDistance = 128.0;
        final float minRotation = 0.1f;

        // lerp position
        double distance = Distance(start, end);
        if (distance < minDistance || distance > maxDistance)
        {
            result.set(end.getX(), end.getY(), end.getZ());
            result.setWorld(end.getWorld());
        }
        else
        {
            result.add(end.clone().subtract(start).multiply(t));
        }

        // lerp pitch
        // note that pitch is in [-90.0, 90.0]
        if(Math.abs(end.getPitch() - start.getPitch()) < minRotation)
        {
            result.setPitch(end.getPitch());
        }
        else
        {
            result.setPitch(Lerp(start.getPitch(), end.getPitch(), t));
        }

        // lerp yaw
        // note that yaw is in [-180.0, 180.0]
        float startYaw = start.getYaw();
        float endYaw = end.getYaw();
        if (Math.abs(endYaw - startYaw) > 180.0f)
        {
            if (endYaw > startYaw)
            {
                startYaw += 360.0f;
            }
            else
            {
                endYaw += 360.0f;
            }
        }
        float resultYaw = Lerp(startYaw, endYaw, t);
        if (resultYaw < -180.0f)
        {
            resultYaw += 360.0f;
        }
        else if (resultYaw > 180.0f)
        {
            resultYaw -= 360.0f;
        }

        if(Math.abs(resultYaw - end.getYaw()) < minRotation)
        {
            result.setYaw(end.getYaw());
        }
        else
        {
            result.setYaw(resultYaw);
        }

        return result;
    }

    public static double Distance(@NotNull Location start, @NotNull Location end)
    {
        return start.getWorld() == end.getWorld() ? start.distance(end) : Double.MAX_VALUE;
    }

    public static float UnitMapTo(float value, float minMapTo, float maxMapTo)
    {
        return minMapTo + (maxMapTo - minMapTo) * value;
    }

    public static float MapToUnit(float value, float mapFromMin, float mapFromMax)
    {
        return (value - mapFromMin) / (mapFromMax - mapFromMin);
    }

    public static float Map(float value, float mapFromMin, float mapFromMax, float minMapTo, float maxMapTo)
    {
        float unit = MapToUnit(value, mapFromMin, mapFromMax);
        return UnitMapTo(unit, minMapTo, maxMapTo);
    }

    public static @NotNull Vector GetDirectionFromPitchAndYaw(double pitch, double yaw)
    {
        Vector vector = new Vector();
        vector.setY(-Math.sin(Math.toRadians(pitch)));
        double xz = Math.cos(Math.toRadians(pitch));
        vector.setX(-xz * Math.sin(Math.toRadians(yaw)));
        vector.setZ(xz * Math.cos(Math.toRadians(yaw)));
        return vector;
    }

    public static void LookAt(@NotNull Location origin, @NotNull Location target)
    {
        double dx = target.getX() - origin.getX();
        double dy = target.getY() - origin.getY();
        double dz = target.getZ() - origin.getZ();
        origin.setDirection(new Vector(dx, dy, dz));
    }

    public static @Nullable RayTraceResult RayTrace(@NotNull Location start, @NotNull Location end)
    {
        return start.getWorld()
                    .rayTraceBlocks(start, end.toVector()
                                              .subtract(start.toVector()), start.distance(end), FluidCollisionMode.NEVER, true);
    }

    public static boolean IsVisible(@NotNull Location start, @NotNull Location end)
    {
        RayTraceResult result = start.getWorld()
                                     .rayTraceBlocks(start, end.toVector()
                                                               .subtract(start.toVector()), start.distance(end), FluidCollisionMode.NEVER, true);
        return result == null;
    }
}
