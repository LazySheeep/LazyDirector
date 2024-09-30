package io.lazysheeep.lazydirector.director;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Director
{
    private final List<Cameraman> cameramen = new ArrayList<>();

    public Director(@NotNull ConfigurationSection configSection)
    {
        for(Map<?, ?> config : configSection.getMapList("cameramen"))
        {
            cameramen.add(new Cameraman(config));
        }
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

    public void update()
    {
        for(Cameraman cameraman : cameramen)
        {
            cameraman.update();
        }
    }
}
