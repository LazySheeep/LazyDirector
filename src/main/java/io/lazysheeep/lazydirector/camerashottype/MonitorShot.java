package io.lazysheeep.lazydirector.camerashottype;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import io.lazysheeep.lazydirector.util.RandomUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MonitorShot extends CameraShotType
{
    private static final int MaxTryCount = 3;
    private static final int IterationsEachTry = 10;
    private static final float MaxBadViewTime = 3.0f;

    private static final float InitDistance = 5.0f;
    private static final float MaxDistance = 10.0f;

    private Location cameraLocation = null;

    private int tryCount = 0;
    private float badViewTimer = 0.0f;

    private @NotNull Location newCameraLocation(@NotNull Hotspot focus)
    {
        double pitch = RandomUtils.NextDouble(-90.0d, 90.0d);
        double yaw = RandomUtils.NextDouble(-180.0d, 180.0d);
        Vector direction = MathUtils.GetDirectionFromPitchAndYaw(pitch, yaw);
        cameraLocation = focus.getLocation().add(direction.clone().multiply(InitDistance).multiply(-1.0f)).setDirection(direction);
        return cameraLocation;
    }

    @Override
    public @Nullable Location updateCameraLocation(@NotNull Hotspot focus)
    {
        if(cameraLocation == null)
        {
            newCameraLocation(focus);
        }
        else
        {
            Vector nextDirection = focus.getLocation().toVector().subtract(cameraLocation.toVector()).normalize();
            cameraLocation.setDirection(nextDirection);
        }

        // check if the focus is not too far and visible from the camera
        if(MathUtils.Distance(cameraLocation, focus.getLocation()) <= MaxDistance && MathUtils.IsVisible(cameraLocation, focus.getLocation()))
        {
            tryCount = 0;
            badViewTimer = 0.0f;
        }
        else
        {
            badViewTimer += LazyDirector.GetServerTickDeltaTime();
            if(badViewTimer > MaxBadViewTime)
            {
                if(tryCount < MaxTryCount)
                {
                    int iteration = 0;
                    while (iteration < IterationsEachTry)
                    {
                        newCameraLocation(focus);
                        if (MathUtils.IsVisible(cameraLocation, focus.getLocation()))
                        {
                            // success
                            tryCount = -1;
                            break;
                        }
                        iteration++;
                    }
                    tryCount++;
                }
                else
                {
                    // fail
                    tryCount = 0;
                    cameraLocation = null;
                }
                badViewTimer = 0.0f;
            }
        }

        return cameraLocation;
    }
}
