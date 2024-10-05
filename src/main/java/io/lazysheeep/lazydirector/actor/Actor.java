package io.lazysheeep.lazydirector.actor;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.ActorGroupHotspot;
import io.lazysheeep.lazydirector.hotspot.ActorHotspot;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 *     An actor is a wrapper of a player. An actor can produce heat and is related to some hotspots.
 * </p>
 * <p>
 *     All actors are managed by the {@link ActorManager}.
 *     <br>
 *     The creation an destruction of actors are automatically handled by the {@link ActorManager}.
 * </p>
 */
public class Actor
{
    private Player hostPlayer;
    private ActorHotspot actorHotspot;
    private ActorGroupHotspot actorGroupHotspot;

    /**
     * <p>
     *     Get the host player
     * </p>
     * <p>
     *     Make sure to check if the actor is valid before calling this method.
     *     <br>
     *     If the actor is invalid, an {@link IllegalStateException} will be thrown
     * </p>
     * @return The host player
     */
    public @NotNull Player getHostPlayer()
    {
        if(isValid())
        {
            return hostPlayer;
        }
        else
        {
            throw new IllegalStateException("Cannot get host player: actor is invalid");
        }
    }

    /**
     * <p>
     *     Get the actor hotspot
     * </p>
     * <p>
     *     If the actor is invalid, an {@link IllegalStateException} will be thrown.
     *     <br>
     *     If the actor hotspot is invalid, a new actor hotspot will be created.
     * </p>
     *
     * @return The actor hotspot
     */
    public @NotNull ActorHotspot getActorHotspot()
    {
        if(isValid())
        {
            if(!actorHotspot.isValid())
            {
                actorHotspot = LazyDirector.GetPlugin().getHotspotManager().createActorHotspot(this);
            }
            return actorHotspot;
        }
        else
        {
            throw new IllegalStateException("Cannot get actor hotspot: actor is invalid");
        }
    }

    /**
     * <p>
     *     Get the actor group hotspot
     * </p>
     * <p>
     *     If the actor is invalid, an {@link IllegalStateException} will be thrown.
     * </p>
     *
     * @return The actor group hotspot, may be null if the actor don't belong to any group hotspot
     */
    public @Nullable ActorGroupHotspot getActorGroupHotspot()
    {
        if(isValid())
        {
            return actorGroupHotspot;
        }
        else
        {
            throw new IllegalStateException("Cannot get actor group hotspot: actor is invalid");
        }
    }

    public void setActorGroupHotspot(@Nullable ActorGroupHotspot actorGroupHotspot)
    {
        if(this.actorGroupHotspot != null)
        {
            this.actorGroupHotspot.removeActor(this);
        }
        this.actorGroupHotspot = actorGroupHotspot;
        if(actorGroupHotspot != null)
        {
            actorGroupHotspot.addActor(this);
        }
    }

    /**
     * <p>
     *     The constructor of the actor.
     * </p>
     * <p>
     *     Should only be called by {@link ActorManager}.
     * </p>
     * @param hostPlayer The host player
     */
    Actor(@NotNull Player hostPlayer)
    {
        this.hostPlayer = hostPlayer;
        actorHotspot = LazyDirector.GetPlugin().getHotspotManager().createActorHotspot(this);
    }

    /**
     * <p>
     *     Destroy the actor.
     * </p>
     * <p>
     *     The actor should no longer be accessed after this method is called.
     * </p>
     * <p>
     *     Should only be called by the {@link ActorManager}.
     * </p>
     */
    void destroy()
    {
        LazyDirector.GetPlugin().getHotspotManager().destroyHotspot(actorHotspot);
        actorHotspot = null;
        if(actorGroupHotspot != null)
        {
            setActorGroupHotspot(null);
        }
        hostPlayer = null;
    }

    /**
     * <p>
     *     Check if the actor is valid.
     * </p>
     * <p>
     *     The actor should be valid after creation, and become invalid after being destroyed.
     * </p>
     * @return Whether the actor is valid.
     */
    public boolean isValid()
    {
        return hostPlayer != null;
    }

    /**
     * <p>
     *     The shortcut for increasing the heat of the actor hotspot.
     * </p>
     * @param heatTypeName The name of the heat type
     * @see ActorHotspot#increase(String)
     */
    public void increase(String heatTypeName)
    {
        actorHotspot.increase(heatTypeName);
    }

    /**
     * <p>
     *     The shortcut for increasing the heat of the actor hotspot.
     * </p>
     * @param heatTypeName The name of the heat type
     * @param multiplier The multiplier to apply to the heat increment
     * @see ActorHotspot#increase(String, float)
     */
    public final void increase(String heatTypeName, float multiplier)
    {
        actorHotspot.increase(heatTypeName, multiplier);
    }

    /**
     * <p>
     *     Called once every tick by the {@link ActorManager}.
     * </p>
     */
    void update()
    {
        hostPlayer.getWorld().getNearbyPlayers(hostPlayer.getLocation(), 8.0f).forEach(nearByPlayer -> {
            if(nearByPlayer != hostPlayer)
            {
                Actor nearByActor = LazyDirector.GetPlugin().getActorManager().getActor(nearByPlayer);
                if(nearByActor != null)
                {
                    LazyDirector.GetPlugin().getHotspotManager().joinActorGroupHotspot(this, nearByActor);
                    actorHotspot.increase("player_group_gathering");
                }
            }
        });

        if(actorHotspot.getHeat("player_group_gathering") <= 0.0f)
        {
            setActorGroupHotspot(null);
        }
    }

    @Override
    public String toString()
    {
        return hostPlayer.getName();
    }
}
