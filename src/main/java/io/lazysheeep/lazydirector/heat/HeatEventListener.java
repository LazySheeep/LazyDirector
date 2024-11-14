package io.lazysheeep.lazydirector.heat;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.actor.Actor;
import io.lazysheeep.lazydirector.events.HotspotBeingFocusedEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class HeatEventListener implements Listener
{
    @EventHandler(priority = EventPriority.MONITOR)
    public void onHotspotBeingFocused(HotspotBeingFocusedEvent event)
    {
        event.getHotspot().heat("hunger");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Actor actor = LazyDirector.GetPlugin().getActorManager().getActor(event.getPlayer());
        if (actor != null)
        {
            if (event.hasChangedBlock())
            {
                actor.heat("player_movement");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Actor actor = LazyDirector.GetPlugin().getActorManager().getActor(event.getPlayer());
        if (actor != null)
        {
            Location blockLocation = event.getBlock().getLocation();
            if (!actor.recentInteractedLocations.contains(blockLocation))
            {
                actor.heat("player_place_block");
                actor.recentInteractedLocations.add(blockLocation);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event)
    {
        Actor actor = LazyDirector.GetPlugin().getActorManager().getActor(event.getPlayer());
        if (actor != null)
        {
            Location blockLocation = event.getBlock().getLocation();
            if (!actor.recentInteractedLocations.contains(blockLocation))
            {
                actor.heat("player_break_block");
                actor.recentInteractedLocations.add(blockLocation);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            Block interationBlock = event.getClickedBlock();
            if (interationBlock != null && interationBlock.getState() instanceof Container)
            {
                Location blockLocation = interationBlock.getLocation();
                Actor actor = LazyDirector.GetPlugin().getActorManager().getActor(event.getPlayer());
                if (actor != null && !actor.recentInteractedLocations.contains(blockLocation))
                {
                    actor.heat("player_open_container");
                    actor.recentInteractedLocations.add(blockLocation);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        Entity damager = event.getDamageSource().getCausingEntity();
        if (damager == null)
        {
            return;
        }
        Entity victim = event.getEntity();
        float damage = (float) event.getFinalDamage();

        if (damager instanceof Player damagerPlayer)
        {
            Actor damagerActor = LazyDirector.GetPlugin().getActorManager().getActor(damagerPlayer);
            if (damagerActor != null)
            {
                if (victim instanceof Player victimPlayer)
                {
                    damagerActor.heat("player_attack_player", damage);

                    Actor victimActor = LazyDirector.GetPlugin().getActorManager().getActor(victimPlayer);
                    if (victimActor != null)
                    {
                        victimActor.heat("player_attacked_by_player", damage);
                    }
                }
                else
                {
                    damagerActor.heat("player_attack_entity", damage);
                }
            }
        }
        else
        {
            if (victim instanceof Player victimPlayer)
            {
                Actor victimActor = LazyDirector.GetPlugin().getActorManager().getActor(victimPlayer);
                if (victimActor != null)
                {
                    victimActor.heat("player_attacked_by_entity", damage);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event)
    {
        Player killerPlayer = event.getEntity().getKiller();
        if (killerPlayer == null)
        {
            return;
        }
        Entity victim = event.getEntity();

        Actor killerActor = LazyDirector.GetPlugin().getActorManager().getActor(killerPlayer);
        if (killerActor != null)
        {
            if (victim instanceof Player)
            {
                killerActor.heat("player_kill_player");
            }
            else
            {
                killerActor.heat("player_kill_entity");
            }
        }
    }
}
