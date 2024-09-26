package io.lazysheeep.lazydirector;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.lazysheeep.lazydirector.actor.Actor;
import io.lazysheeep.lazydirector.actor.ActorManager;
import io.lazysheeep.lazydirector.hotspot.HotspotManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

public class Director implements Listener
{
    private ActorManager actorManager;
    private HotspotManager hotspotManager;
    private Cameraman cameraman;

    public HotspotManager getHotspotManager()
    {
        return hotspotManager;
    }

    public Cameraman getCameraman()
    {
        return cameraman;
    }

    public void LoadConfig(FileConfiguration fileConfig)
    {
        actorManager = new ActorManager(fileConfig);
        hotspotManager = new HotspotManager();
        cameraman = new Cameraman("DefaultCameraman");
    }

    public void Start()
    {
        Bukkit.getPluginManager().registerEvents(this, LazyDirector.getPlugin());
    }

    public void Stop()
    {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerTickStart(ServerTickStartEvent event)
    {
        actorManager.update();
        hotspotManager.update();

        cameraman.setFocus(hotspotManager.getAllHotspotsSorted().getFirst());
        cameraman.update();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Actor actor = actorManager.getActor(event.getPlayer());
        if(actor != null)
        {
            if(event.hasChangedBlock())
            {
                actor.actorHotspot.charge(HeatType.PLAYER_MOVEMENT);
            }
        }
    }
}
