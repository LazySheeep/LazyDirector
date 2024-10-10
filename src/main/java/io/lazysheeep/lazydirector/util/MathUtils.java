package io.lazysheeep.lazydirector.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

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
        if (start.getWorld() == end.getWorld())
        {
            double distance = start.distance(end);
            if(distance < 0.1)
            {
                return end.clone();
            }
            else if(distance < 64.0)
            {
                Location result = start.clone();
                result.add(end.clone().subtract(start).multiply(t));
                // lerp pitch and yaw
                // note that pitch is in [-90.0, 90.0] and yaw is in [-180.0, 180.0]
                result.setPitch(Lerp(start.getPitch(), end.getPitch(), t));
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
                result.setYaw(resultYaw);

                return result;
            }
            else
            {
                return end.clone();
            }
        }
        else
        {
            return end.clone();
        }
    }
}
