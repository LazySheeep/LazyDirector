package io.lazysheeep.lazydirector.hotspot;

import io.lazysheeep.lazydirector.actor.Actor;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ActorGatheringHotspot extends Hotspot
{
    private final List<Actor> actors;

    ActorGatheringHotspot(Actor... initActors)
    {
        actors = new LinkedList<>(Arrays.asList(initActors));
    }

    public void remove(Actor actor)
    {
        actors.remove(actor);
    }

    @Override
    protected void destroy()
    {
        for(Actor actor : actors)
        {
            actor.actorGatheringHotspot = null;
        }
        actors.clear();
    }

    @Override
    public boolean isValid()
    {
        return !actors.isEmpty();
    }

    @Override
    protected void additionalUpdate()
    {
        // Do nothing
    }


    @Override
    public Location getLocation()
    {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public String toString()
    {
        return "";
    }
}
