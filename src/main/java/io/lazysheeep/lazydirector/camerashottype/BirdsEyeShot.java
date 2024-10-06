package io.lazysheeep.lazydirector.camerashottype;

import io.lazysheeep.lazydirector.util.MathUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class BirdsEyeShot extends CameraShotType
{
    public BirdsEyeShot() {}

    @Override
    public Location getNextCameraLocation(@NotNull Location currentCameraLocation, @NotNull Location focusLocation)
    {
        Location targetLocation = focusLocation.clone().add(5.0f, 5.0f, 5.0f).setDirection(new Vector(-1.0f, -1.0f, -1.0f));
        return MathUtils.Lerp(currentCameraLocation, targetLocation, 0.2f);
    }
}
