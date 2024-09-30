package io.lazysheeep.lazydirector.actor;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.ActorGatheringHotspot;
import io.lazysheeep.lazydirector.hotspot.ActorHotspot;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class Actor
{
    public Player hostPlayer;
    public ActorHotspot actorHotspot;
    public ActorGatheringHotspot actorGatheringHotspot;

    Actor(@NotNull Player player)
    {
        hostPlayer = player;
        actorHotspot = LazyDirector.GetPlugin().getHotspotManager().createActorHotspot(this);
    }

    void destroy()
    {
        LazyDirector.GetPlugin().getHotspotManager().destroyHotspot(actorHotspot);
        actorHotspot = null;
        if(actorGatheringHotspot != null)
        {
            actorGatheringHotspot.remove(this);
            actorGatheringHotspot = null;
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
