package io.lazysheeep.lazydirector.camerashottype;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public abstract class CameraShotType
{

    /**
     * <p>
     *     Get the next camera location.
     * </p>
     * @param currentCameraLocation The current camera location
     * @param focusLocation The focus location
     * @return The next camera location
     */
    public abstract Location getNextCameraLocation(@NotNull Location currentCameraLocation, @NotNull Location focusLocation);
}
