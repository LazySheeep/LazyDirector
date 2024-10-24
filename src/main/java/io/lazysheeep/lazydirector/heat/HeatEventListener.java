package io.lazysheeep.lazydirector.heat;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.actor.Actor;
import io.lazysheeep.lazydirector.events.HotspotBeingFocusedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        if(event.getDamager() instanceof Player)
        {
            Actor actor = LazyDirector.GetPlugin().getActorManager().getActor((Player) event.getDamager());
            if(actor != null)
            {
                actor.increase("player_hurt_entity");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Actor actor = LazyDirector.GetPlugin().getActorManager().getActor(event.getPlayer());
        if(actor != null)
        {
            actor.increase("player_place_block");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event)
    {
        Actor actor = LazyDirector.GetPlugin().getActorManager().getActor(event.getPlayer());
        if(actor != null)
        {
            actor.increase("player_break_block");
        }
    }
}
