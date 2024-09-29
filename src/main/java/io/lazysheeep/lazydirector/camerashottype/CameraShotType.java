package io.lazysheeep.lazydirector.camerashottype;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public abstract class CameraShotType
{
    public abstract void updateCameraLocation(@NotNull Entity camera, @NotNull Location focusLocation);
}
