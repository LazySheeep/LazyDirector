package io.lazysheeep.lazydirector.cameraview;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.camera.Camera;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import org.bukkit.Location;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
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

    public IsometricView(@NotNull ConfigurationNode configNode)
    {
        minDistance = configNode.node("minDistance").getFloat(0.0f);
        maxDistance = configNode.node("maxDistance").getFloat(0.0f);
        minPitch = configNode.node("minPitch").getDouble(30.0);
        maxPitch = configNode.node("maxPitch").getDouble(30.0);
        enableVisibilityCheck = configNode.node("enableVisibilityCheck").getBoolean(false);
        maxBadViewTime = configNode.node("maxBadViewTime").getFloat(Float.MAX_VALUE);
    }

    public IsometricView(float minDistance, float maxDistance, double minPitch, double maxPitch, boolean enableVisibilityCheck, float maxBadViewTime)
    {
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.minPitch = minPitch;
        this.maxPitch = maxPitch;
        this.enableVisibilityCheck = enableVisibilityCheck;
        this.maxBadViewTime = maxBadViewTime;
    }

    private double pitch = 30.0f;
    private double yaw = -135.0f;
    private float badViewTimer = 0.0f;
    private Location currentCameraLocation = null;
    private boolean cannotFindGoodView = false;

    private @NotNull Location calculateCameraLocation(@NotNull Hotspot focus)
    {
        Vector cameraDirection = MathUtils.GetDirectionFromPitchAndYaw(pitch, yaw);
        Location focusLocation = focus.getLocation();
        Location cameraLocation = focusLocation.clone().add(cameraDirection.clone().multiply(-maxDistance)).setDirection(cameraDirection);
        if(enableVisibilityCheck)
        {
            RayTraceResult rayTraceResult = MathUtils.RayTrace(focusLocation, cameraLocation);
            if(rayTraceResult != null)
            {
                Vector hitPosition = rayTraceResult.getHitPosition();
                if(hitPosition.distance(focusLocation.toVector()) > minDistance)
                {
                    cameraLocation.set(hitPosition.getX(), hitPosition.getY(), hitPosition.getZ());
                    cameraLocation.add(cameraLocation.getDirection().multiply(0.1f));
                }
                else
                {
                    cameraLocation = focusLocation.clone().add(cameraDirection.clone().multiply(-minDistance)).setDirection(cameraDirection);
                }
            }
        }
        return cameraLocation;
    }

    @Override
    public @NotNull Location getCurrentCameraLocation()
    {
        if(currentCameraLocation == null)
        {
            throw new IllegalStateException("Camera location is not initialized.");
        }
        return currentCameraLocation;
    }

    @Override
    public void newCameraLocation(@NotNull Hotspot focus)
    {
        Location newCameraLocation = null;
        cannotFindGoodView = true;
        for(pitch = minPitch; pitch < maxPitch; pitch += 5.0)
        {
            for(yaw = -135.0; yaw < 135.0; yaw += 90.0)
            {
                newCameraLocation = calculateCameraLocation(focus);
                if (!enableVisibilityCheck || MathUtils.IsVisible(newCameraLocation, focus.getLocation()))
                {
                    currentCameraLocation = newCameraLocation;
                    badViewTimer = 0.0f;
                    cannotFindGoodView = false;
                    return;
                }
            }
        }
        if(currentCameraLocation == null)
        {
            currentCameraLocation = newCameraLocation;
        }
    }

    @Override
    public void updateCameraLocation(@NotNull Hotspot focus, @NotNull Camera camera)
    {
        // update camera location
        if (currentCameraLocation == null || badViewTimer >= maxBadViewTime)
        {
            newCameraLocation(focus);
        }
        else
        {
            currentCameraLocation = calculateCameraLocation(focus);
        }

        // update bad view timer
        if(!enableVisibilityCheck || MathUtils.IsVisible(currentCameraLocation, focus.getLocation()))
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
