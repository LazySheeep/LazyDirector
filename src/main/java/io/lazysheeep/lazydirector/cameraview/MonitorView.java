package io.lazysheeep.lazydirector.cameraview;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import io.lazysheeep.lazydirector.util.RandomUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

public class MonitorView extends CameraView
{
    private final float initDistance;
    private final float maxDistance;
    private final float criticalDistance;
    private final boolean enableVisibilityCheck;
    private final float maxBadViewTime;
    private final int retriesWhenBadView;

    public MonitorView(@NotNull ConfigurationNode configNode)
    {
        initDistance = configNode.node("initDistance").getFloat(0.0f);
        maxDistance = configNode.node("maxDistance").getFloat(Float.MAX_VALUE);
        criticalDistance = configNode.node("criticalDistance").getFloat(Float.MAX_VALUE);
        enableVisibilityCheck = configNode.node("enableVisibilityCheck").getBoolean(false);
        maxBadViewTime = configNode.node("maxBadViewTime").getFloat(Float.MAX_VALUE);
        retriesWhenBadView = configNode.node("retriesWhenBadView").getInt(1);
    }

    private float badViewTimer = 0.0f;
    private Location currentCameraLocation = null;
    private boolean cannotFindGoodView = false;

    @Override
    public @NotNull Location getCurrentCameraLocation()
    {
        if(currentCameraLocation == null)
        {
            throw new IllegalStateException("Camera location is not initialized.");
        }
        return currentCameraLocation;
    }

    public void newCameraLocation(@NotNull Hotspot focus)
    {
        Location newCameraLocation = null;
        int retries = 0;
        cannotFindGoodView = true;
        while (retries < retriesWhenBadView)
        {
            double pitch = RandomUtils.NextDouble(-90.0d, 90.0d);
            double yaw = RandomUtils.NextDouble(-180.0d, 180.0d);
            Vector direction = MathUtils.GetDirectionFromPitchAndYaw(pitch, yaw);
            newCameraLocation = focus.getLocation()
                                         .add(direction.clone().multiply(initDistance).multiply(-1.0f))
                                         .setDirection(direction);
            if (!enableVisibilityCheck || MathUtils.IsVisible(newCameraLocation, focus.getLocation()))
            {
                currentCameraLocation = newCameraLocation;
                badViewTimer = 0.0f;
                cannotFindGoodView = false;
                break;
            }
            retries++;
        }
        if(currentCameraLocation == null)
        {
            currentCameraLocation = newCameraLocation;
        }
    }

    @Override
    public void updateCameraLocation(@NotNull Hotspot focus)
    {
        if (currentCameraLocation == null || badViewTimer >= maxBadViewTime || MathUtils.Distance(currentCameraLocation, focus.getLocation()) > criticalDistance)
        {
            newCameraLocation(focus);
        }
        else
        {
            Vector nextDirection = focus.getLocation().toVector().subtract(currentCameraLocation.toVector()).normalize();
            currentCameraLocation.setDirection(nextDirection);
        }

        // check if the focus is not too far and visible from the camera
        double distance = MathUtils.Distance(currentCameraLocation, focus.getLocation());
        if (distance <= maxDistance && (!enableVisibilityCheck || MathUtils.IsVisible(currentCameraLocation, focus.getLocation())))
        {
            badViewTimer = 0.0f;
        }
        else
        {
            badViewTimer += LazyDirector.GetServerTickDeltaTime();
        }
    }

    @Override
    public boolean isViewGood()
    {
        return badViewTimer <= 0.0f;
    }

    @Override
    public boolean cannotFindGoodView()
    {
        return cannotFindGoodView;
    }
}
