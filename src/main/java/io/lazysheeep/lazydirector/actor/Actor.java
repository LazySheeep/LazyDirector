package io.lazysheeep.lazydirector.actor;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.ActorGroupHotspot;
import io.lazysheeep.lazydirector.hotspot.ActorHotspot;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class Actor
{
    public Player hostPlayer;
    public ActorHotspot actorHotspot;
    public ActorGroupHotspot actorGroupHotspot;

    Actor(@NotNull Player player)
    {
        hostPlayer = player;
        actorHotspot = LazyDirector.GetPlugin().getHotspotManager().createActorHotspot(this);
    }

    void destroy()
    {
        LazyDirector.GetPlugin().getHotspotManager().destroyHotspot(actorHotspot);
        actorHotspot = null;
        if(actorGroupHotspot != null)
        {
            actorGroupHotspot.remove(this);
            actorGroupHotspot = null;
        }
        hostPlayer = null;
    }

    private Location lastLocation;

    public void update()
    {
        // do nothing
    }

    @Override
    public String toString()
    {
        return hostPlayer.getName();
    }
}
