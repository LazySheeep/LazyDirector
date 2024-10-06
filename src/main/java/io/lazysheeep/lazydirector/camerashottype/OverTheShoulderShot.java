package io.lazysheeep.lazydirector.camerashottype;

import io.lazysheeep.lazydirector.util.MathUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class OverTheShoulderShot extends CameraShotType
{
    public OverTheShoulderShot() {}

    @Override
    public Location getNextCameraLocation(@NotNull Location currentCameraLocation, @NotNull Location focusLocation)
    {
        Vector focusForwardDirection = focusLocation.getDirection();
        Vector focusRightDirection = focusForwardDirection.getCrossProduct(new Vector(0.0f, 1.0f, 0.0f)).normalize();
        Vector focusUpDirection = focusRightDirection.getCrossProduct(focusForwardDirection).normalize();
        Location targetLocation = focusLocation.clone().add(focusForwardDirection.multiply(-3.0f)).add(focusRightDirection.multiply(1.0f)).add(focusUpDirection.multiply(0.2f));
        return MathUtils.Lerp(currentCameraLocation, targetLocation, 0.2f);
    }
}
