package io.lazysheeep.lazydirector.cameraview;

import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import org.bukkit.Location;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

public class OverTheShoulderView extends CameraView
{
    private final Vector position;
    private final float minDistance;

    public OverTheShoulderView(@NotNull ConfigurationNode configNode)
    {
        ConfigurationNode positionNode = configNode.node("position");
        this.position = new Vector(positionNode.node("x").getDouble(0.0),
                                   positionNode.node("y").getDouble(0.0),
                                   positionNode.node("z").getDouble(0.0));
        this.minDistance = configNode.node("minDistance").getFloat(0.0f);
    }

    public OverTheShoulderView(@NotNull Vector position, float minDistance)
    {
        this.position = position;
        this.minDistance = minDistance;
    }

    private Location currentCameraLocation = null;

    private @NotNull Location calculateCameraLocation(@NotNull Hotspot focus)
    {
        Vector focusForwardDirection = focus.getLocation().getDirection();
        Vector focusRightDirection = focusForwardDirection.getCrossProduct(new Vector(0.0f, 1.0f, 0.0f)).normalize();
        Vector focusUpDirection = focusRightDirection.getCrossProduct(focusForwardDirection).normalize();
        Vector cameraOffset = focusForwardDirection.multiply(position.getZ())
                                                   .add(focusRightDirection.multiply(position.getX()))
                                                   .add(focusUpDirection.multiply(position.getY()));
        Location cameraLocation = focus.getLocation().add(cameraOffset);

        RayTraceResult rayTraceResult = MathUtils.RayTrace(focus.getLocation(), cameraLocation);
        if(rayTraceResult != null)
        {
            Vector hitPosition = rayTraceResult.getHitPosition();
            if(hitPosition.distance(focus.getLocation().toVector()) > minDistance)
            {
                cameraLocation.set(hitPosition.getX(), hitPosition.getY(), hitPosition.getZ());
                cameraLocation.add(cameraLocation.getDirection().multiply(0.2f));
            }
            else
            {
                cameraLocation = focus.getLocation().add(cameraOffset.clone().normalize().multiply(minDistance));
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
        currentCameraLocation = calculateCameraLocation(focus);
    }

    @Override
    public void updateCameraLocation(@NotNull Hotspot focus)
    {
        currentCameraLocation = calculateCameraLocation(focus);
    }

    @Override
    public boolean isViewGood()
    {
        return true;
    }

    @Override
    public boolean cannotFindGoodView()
    {
        return false;
    }
}
