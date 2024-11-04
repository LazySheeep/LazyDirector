package io.lazysheeep.lazydirector.hotspot;

import io.lazysheeep.lazydirector.actor.Actor;
import org.bukkit.Location;

public class ActorHotspot extends Hotspot
{
    private Actor actor;

    ActorHotspot(Actor actor)
    {
        this.actor = actor;
        heat("hunger");
    }

    @Override
    protected void additionalDestroy()
    {
        actor = null;
    }

    @Override
    public boolean isValid()
    {
        return actor != null;
    }

    @Override
    protected void additionalUpdate()
    {
        // Do nothing
    }

    @Override
    public Location getLocation()
    {
        return actor.getHostPlayer().getEyeLocation();
    }

    @Override
    public String additionalToString()
    {
        return "Actor: " + actor;
    }

}
