package io.lazysheeep.lazydirector.camera;

import io.lazysheeep.lazydirector.LazyDirector;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CameraManager
{
    private final List<Camera> managedCameras = new ArrayList<>();

    /**
     * Get a copy of all cameras
     * @return A copy of all cameras
     */
    public @NotNull List<Camera> getAllCamera()
    {
        return new ArrayList<>(managedCameras);
    }

    public CameraManager() {}

    /**
     * <p>
     *     Load or reload the configuration for the camera manager
     * </p>
     * <p>
     *     Note that this method will destroy all existing cameras before loading the new configuration.
     * </p>
     * @param configNode The configuration node to load from
     * @return The director itself
     * @throws ConfigurateException
     */
    public @NotNull CameraManager loadConfig(@NotNull ConfigurationNode configNode) throws ConfigurateException
    {
        destroy();
        for(ConfigurationNode cameramanNode : configNode.node("cameras").childrenList())
        {
            managedCameras.add(new Camera(cameramanNode));
        }
        LazyDirector.Log(Level.INFO, "Loaded " + managedCameras.size() + " camera");
        return this;
    }

    /**
     * <p>
     *     Destroy all cameras
     * </p>
     */
    public void destroy()
    {
        for(Camera camera : managedCameras)
        {
            camera.destroy();
        }
        managedCameras.clear();
        LazyDirector.Log(Level.INFO, "Destroyed all camera");
    }

    /**
     * <p>
     *     Get a camera by name
     * </p>
     * @param name The name of the camera
     * @return The camera with the given name, or null if not found
     */
    public @Nullable Camera getCamera(@NotNull String name)
    {
        for(Camera camera : managedCameras)
        {
            if(camera.getName().equals(name))
            {
                return camera;
            }
        }
        // LazyDirector.Log(Level.WARNING, "Cameraman \"" + name + "\" not found");
        return null;
    }

    /**
     * <p>
     *     Detach a player from camera (if attached to any)
     * </p>
     * @param outputPlayer The player to detach
     */
    public void detachFromAnyCamera(Player outputPlayer)
    {
        for(Camera camera : managedCameras)
        {
            camera.detachOutput(outputPlayer);
        }
    }

    /**
     * <p>
     *     Call the update method of all cameras.
     * </p>
     * <p>
     *     This method is called once every tick by {@link LazyDirector}
     * </p>
     */
    public void update()
    {
        for(Camera camera : managedCameras)
        {
            camera.update();
        }
    }
}
