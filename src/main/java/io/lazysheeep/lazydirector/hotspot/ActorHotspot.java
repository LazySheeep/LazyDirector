package io.lazysheeep.lazydirector.hotspot;

import io.lazysheeep.lazydirector.Heat;
import io.lazysheeep.lazydirector.HeatType;
import io.lazysheeep.lazydirector.actor.Actor;
import org.bukkit.Location;

public class ActorHotspot extends Hotspot
{
    private Actor actor;

    ActorHotspot(Actor actor)
    {
        this.actor = actor;
    }

    @Override
    public void destroy()
    {
        actor = null;
    }

    @Override
    public void update()
    {
        for(HeatType type : HeatType.values())
        {
            Heat heat = heats[type.ordinal()];
            if(heat != null)
            {
                heat.coolDown();
            }
        }
    }

    @Override
    public Location getLocation()
    {
        return actor.hostPlayer.getEyeLocation();
    }

}
