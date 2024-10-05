package io.lazysheeep.lazydirector.hotspot;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.actor.Actor;
import org.bukkit.Location;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;
import java.util.logging.Level;

/**
 * <p>
 *     HotspotManager manages all the hotspots in the world.
 * </p>
 * <p>
 *     It is responsible for creating, destroying, and updating hotspots.
 * </p>
 */
public class HotspotManager
{
    public HotspotManager() {}

    /**
     * <p>
     *     Load configuration for the hotspot manager and create the static hotspots.
     * </p>
     * <p>
     *     Note that all the existing hotspots will be destroyed before loading the configuration.
     * </p>
     * @param configNode The configuration node to load the hotspots from
     * @return The HotspotManager itself
     */
    public HotspotManager loadConfig(ConfigurationNode configNode)
    {
        destroy();
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

    /**
     * <p>
     *     Destroy all hotspots.
     * </p>
     */
    public void destroy()
    {
        for(Hotspot hotspot : hotspots)
        {
            destroyHotspot(hotspot);
        }
        hotspots.clear();
    }

    private final List<Hotspot> hotspots = new LinkedList<>();

    /**
     * <p>
     *     Get all hotspots sorted by heat.
     * </p>
     * @return The sorted hotspots
     */
    public List<Hotspot> getAllHotspotsSorted()
    {
        List<Hotspot> sortedHotspots = new ArrayList<>(hotspots);
        Collections.sort(sortedHotspots);
        return sortedHotspots.reversed();
    }

    /**
     * <p>
     *     Create a static hotspot.
     * </p>
     * @param location The location of the hotspot
     * @param heat The initial heat of the hotspot
     * @return The created hotspot
     */
    public StaticHotspot createStaticHotspot(Location location, float heat)
    {
        StaticHotspot staticHotspot = new StaticHotspot(location, heat);
        hotspots.add(staticHotspot);
        LazyDirector.Log(Level.INFO, "Created hotspot: " + staticHotspot);
        return staticHotspot;
    }

    /**
     * <p>
     *     Create an actor hotspot.
     * </p>
     * @param actor The actor to create the hotspot for
     * @return The created hotspot
     */
    public ActorHotspot createActorHotspot(Actor actor)
    {
        ActorHotspot actorHotspot = new ActorHotspot(actor);
        hotspots.add(actorHotspot);
        LazyDirector.Log(Level.INFO, "Created hotspot: " + actorHotspot);
        return actorHotspot;
    }

    /**
     * <p>
     *     Create an actor group hotspot.
     * </p>
     * @param initActors The initial actors in the group
     * @return The created hotspot
     */
    public ActorGroupHotspot createActorGroupHotspot(Actor... initActors)
    {
        ActorGroupHotspot actorGroupHotspot = new ActorGroupHotspot(initActors);
        hotspots.add(actorGroupHotspot);
        LazyDirector.Log(Level.INFO, "Created hotspot: " + actorGroupHotspot);
        return actorGroupHotspot;
    }

    /**
     * <p>
     *     Destroy a hotspot.
     * </p>
     * @param hotspot The hotspot to destroy
     */
    public void destroyHotspot(Hotspot hotspot)
    {
        LazyDirector.Log(Level.INFO, "Destroying hotspot: " + hotspot);
        hotspot.destroy();
        hotspots.remove(hotspot);
    }

    /**
     * <p>
     *     Call the update method of all hotspots.
     * </p>
     * <p>
     *     This method is called every tick by {@link LazyDirector}.
     * </p>
     */
    public void update()
    {
        for(Hotspot hotspot : hotspots)
        {
            hotspot.update();
        }
    }
}