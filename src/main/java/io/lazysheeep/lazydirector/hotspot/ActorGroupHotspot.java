package io.lazysheeep.lazydirector.hotspot;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.actor.Actor;
import io.lazysheeep.lazydirector.util.MathUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class ActorGroupHotspot extends Hotspot
{
    private List<Actor> actors = new LinkedList<>();
    private World world = null;

    public List<Actor> getActors()
    {
        return new ArrayList<>(actors);
    }

    public void addActor(Actor actor)
    {
        if(!actors.contains(actor))
        {
            actors.add(actor);
            if(world == null)
            {
                world = actor.getHostPlayer().getWorld();
            }
        }
    }

    public void removeActor(Actor actor)
    {
        actors.remove(actor);
    }

    ActorGroupHotspot() {}

    @Override
    public float getHeat()
    {
        float maxHeat = 0.0f;
        for(Actor actor : actors)
        {
            maxHeat = Math.max(maxHeat, actor.getActorHotspot().getHeat());
        }
        return maxHeat + 1.0f;
    }

    @Override
    protected void additionalDestroy()
    {
        for(Actor actor : actors)
        {
            actor.setActorGroupHotspot(null);
        }
        actors.clear();
        actors = null;
    }

    @Override
    public boolean isValid()
    {
        return actors != null;
    }

    @Override
    protected void additionalUpdate()
    {
        if(actors.isEmpty())
        {
            LazyDirector.GetPlugin().getHotspotManager().destroyHotspot(this);
            return;
        }
        // remove actors that are too far away
        for(Actor actor : actors)
        {
            if(MathUtils.Distance(actor.getHostPlayer().getLocation(), getLocation()) > 32.0d)
            {
                actor.setActorGroupHotspot(null);
            }
        }
    }

    @Override
    public @NotNull Location getLocation()
    {
        Location location = new Location(actors.getFirst().getHostPlayer().getWorld(), 0, 0, 0);
        for(Actor actor : actors)
        {
            location.add(actor.getHostPlayer().getEyeLocation());
        }
        return location.multiply(1.0 / actors.size());
    }

    @Override
    protected String additionalToString()
    {
        return "actors=" + actors;
    }
}
