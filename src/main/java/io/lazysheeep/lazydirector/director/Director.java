package io.lazysheeep.lazydirector.director;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;

public class Director
{
    private final List<Cameraman> cameramen = new ArrayList<>();

    /**
     * Get a copy of cameramen
     * @return A copy of cameramen
     */
    public List<Cameraman> getCameramen()
    {
        return new ArrayList<>(cameramen);
    }

    public Director() {}

    public Director loadConfig(@NotNull ConfigurationNode configNode) throws ConfigurateException
    {
        for(ConfigurationNode cameramanNode : configNode.node("cameramen").childrenList())
        {
            cameramen.add(new Cameraman().loadConfig(cameramanNode));
        }
        return this;
    }

    public void destroy()
    {
        for(Cameraman cameraman : cameramen)
        {
            cameraman.destroy();
        }
    }

    public Cameraman getCameraman(String name)
    {
        for(Cameraman cameraman : cameramen)
        {
            if(cameraman.getName().equals(name))
            {
                return cameraman;
            }
        }
        return null;
    }

    void switchCameraman(Player player, Cameraman currentCameraman)
    {
        currentCameraman.detachCamera(player);
        int index = cameramen.indexOf(currentCameraman);
        Cameraman nextCameraman = cameramen.get(index == cameramen.size() - 1 ? 0 : index + 1);
        nextCameraman.attachCamera(player);
    }

    public void detachFromAnyCamera(Player player)
    {
        for(Cameraman cameraman : cameramen)
        {
            cameraman.detachCamera(player);
        }
    }

    public void update()
    {
        for(Cameraman cameraman : cameramen)
        {
            cameraman.update();
        }
    }
}
