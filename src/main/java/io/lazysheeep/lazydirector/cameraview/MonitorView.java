package io.lazysheeep.lazydirector.cameraview;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import io.lazysheeep.lazydirector.util.RandomUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

public class MonitorView extends CameraView
{
    private final float initDistance;
    private final float maxDistance;
    private final float criticalDistance;
    private final boolean enableVisibilityCheck;
    private final float maxBadViewTime;
    private final int retriesWhenBadView;

    private Location cameraLocation;
    private float badViewTimer;

    public MonitorView(@NotNull ConfigurationNode configNode)
    {
        initDistance = configNode.node("initDistance").getFloat(0.0f);
        maxDistance = configNode.node("maxDistance").getFloat(Float.MAX_VALUE);
        criticalDistance = configNode.node("criticalDistance").getFloat(Float.MAX_VALUE);
        enableVisibilityCheck = configNode.node("enableVisibilityCheck").getBoolean(false);
        maxBadViewTime = configNode.node("maxBadViewTime").getFloat(Float.MAX_VALUE);
        retriesWhenBadView = configNode.node("retriesWhenBadView").getInt(1);
        reset();
    }

    private @NotNull Location newCameraLocation(@NotNull Hotspot focus)
    {
        double pitch = RandomUtils.NextDouble(-90.0d, 90.0d);
        double yaw = RandomUtils.NextDouble(-180.0d, 180.0d);
        Vector direction = MathUtils.GetDirectionFromPitchAndYaw(pitch, yaw);
        cameraLocation = focus.getLocation()
                              .add(direction.clone().multiply(initDistance).multiply(-1.0f))
                              .setDirection(direction);
        return cameraLocation;
    }

    @Override
    public @Nullable Location updateCameraLocation(@NotNull Hotspot focus)
    {
        if (cameraLocation == null)
        {
            newCameraLocation(focus);
        }
        else
        {
            Vector nextDirection = focus.getLocation().toVector().subtract(cameraLocation.toVector()).normalize();
            cameraLocation.setDirection(nextDirection);
        }

        // check if the focus is not too far and visible from the camera
        double distance = MathUtils.Distance(cameraLocation, focus.getLocation());
        if (distance <= maxDistance && (!enableVisibilityCheck || MathUtils.IsVisible(cameraLocation, focus.getLocation())))
        {
            badViewTimer = 0.0f;
        }
        else
        {
            badViewTimer += LazyDirector.GetServerTickDeltaTime();
            if (badViewTimer > maxBadViewTime || distance > criticalDistance)
            {
                boolean success = false;
                int iteration = 0;
                while (iteration < retriesWhenBadView)
                {
                    newCameraLocation(focus);
                    if (MathUtils.IsVisible(cameraLocation, focus.getLocation()))
                    {
                        // success
                        success = true;
                        break;
                    }
                    iteration++;
                }
                // fail
                if (!success)
                {
                    cameraLocation = null;
                }
                badViewTimer = 0.0f;
            }
        }

        return cameraLocation;
    }

    @Override
    public void reset()
    {
        cameraLocation = null;
        badViewTimer = 0.0f;
    }
}
