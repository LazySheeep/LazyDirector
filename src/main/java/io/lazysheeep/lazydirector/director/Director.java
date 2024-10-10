package io.lazysheeep.lazydirector.director;

import io.lazysheeep.lazydirector.LazyDirector;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * <p>
 *     The director is responsible for managing all cameramen.
 * </p>
 */
public class Director
{
    private final List<Cameraman> cameramen = new ArrayList<>();

    /**
     * Get a copy of all cameramen
     * @return A copy of all cameramen
     */
    public @NotNull List<Cameraman> getAllCameramen()
    {
        return new ArrayList<>(cameramen);
    }

    public Director() {}

    /**
     * <p>
     *     Load or reload the configuration for the director.
     * </p>
     * <p>
     *     Note that this method will destroy all existing cameramen before loading the new configuration.
     * </p>
     * @param configNode The configuration node to load from
     * @return The director itself
     * @throws ConfigurateException
     */
    public @NotNull Director loadConfig(@NotNull ConfigurationNode configNode) throws ConfigurateException
    {
        destroy();
        for(ConfigurationNode cameramanNode : configNode.node("cameramen").childrenList())
        {
            cameramen.add(new Cameraman(cameramanNode));
        }
        LazyDirector.Log(Level.INFO, "Loaded " + cameramen.size() + " cameramen");
        return this;
    }

    /**
     * <p>
     *     Destroy all cameramen
     * </p>
     */
    public void destroy()
    {
        for(Cameraman cameraman : cameramen)
        {
            cameraman.destroy();
        }
        cameramen.clear();
        LazyDirector.Log(Level.INFO, "Destroyed all cameramen");
    }

    /**
     * <p>
     *     Get a cameraman by name
     * </p>
     * @param name The name of the cameraman
     * @return The cameraman with the given name, or null if not found
     */
    public @Nullable Cameraman getCameraman(@NotNull String name)
    {
        for(Cameraman cameraman : cameramen)
        {
            if(cameraman.getName().equals(name))
            {
                return cameraman;
            }
        }
        LazyDirector.Log(Level.WARNING, "Cameraman \"" + name + "\" not found");
        return null;
    }

    /**
     * <p>
     *     Switch a player's attached cameraman to the next one in the list.
     * </p>
     * @param outputPlayer The player to switch cameraman
     * @param currentCameraman The current cameraman the player is attached to
     */
    void switchCameraman(@NotNull Player outputPlayer, @NotNull Cameraman currentCameraman)
    {
        currentCameraman.detachCamera(outputPlayer);
        int index = cameramen.indexOf(currentCameraman);
        Cameraman nextCameraman = cameramen.get(index == cameramen.size() - 1 ? 0 : index + 1);
        nextCameraman.attachCamera(outputPlayer);
    }

    /**
     * <p>
     *     Detach a player from cameraman (if attached to any)
     * </p>
     * @param outputPlayer The player to detach
     */
    public void detachFromAnyCamera(Player outputPlayer)
    {
        for(Cameraman cameraman : cameramen)
        {
            cameraman.detachCamera(outputPlayer);
        }
    }

    /**
     * <p>
     *     Call the update method of all cameramen.
     * </p>
     * <p>
     *     This method is called once every tick by {@link LazyDirector}
     * </p>
     */
    public void update()
    {
        for(Cameraman cameraman : cameramen)
        {
            cameraman.update();
        }
    }

    public void lateUpdate()
    {
        for (Cameraman cameraman : cameramen)
        {
            cameraman.lateUpdate();
        }
    }
}
