package io.lazysheeep.lazydirector.hotspot;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.actor.Actor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;
import java.util.logging.Level;

public class HotspotManager implements Listener
{
    private final List<Hotspot> hotspots = new LinkedList<>();

    public HotspotManager(ConfigurationSection configSection)
    {
        for(Map<?, ?> config : configSection.getMapList("staticHotspots"))
        {
            String worldName = (String)config.get("world");
            float x = ((Number)config.get("x")).floatValue();
            float y = ((Number)config.get("y")).floatValue();
            float z = ((Number)config.get("z")).floatValue();
            Location location = new Location(LazyDirector.GetPlugin().getServer().getWorld(worldName), x, y, z);
            int heat = ((Number)config.get("heat")).intValue();
            createStaticHotspot(location, heat);
        }
    }

    public StaticHotspot createStaticHotspot(Location location, int heat)
    {
        StaticHotspot staticHotspot = new StaticHotspot(location, heat);
        hotspots.add(staticHotspot);
        LazyDirector.GetPlugin().getLogger().log(Level.INFO, "Created hotspot: " + staticHotspot);
        return staticHotspot;
    }

    public ActorHotspot createActorHotspot(Actor actor)
    {
        ActorHotspot actorHotspot = new ActorHotspot(actor);
        hotspots.add(actorHotspot);
        LazyDirector.GetPlugin().getLogger().log(Level.INFO, "Created hotspot: " + actorHotspot);
        return actorHotspot;
    }

    public ActorGatheringHotspot createActorGatheringHotspot(Actor... initActors)
    {
        ActorGatheringHotspot actorGatheringHotspot = new ActorGatheringHotspot(initActors);
        hotspots.add(actorGatheringHotspot);
        LazyDirector.GetPlugin().getLogger().log(Level.INFO, "Created hotspot: " + actorGatheringHotspot);
        return actorGatheringHotspot;
    }

    public void destroyHotspot(Hotspot hotspot)
    {
        LazyDirector.GetPlugin().getLogger().log(Level.INFO, "Destroying hotspot: " + hotspot);
        hotspot.destroy();
        hotspots.remove(hotspot);
    }

    public List<Hotspot> getAllHotspotsSorted()
    {
        List<Hotspot> sortedHotspots = new ArrayList<>(hotspots);
        Collections.sort(sortedHotspots);
        return sortedHotspots.reversed();
    }

    public void update()
    {
        for(Hotspot hotspot : hotspots)
        {
            hotspot.update();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Actor actor = LazyDirector.GetPlugin().getActorManager().getActor(event.getPlayer());
        if(actor != null)
        {
            if(event.hasChangedBlock())
            {
                actor.actorHotspot.increase("player_movement");
            }
        }
    }
}