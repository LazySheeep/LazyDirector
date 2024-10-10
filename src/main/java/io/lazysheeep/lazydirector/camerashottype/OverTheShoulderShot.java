package io.lazysheeep.lazydirector.camerashottype;

import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import org.bukkit.Location;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OverTheShoulderShot extends CameraShotType
{
    private static final Vector direction = new Vector(1.0f, 0.2f, -3.0f).normalize();
    private static final float distance = 4.0f;

    public OverTheShoulderShot()
    {
    }

    @Override
    public @Nullable Location updateCameraLocation(@NotNull Hotspot focus)
    {
        Vector focusForwardDirection = focus.getLocation().getDirection();
        Vector focusRightDirection = focusForwardDirection.getCrossProduct(new Vector(0.0f, 1.0f, 0.0f)).normalize();
        Vector focusUpDirection = focusRightDirection.getCrossProduct(focusForwardDirection).normalize();
        Location nextCameraLocation = focus.getLocation()
                                           .add(focusForwardDirection.multiply(direction.getZ() * distance))
                                           .add(focusRightDirection.multiply(direction.getX() * distance))
                                           .add(focusUpDirection.multiply(direction.getY() * distance));

        RayTraceResult rayTraceResult = MathUtils.RayTrace(focus.getLocation(), nextCameraLocation);
        if(rayTraceResult != null)
        {
            Vector hitPosition = rayTraceResult.getHitPosition();
            nextCameraLocation.set(hitPosition.getX(), hitPosition.getY(), hitPosition.getZ());
            nextCameraLocation.add(nextCameraLocation.getDirection().multiply(0.1f));
        }

        return nextCameraLocation;
    }
}
