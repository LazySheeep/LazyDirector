package io.lazysheeep.lazydirector.camerashottype;

import io.lazysheeep.lazydirector.hotspot.Hotspot;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CameraView
{
    /**
     * <p>
     * Update camera location.
     * </p>
     *
     * @param focus The focus location
     * @return The updated camera location
     */
    public abstract @Nullable Location updateCameraLocation(@NotNull Hotspot focus);

    public abstract void reset();
}
