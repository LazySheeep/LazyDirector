package io.lazysheeep.lazydirector.camerashottype;

import io.lazysheeep.lazydirector.hotspot.Hotspot;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MonitorShot extends CameraShotType
{
    protected @NotNull Location nextCameraLocation(@NotNull Hotspot focus)
    {
        return null;
    }

    protected @NotNull Location newCameraLocation(@NotNull Hotspot focus)
    {
        return null;
    }

    @Override
    public @Nullable Location updateCameraLocation(@NotNull Hotspot focus)
    {
        return null;
    }
}
