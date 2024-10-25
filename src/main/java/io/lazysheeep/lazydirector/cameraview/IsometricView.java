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

// isometric view
public class IsometricView extends CameraView
{
    private final float minDistance;
    private final float maxDistance;
    private final boolean enableVisibilityCheck;
    private final float maxBadViewTime;
    private final int retriesWhenBadView;

    private double pitch;
    private double yaw;
    private float distance;
    private float badViewTimer;

    public IsometricView(@NotNull ConfigurationNode configNode)
    {
        minDistance = configNode.node("minDistance").getFloat(0.0f);
        maxDistance = configNode.node("maxDistance").getFloat(0.0f);
        enableVisibilityCheck = configNode.node("enableVisibilityCheck").getBoolean(false);
        retriesWhenBadView = configNode.node("retriesWhenBadView").getInt(1);
        maxBadViewTime = configNode.node("maxBadViewTime").getFloat(Float.MAX_VALUE);

        reset();
    }

    public IsometricView(float minDistance, float maxDistance, boolean enableVisibilityCheck, float maxBadViewTime, int retriesWhenBadView)
    {
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.enableVisibilityCheck = enableVisibilityCheck;
        this.retriesWhenBadView = retriesWhenBadView;
        this.maxBadViewTime = maxBadViewTime;

        reset();
    }

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
        if(!enableVisibilityCheck || MathUtils.IsVisible(nextCameraLocation, focus.getLocation()))
        {
            badViewTimer = 0.0f;
            distance = maxDistance;
        }
        else
        {
            badViewTimer += LazyDirector.GetServerTickDeltaTime();
            if(badViewTimer > maxBadViewTime)
            {
                boolean success = false;
                int iteration = 0;
                distance = maxDistance;
                while (iteration < retriesWhenBadView)
                {
                    pitch = RandomUtils.NextDouble(15.0d, 60.0d);
                    yaw = RandomUtils.NextDouble(-180.0d, 180.0d);
                    distance -= (maxDistance - minDistance) / retriesWhenBadView;
                    nextCameraLocation = nextCameraLocation(focus);
                    if (MathUtils.IsVisible(nextCameraLocation, focus.getLocation()))
                    {
                        // success
                        success = true;
                        break;
                    }
                    iteration++;
                }
                // fail
                if(!success)
                {
                    nextCameraLocation = null;
                }
                badViewTimer = 0.0f;
            }
        }
        return nextCameraLocation;
    }

    @Override
    public void reset()
    {
        pitch = 30.0d;
        yaw = 45.0d;
        distance = maxDistance;
        badViewTimer = 0.0f;
    }
}
