package io.lazysheeep.lazydirector.director;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.lazysheeep.lazydirector.camerashottype.BirdsEyeShot;
import io.lazysheeep.lazydirector.camerashottype.OverTheShoulderShot;
import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.actor.Actor;
import io.lazysheeep.lazydirector.actor.ActorManager;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.hotspot.HotspotManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.List;

public class Director implements Listener
{
    private ActorManager actorManager;
    private HotspotManager hotspotManager;
    private final List<Cameraman> cameramen = new ArrayList<>();

    public HotspotManager getHotspotManager()
    {
        return hotspotManager;
    }

    public void LoadConfig(FileConfiguration fileConfig)
    {
        actorManager = new ActorManager(fileConfig);
        hotspotManager = new HotspotManager();
        cameramen.add(new Cameraman("CameramanA", new BirdsEyeShot()));
        cameramen.add(new Cameraman("CameramanB", new OverTheShoulderShot()));
    }

    public void Start()
    {
        Bukkit.getPluginManager().registerEvents(this, LazyDirector.getPlugin());
    }

    public void Stop()
    {
        HandlerList.unregisterAll(this);
    }

    public Cameraman getCameraman(String name)
    {
        for(Cameraman cameraman : cameramen)
        {
            if(cameraman.name.equals(name))
            {
                return cameraman;
            }
        }
        return null;
    }

    private void switchFocus(Cameraman cameraman)
    {
        List<Hotspot> sortedHotspots = hotspotManager.getAllHotspotsSorted();
        if(!sortedHotspots.isEmpty())
        {
            cameraman.setFocus(sortedHotspots.getFirst());
        }
        else
        {
            cameraman.setFocus(null);
        }
    }

    void switchCameraman(Player player, Cameraman currentCameraman)
    {
        currentCameraman.detachCamera(player);
        int index = cameramen.indexOf(currentCameraman);
        Cameraman nextCameraman = cameramen.get(index == cameramen.size() - 1 ? 0 : index + 1);
        nextCameraman.attachCamera(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerTickStart(ServerTickStartEvent event)
    {
        actorManager.update();
        hotspotManager.update();

        for(Cameraman cameraman : cameramen)
        {
            switchFocus(cameraman);
            cameraman.update();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Actor actor = actorManager.getActor(event.getPlayer());
        if(actor != null)
        {
            if(event.hasChangedBlock())
            {
                actor.actorHotspot.increase("player_movement");
            }
        }
    }
}
