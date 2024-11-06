package io.lazysheeep.lazydirector.cameraview;

import io.lazysheeep.lazydirector.hotspot.Hotspot;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

public class RawView extends CameraView
{
    public RawView(@Nullable ConfigurationNode configNode) {}

    @Override
    public @Nullable Location updateCameraLocation(@NotNull Hotspot focus)
    {
        return focus.getLocation();
    }

    @Override
    public void reset()
    {

    }
}
