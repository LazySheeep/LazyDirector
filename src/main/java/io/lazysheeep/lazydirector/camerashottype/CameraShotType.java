package io.lazysheeep.lazydirector.camerashottype;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public abstract class CameraShotType
{
    /**
     * <p>
     *     Updates the location of the camera entity corresponding to the focus location.
     * </p>
     * @param camera An entity that represents the camera
     * @param focusLocation The location that the camera should focus on
     */
    public abstract void updateCameraLocation(@NotNull Entity camera, @NotNull Location focusLocation);
}
