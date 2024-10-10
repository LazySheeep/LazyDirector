package io.lazysheeep.lazydirector.camerashottype;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import io.lazysheeep.lazydirector.util.RandomUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BirdsEyeShot extends CameraShotType
{
    private static final int maxTryCount = 3;
    private static final int iterationEachTry = 10;
    private static final float maxViewBlockedTime = 3.0f;

    private double pitch = 45.0d;
    private double yaw = 0.0d;
    private float distance = 8.0f;

    private int tryCount = 0;
    private float viewBlockedTimer = 0.0f;

    public BirdsEyeShot() {}

    public @NotNull Location nextCameraLocation(@NotNull Hotspot focus)
    {
        Vector direction = MathUtils.GetDirectionFromPitchAndYaw(pitch, yaw);
        return focus.getLocation().add(direction.clone().multiply(distance).multiply(-1.0f)).setDirection(direction);
    }

    public @NotNull Location newCameraLocation(@NotNull Hotspot focus)
    {
        pitch = RandomUtils.NextDouble(30.0f, 60.0d);
        yaw = RandomUtils.NextDouble(-180.0f, 180.0d);
        distance = RandomUtils.NextFloat(5.0f, 10.0f);
        return nextCameraLocation(focus);
    }

    @Override
    public @Nullable Location updateCameraLocation(@NotNull Hotspot focus)
    {
        Location nextCameraLocation = nextCameraLocation(focus);
        if(MathUtils.IsVisible(nextCameraLocation, focus.getLocation()))
        {
            tryCount = 0;
            viewBlockedTimer = 0.0f;
        }
        else
        {
            viewBlockedTimer += LazyDirector.GetServerTickDeltaTime();
            if(viewBlockedTimer > maxViewBlockedTime)
            {
                if(tryCount < maxTryCount)
                {
                    int iteration = 0;
                    while (iteration < iterationEachTry)
                    {
                        nextCameraLocation = newCameraLocation(focus);
                        if (MathUtils.IsVisible(nextCameraLocation, focus.getLocation()))
                        {
                            tryCount = -1;
                            break;
                        }
                        iteration++;
                    }
                    tryCount++;
                }
                else
                {
                    tryCount = 0;
                    nextCameraLocation = null;
                }
                viewBlockedTimer = 0.0f;
            }
        }
        return nextCameraLocation;
    }
}
