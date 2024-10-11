package io.lazysheeep.lazydirector.camerashottype;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import io.lazysheeep.lazydirector.util.RandomUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// isometric view
public class BirdsEyeShot extends CameraShotType
{
    private static final int MaxTryCount = 3;
    private static final int IterationsEachTry = 10;
    private static final float MaxBadViewTime = 3.0f;

    private static final float MinDistance = 5.0f;
    private static final float MaxDistance = 10.0f;

    private double pitch = 45.0d;
    private double yaw = 0.0d;
    private float distance = MaxDistance;

    private int tryCount = 0;
    private float badViewTimer = 0.0f;

    public BirdsEyeShot() {}

    private @NotNull Location nextCameraLocation(@NotNull Hotspot focus)
    {
        Vector direction = MathUtils.GetDirectionFromPitchAndYaw(pitch, yaw);
        return focus.getLocation().add(direction.clone().multiply(distance).multiply(-1.0f)).setDirection(direction);
    }

    @Override
    public @Nullable Location updateCameraLocation(@NotNull Hotspot focus)
    {
        Location nextCameraLocation = nextCameraLocation(focus);
        // check if the focus is visible from the camera
        if(MathUtils.IsVisible(nextCameraLocation, focus.getLocation()))
        {
            tryCount = 0;
            badViewTimer = 0.0f;
            distance = MaxDistance;
        }
        else
        {
            badViewTimer += LazyDirector.GetServerTickDeltaTime();
            if(badViewTimer > MaxBadViewTime)
            {
                if(tryCount < MaxTryCount)
                {
                    int iteration = 0;
                    distance = MaxDistance;
                    while (iteration < IterationsEachTry)
                    {
                        pitch = RandomUtils.NextDouble(30.0d, 60.0d);
                        yaw = RandomUtils.NextDouble(-180.0d, 180.0d);
                        distance -= (MaxDistance - MinDistance) / IterationsEachTry;
                        nextCameraLocation = nextCameraLocation(focus);
                        if (MathUtils.IsVisible(nextCameraLocation, focus.getLocation()))
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
                    nextCameraLocation = null;
                }
                badViewTimer = 0.0f;
            }
        }
        return nextCameraLocation;
    }
}
