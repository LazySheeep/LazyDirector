package io.lazysheeep.lazydirector.hotspot;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.actor.Actor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    public @NotNull HotspotManager loadConfig(@NotNull ConfigurationNode configNode)
    {
        destroy();
        // load default hotspot
        defaultHotspot = new StaticHotspot(configNode.node("defaultHotspot"));
        LazyDirector.Log(Level.INFO, "Loaded default hotspot: " + defaultHotspot);
        // load static hotspots
        for(ConfigurationNode staticHotspotNode : configNode.node("staticHotspots").childrenList())
        {
            createStaticHotspot(staticHotspotNode);
        }
        LazyDirector.Log(Level.INFO, "Loaded " + hotspots.size() + " static hotspots");
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
            delayedDestroyHotspot(hotspot);
        }
        hotspots.clear();
        if(defaultHotspot != null)
        {
            destroyHotspot(defaultHotspot);
            defaultHotspot = null;
        }
        LazyDirector.Log(Level.INFO, "Destroyed all hotspots");
    }

    private final List<Hotspot> hotspots = new LinkedList<>();
    private Hotspot defaultHotspot = null;

    /**
     * <p>
     *     Get all hotspots sorted by heat.
     * </p>
     * @return The sorted hotspots
     */
    public @NotNull List<Hotspot> getAllHotspotsSorted()
    {
        List<Hotspot> sortedHotspots = new ArrayList<>(hotspots);
        Collections.sort(sortedHotspots);
        return sortedHotspots.reversed();
    }

    /**
     * <p>
     *     Get the default hotspot.
     * </p>
     * @return The default hotspot
     */
    public @NotNull Hotspot getDefaultHotspot()
    {
        return defaultHotspot;
    }

    /**
     * <p>
     *     Create a static hotspot.
     * </p>
     * @param staticHotspotConfigNode The configuration node to create the hotspot from
     * @return The created hotspot
     */
    public @NotNull StaticHotspot createStaticHotspot(@NotNull ConfigurationNode staticHotspotConfigNode)
    {
        StaticHotspot staticHotspot = new StaticHotspot(staticHotspotConfigNode);
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
    public @NotNull ActorHotspot createActorHotspot(@NotNull Actor actor)
    {
        ActorHotspot actorHotspot = new ActorHotspot(actor);
        hotspots.add(actorHotspot);
        LazyDirector.Log(Level.INFO, "Created hotspot: " + actorHotspot);
        return actorHotspot;
    }

    /**
     * <p>
     *     Create an empty actor group hotspot.
     * </p>
     * @return The created hotspot
     */
    public @NotNull ActorGroupHotspot createActorGroupHotspot(Actor initActor)
    {
        ActorGroupHotspot actorGroupHotspot = new ActorGroupHotspot(initActor);
        hotspots.add(actorGroupHotspot);
        LazyDirector.Log(Level.INFO, "Created hotspot: " + actorGroupHotspot);
        return actorGroupHotspot;
    }

    /**
     * <p>
     *     Join two actors into the same actor group hotspot.
     * </p>
     * <p>
     *     If both actors are not in any actor group hotspot, create a new one.
     *     <br>
     *     If both actors are already in the same actor group hotspot, return it.
     *     <br>
     *     If one of the actors is in an actor group hotspot, join the other actor to it.
     *     <br>
     *     If both actors are in different actor group hotspots, merge them.
     * </p>
     * @param actorA The first actor
     * @param actorB The second actor
     * @return The actor group hotspot that the actors are joined in
     */
    public @NotNull ActorGroupHotspot joinActorGroupHotspot(@NotNull Actor actorA, @NotNull Actor actorB)
    {
        ActorGroupHotspot actorGroupHotspotA = actorA.getActorGroupHotspot();
        ActorGroupHotspot actorGroupHotspotB = actorB.getActorGroupHotspot();
        // if same
        if(actorGroupHotspotA == actorGroupHotspotB)
        {
            // if both actorGroupHotspots are null, create a new one
            if(actorGroupHotspotA == null)
            {
                ActorGroupHotspot actorGroupHotspot = createActorGroupHotspot(actorA);
                actorA.setActorGroupHotspot(actorGroupHotspot);
                actorGroupHotspot.addActor(actorB);
                actorB.setActorGroupHotspot(actorGroupHotspot);
                return actorGroupHotspot;
            }
            // if both actorGroupHotspots are the same, just return it
            else
            {
                return actorGroupHotspotA;
            }
        }
        // if not same
        else
        {
            // if one of the actorGroupHotspots is null, set this to the other one
            if(actorGroupHotspotA == null)
            {
                actorA.setActorGroupHotspot(actorGroupHotspotB);
                actorGroupHotspotB.addActor(actorA);
                return actorGroupHotspotB;
            }
            else if(actorGroupHotspotB == null)
            {
                actorB.setActorGroupHotspot(actorGroupHotspotA);
                actorGroupHotspotA.addActor(actorB);
                return actorGroupHotspotA;
            }
            // if both are not null, merge them
            else
            {
                for(Actor actor : actorGroupHotspotB.getActors())
                {
                    actorGroupHotspotB.removeActor(actor);
                    actor.setActorGroupHotspot(actorGroupHotspotA);
                    actorGroupHotspotA.addActor(actor);
                }
                destroyHotspot(actorGroupHotspotB);
                return actorGroupHotspotA;
            }
        }
    }

    /**
     * <p>
     *     Destroy a hotspot.
     * </p>
     * @param hotspot The hotspot to destroy
     */
    public void destroyHotspot(@NotNull Hotspot hotspot)
    {
        LazyDirector.Log(Level.INFO, "Destroying hotspot: " + hotspot);
        hotspot.destroy();
        hotspots.remove(hotspot);
    }

    public void delayedDestroyHotspot(@NotNull Hotspot hotspot)
    {
        LazyDirector.Log(Level.INFO, "Destroying hotspot: " + hotspot);
        hotspot.destroy();
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
        // remove destroyed hotspots
        hotspots.removeIf(hotspot -> !hotspot.isValid());
        // update all hotspots
        for(Hotspot hotspot : hotspots)
        {
            hotspot.update();
        }
        // remove destroyed hotspots
        hotspots.removeIf(hotspot -> !hotspot.isValid());
    }
}