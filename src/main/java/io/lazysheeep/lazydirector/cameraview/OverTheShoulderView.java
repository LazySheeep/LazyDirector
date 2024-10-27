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

    public OverTheShoulderView(@NotNull ConfigurationNode configNode)
    {
        ConfigurationNode positionNode = configNode.node("position");
        this.position = new Vector(positionNode.node("x").getDouble(0.0),
                                   positionNode.node("y").getDouble(0.0),
                                   positionNode.node("z").getDouble(0.0));

        reset();
    }

    public OverTheShoulderView(@NotNull Vector position)
    {
        this.position = position;
    }

    @Override
    public @Nullable Location updateCameraLocation(@NotNull Hotspot focus)
    {
        Vector focusForwardDirection = focus.getLocation().getDirection();
        Vector focusRightDirection = focusForwardDirection.getCrossProduct(new Vector(0.0f, 1.0f, 0.0f)).normalize();
        Vector focusUpDirection = focusRightDirection.getCrossProduct(focusForwardDirection).normalize();
        Location nextCameraLocation = focus.getLocation()
                                           .add(focusForwardDirection.multiply(position.getZ()))
                                           .add(focusRightDirection.multiply(position.getX()))
                                           .add(focusUpDirection.multiply(position.getY()));

        RayTraceResult rayTraceResult = MathUtils.RayTrace(focus.getLocation(), nextCameraLocation);
        if(rayTraceResult != null)
        {
            Vector hitPosition = rayTraceResult.getHitPosition();
            nextCameraLocation.set(hitPosition.getX(), hitPosition.getY(), hitPosition.getZ());
            nextCameraLocation.add(nextCameraLocation.getDirection().multiply(0.1f));
        }

        return nextCameraLocation;
    }

    @Override
    public void reset()
    {
        // Do nothing
    }
}
