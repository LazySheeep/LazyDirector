package io.lazysheeep.lazydirector.cameraview;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import io.lazysheeep.lazydirector.util.RandomUtils;
import org.bukkit.Location;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

// isometric view
public class IsometricView extends CameraView
{
    private final float minDistance;
    private final float maxDistance;
    private final double minPitch;
    private final double maxPitch;
    private final boolean enableVisibilityCheck;
    private final float maxBadViewTime;

    private double pitch;
    private double yaw;
    private float badViewTimer;

    public IsometricView(@NotNull ConfigurationNode configNode)
    {
        minDistance = configNode.node("minDistance").getFloat(0.0f);
        maxDistance = configNode.node("maxDistance").getFloat(0.0f);
        minPitch = configNode.node("minPitch").getDouble(30.0);
        maxPitch = configNode.node("maxPitch").getDouble(30.0);
        enableVisibilityCheck = configNode.node("enableVisibilityCheck").getBoolean(false);
        maxBadViewTime = configNode.node("maxBadViewTime").getFloat(Float.MAX_VALUE);
        reset();
    }

    public IsometricView(float minDistance, float maxDistance, double minPitch, double maxPitch, boolean enableVisibilityCheck, float maxBadViewTime)
    {
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.minPitch = minPitch;
        this.maxPitch = maxPitch;
        this.enableVisibilityCheck = enableVisibilityCheck;
        this.maxBadViewTime = maxBadViewTime;

        reset();
    }

    private @NotNull Location nextCameraLocation(@NotNull Hotspot focus)
    {
        Vector cameraDirection = MathUtils.GetDirectionFromPitchAndYaw(pitch, yaw);
        Location focusLocation = focus.getLocation();
        Location nextCameraLocation = focusLocation.clone().add(cameraDirection.clone().multiply(-maxDistance)).setDirection(cameraDirection);
        if(enableVisibilityCheck)
        {
            RayTraceResult rayTraceResult = MathUtils.RayTrace(focusLocation, nextCameraLocation);
            if(rayTraceResult != null)
            {
                Vector hitPosition = rayTraceResult.getHitPosition();
                if(hitPosition.distance(focusLocation.toVector()) > minDistance)
                {
                    nextCameraLocation.set(hitPosition.getX(), hitPosition.getY(), hitPosition.getZ());
                    nextCameraLocation.add(nextCameraLocation.getDirection().multiply(0.1f));
                }
                else
                {
                    nextCameraLocation = focusLocation.clone().add(cameraDirection.clone().multiply(-minDistance)).setDirection(cameraDirection);
                }
            }
        }
        return nextCameraLocation;
    }

    @Override
    public @Nullable Location updateCameraLocation(@NotNull Hotspot focus)
    {
        Location nextCameraLocation = nextCameraLocation(focus);
        // check if the focus is visible from the camera
        if(!enableVisibilityCheck || MathUtils.IsVisible(nextCameraLocation, focus.getLocation()))
        {
            badViewTimer = 0.0f;
        }
        else
        {
            badViewTimer += LazyDirector.GetServerTickDeltaTime();
            if(badViewTimer > maxBadViewTime)
            {
                boolean success = false;
                for(pitch = minPitch; pitch < maxPitch; pitch += 5.0)
                {
                    for(yaw = -135.0; yaw < 135.0; yaw += 90.0)
                    {
                        nextCameraLocation = nextCameraLocation(focus);
                        if (MathUtils.IsVisible(nextCameraLocation, focus.getLocation()))
                        {
                            // success
                            success = true;
                            break;
                        }
                    }
                    if(success)
                    {
                        break;
                    }
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
        pitch = minPitch;
        yaw = -135.0;
        badViewTimer = maxBadViewTime;
    }
}
