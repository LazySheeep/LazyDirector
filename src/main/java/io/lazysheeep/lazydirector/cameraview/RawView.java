package io.lazysheeep.lazydirector.cameraview;

import io.lazysheeep.lazydirector.hotspot.Hotspot;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

public class RawView extends CameraView
{
    public RawView(@Nullable ConfigurationNode configNode) {}

    private Location currentCameraLocation = null;

    @Override
    public @NotNull Location getCurrentCameraLocation()
    {
        if(currentCameraLocation == null)
        {
            throw new IllegalStateException("Camera location is not initialized.");
        }
        return currentCameraLocation;
    }

    @Override
    public void newCameraLocation(@NotNull Hotspot focus)
    {
        currentCameraLocation = focus.getLocation();
    }

    @Override
    public void updateCameraLocation(@NotNull Hotspot focus)
    {
        currentCameraLocation = focus.getLocation();
    }

    @Override
    public boolean isViewGood()
    {
        return true;
    }

    @Override
    public boolean cannotFindGoodView()
    {
        return false;
    }
}
