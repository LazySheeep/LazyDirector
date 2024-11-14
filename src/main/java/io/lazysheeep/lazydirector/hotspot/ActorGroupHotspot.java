package io.lazysheeep.lazydirector.hotspot;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.actor.Actor;
import io.lazysheeep.lazydirector.util.MathUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ActorGroupHotspot extends Hotspot
{
    private List<Actor> actors = new LinkedList<>();
    private final World world;

    ActorGroupHotspot(Actor initActor)
    {
        actors.add(initActor);
        world = initActor.getPlayer().getWorld();
    }

    public List<Actor> getActors()
    {
        return new ArrayList<>(actors);
    }

    public void addActor(Actor actor)
    {
        if(!actors.contains(actor))
        {
            actors.add(actor);
        }
    }

    public void removeActor(Actor actor)
    {
        actors.remove(actor);
    }

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
    public int heat(String heatTypeName)
    {
        int ret = 0;
        for(Actor actor : actors)
        {
            ret = actor.heat(heatTypeName);
        }
        return ret;
    }

    @Override
    public int heat(String heatTypeName, float multiplier)
    {
        int ret = 0;
        for(Actor actor : actors)
        {
            ret = actor.heat(heatTypeName, multiplier);
        }
        return ret;
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
        // remove invalid actors
        actors.removeIf(actor -> !actor.isValid());
        // remove actors that are in different worlds
        for(Actor actor : actors)
        {
            if(actor.getPlayer().getWorld() != world)
            {
                actor.setActorGroupHotspot(null);
            }
        }
        actors.removeIf(actor -> actor.getActorGroupHotspot() != this);
        // remove actors that are too far away
        if(!actors.isEmpty())
        {
            Location location = getLocation();
            for(Actor actor : actors)
            {
                if(MathUtils.Distance(actor.getPlayer().getLocation(), location) > 32.0d)
                {
                    actor.setActorGroupHotspot(null);
                }
            }
            actors.removeIf(actor -> actor.getActorGroupHotspot() != this);
        }
        // destroy if no actors left
        if(actors.isEmpty())
        {
            LazyDirector.GetPlugin().getHotspotManager().delayedDestroyHotspot(this);
        }
    }

    @Override
    public @NotNull Location getLocation()
    {
        Location location = new Location(actors.getFirst().getPlayer().getWorld(), 0, 0, 0);
        for(Actor actor : actors)
        {
            location.add(actor.getPlayer().getEyeLocation());
        }
        return location.multiply(1.0 / actors.size());
    }

    @Override
    protected String additionalToString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Actors:");
        actors.forEach(actor -> stringBuilder.append(" ").append(actor));
        return stringBuilder.toString();
    }
}
