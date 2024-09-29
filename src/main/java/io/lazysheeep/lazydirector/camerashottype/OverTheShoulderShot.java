package io.lazysheeep.lazydirector.camerashottype;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class OverTheShoulderShot extends CameraShotType
{
    @Override
    public void updateCameraLocation(@NotNull Entity camera, @NotNull Location focusLocation)
    {
        Vector focusForwardDirection = focusLocation.getDirection();
        Vector focusRightDirection = focusForwardDirection.getCrossProduct(new Vector(0.0f, 1.0f, 0.0f)).normalize();
        Vector focusUpDirection = focusRightDirection.getCrossProduct(focusForwardDirection).normalize();
        Location targetLocation = focusLocation.add(focusForwardDirection.multiply(-3.0f)).add(focusRightDirection.multiply(1.0f)).add(focusUpDirection.multiply(0.2f));

        Location currentLocation = camera.getLocation();
        if(currentLocation.getWorld() != targetLocation.getWorld() || currentLocation.distance(targetLocation) > 128.0f)
        {
            camera.teleport(targetLocation);
        }
        else
        {
            // set rotation
            camera.setRotation(targetLocation.getYaw(), targetLocation.getPitch());
            // set position
            Vector velocity = (targetLocation.toVector().subtract(currentLocation.toVector()).multiply(0.25f));
            camera.teleport(camera.getLocation().add(velocity));
        }
    }
}
