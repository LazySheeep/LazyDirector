package io.lazysheeep.lazydirector.hotspot;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.actor.Actor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;
import java.util.logging.Level;

public class HotspotManager implements Listener
{
    private final List<Hotspot> hotspots = new LinkedList<>();

    public HotspotManager() {}

    public HotspotManager loadConfig(ConfigurationNode configNode)
    {
        for(ConfigurationNode staticHotspotNode : configNode.node("staticHotspots").childrenList())
        {
            ConfigurationNode locationNode = staticHotspotNode.node("location");
            String worldName = locationNode.node("world").getString();
            float x = locationNode.node("x").getFloat();
            float y = locationNode.node("y").getFloat();
            float z = locationNode.node("z").getFloat();
            Location location = new Location(LazyDirector.GetPlugin().getServer().getWorld(worldName), x, y, z);
            float heat = staticHotspotNode.node("heat").getFloat();
            createStaticHotspot(location, heat);
        }
        return this;
    }

    public StaticHotspot createStaticHotspot(Location location, float heat)
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

    public ActorGroupHotspot createActorGatheringHotspot(Actor... initActors)
    {
        ActorGroupHotspot actorGroupHotspot = new ActorGroupHotspot(initActors);
        hotspots.add(actorGroupHotspot);
        LazyDirector.GetPlugin().getLogger().log(Level.INFO, "Created hotspot: " + actorGroupHotspot);
        return actorGroupHotspot;
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