package io.lazysheeep.lazydirector;

import io.lazysheeep.lazydirector.actor.Actor;
import io.lazysheeep.lazydirector.events.HotspotBeingFocusedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class HeatEventListener implements Listener
{
    @EventHandler(priority = EventPriority.MONITOR)
    public void onHotspotBeingFocused(HotspotBeingFocusedEvent event)
    {
        event.getHotspot().increase("hunger");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Actor actor = LazyDirector.GetPlugin().getActorManager().getActor(event.getPlayer());
        if(actor != null)
        {
            if(event.hasChangedBlock())
            {
                actor.increase("player_movement");
            }
        }
    }
}
