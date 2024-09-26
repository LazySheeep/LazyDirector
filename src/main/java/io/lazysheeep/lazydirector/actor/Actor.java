package io.lazysheeep.lazydirector.actor;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.ActorGatheringHotspot;
import io.lazysheeep.lazydirector.hotspot.ActorHotspot;
import org.bukkit.entity.Player;


public class Actor
{
    public Player hostPlayer;
    public ActorHotspot actorHotspot;
    public ActorGatheringHotspot actorGatheringHotspot;

    Actor(Player player)
    {
        hostPlayer = player;
        actorHotspot = LazyDirector.getDirector().getHotspotManager().createActorHotspot(this);
    }

    void destroy()
    {
        hostPlayer = null;
        actorHotspot.destroy();
        actorHotspot = null;
        actorGatheringHotspot.remove(this);
        actorGatheringHotspot = null;
    }
}
