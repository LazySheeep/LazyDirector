package io.lazysheeep.lazydirector.hotspot;

import io.lazysheeep.lazydirector.actor.Actor;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ActorGroupHotspot extends Hotspot
{
    private final List<Actor> actors;

    ActorGroupHotspot(Actor... initActors)
    {
        actors = new LinkedList<>(Arrays.asList(initActors));
    }

    public void remove(Actor actor)
    {
        actors.remove(actor);
    }

    @Override
    protected void additionalDestroy()
    {
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
    public @NotNull Location getLocation()
    {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    protected String additionalToString()
    {
        return "";
    }
}
