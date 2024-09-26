package io.lazysheeep.lazydirector.hotspot;

import io.lazysheeep.lazydirector.actor.Actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class HotspotManager
{
    private final List<Hotspot> hotspots = new LinkedList<>();

    public ActorHotspot createActorHotspot(Actor actor)
    {
        ActorHotspot actorHotspot = new ActorHotspot(actor);
        hotspots.add(actorHotspot);
        return actorHotspot;
    }

    public ActorGatheringHotspot createActorGatheringHotspot(Actor... initActors)
    {
        ActorGatheringHotspot actorGatheringHotspot = new ActorGatheringHotspot(initActors);
        hotspots.add(actorGatheringHotspot);
        return actorGatheringHotspot;
    }

    public void destroyHotspot(Hotspot hotspot)
    {
        hotspot.destroy();
        hotspots.remove(hotspot);
    }

    public List<Hotspot> getAllHotspotsSorted()
    {
        List<Hotspot> sortedHotspots = new ArrayList<>(hotspots);
        Collections.sort(sortedHotspots);
        return sortedHotspots;
    }

    public void update()
    {
        for(Hotspot hotspot : hotspots)
        {
            hotspot.update();
        }
    }
}