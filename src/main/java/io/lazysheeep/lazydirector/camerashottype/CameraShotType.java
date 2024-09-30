package io.lazysheeep.lazydirector.camerashottype;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class CameraShotType
{
    private static final Map<String, Class<? extends CameraShotType>> CameraShotTypes = new HashMap<>();

    static
    {
        CameraShotTypes.put("birds_eye_shot", BirdsEyeShot.class);
        CameraShotTypes.put("over_the_shoulder_shot", OverTheShoulderShot.class);
    }

    public static Class<? extends CameraShotType> GetType(String type)
    {
        return CameraShotTypes.get(type);
    }

    public abstract void updateCameraLocation(@NotNull Entity camera, @NotNull Location focusLocation);
}
