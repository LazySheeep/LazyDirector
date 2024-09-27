package io.lazysheeep.lazydirector.cameramovement;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class CMPOverlook extends CameraMovementPattern
{
    @Override
    public void updateCameraLocation(@NotNull Entity camera, @NotNull Location focusLocation)
    {
        Location targetLocation = focusLocation.add(5.0f, 5.0f, 5.0f).setDirection(new Vector(-1.0f, -1.0f, -1.0f));

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
            Vector velocity = new Vector((targetLocation.getX() - currentLocation.getX()) / 4, (targetLocation.getY() - currentLocation.getY()) / 4, (targetLocation.getZ() - currentLocation.getZ()) / 4);
            camera.teleport(camera.getLocation().add(velocity));
        }
    }
}
