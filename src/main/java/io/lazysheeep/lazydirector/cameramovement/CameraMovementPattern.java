package io.lazysheeep.lazydirector.cameramovement;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public abstract class CameraMovementPattern
{
    public abstract void updateCameraLocation(@NotNull Entity camera, @NotNull Location focusLocation);
}
