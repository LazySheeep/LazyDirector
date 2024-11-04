package io.lazysheeep.lazydirector.cameraview;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.logging.Level;

public abstract class CameraView
{
    public static @Nullable CameraView CreateCameraView(@NotNull ConfigurationNode configNode)
    {
        String type = configNode.node("type").getString("type_not_found") + "View";
        try
        {
            Class<?> clazz = Class.forName("io.lazysheeep.lazydirector.cameraview." + type);
            return (CameraView) clazz.getConstructor(ConfigurationNode.class).newInstance(configNode);
        }
        catch (Exception e)
        {
            LazyDirector.Log(Level.SEVERE, "Failed to create camera view: " + type + " because: " + e.getMessage());
            return null;
        }
    }

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

    @Override
    public String toString()
    {
        var className = this.getClass().toString().split("\\.");
        return className[className.length - 1];
    }
}
