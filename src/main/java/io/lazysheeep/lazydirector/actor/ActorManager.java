package io.lazysheeep.lazydirector.actor;

import io.lazysheeep.lazydirector.LazyDirector;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class ActorManager
{
    private final List<World> stageWorlds = new ArrayList<>();
    private final List<GameMode> actorGameModes = new ArrayList<>();

    public ActorManager() {}

    /**
     * <p>
     *     Load or reload the configuration for the actor manager.
     * </p>
     * @param configNode The actorManager configuration node.
     * @return The actor manager itself.
     * @throws ConfigurateException
     */
    public @NotNull ActorManager loadConfig(@NotNull ConfigurationNode configNode) throws ConfigurateException
    {
        resetConfig();

        for (ConfigurationNode stageWorldNode : configNode.node("stageWorlds").childrenList())
        {
            String worldName = stageWorldNode.getString("no_value");
            World world = LazyDirector.GetPlugin().getServer().getWorld(worldName);
            if (world != null)
            {
                stageWorlds.add(world);
            }
            else
            {
                throw new ConfigurateException(stageWorldNode, "World not found: " + worldName);
            }
        }
        if(stageWorlds.isEmpty())
        {
            throw new ConfigurateException(configNode.node("stageWorlds"), "No stage worlds found");
        }
        for (ConfigurationNode actorGameModeNode : configNode.node("actorGameModes").childrenList())
        {
            String gameModeName = actorGameModeNode.getString("no_value");
            GameMode gameMode = GameMode.valueOf(gameModeName.toUpperCase());
            actorGameModes.add(gameMode);
        }
        if(actorGameModes.isEmpty())
        {
            throw new ConfigurateException(configNode.node("actorGameModes"), "No actor game modes found");
        }
        return this;
    }

    /**
     * <p>
     *     Reset the configuration.
     * </p>
     */
    private void resetConfig()
    {
        stageWorlds.clear();
        actorGameModes.clear();
    }

    /**
     * <p>
     *     Destroy all actors.
     * </p>
     */
    public void destroy()
    {
        for (Actor actor : actors)
        {
            destroyActor(actor);
        }
    }

    private final List<Actor> actors = new LinkedList<>();

    /**
     * <p>
     *     Create a new actor.
     * </p>
     * <p>
     *     This should be the only way to create an actor.
     * </p>
     * @param hostPlayer The host player
     * @return The new actor
     */
    private Actor createActor(@NotNull Player hostPlayer)
    {
        Actor actor = new Actor(hostPlayer);
        hostPlayer.setMetadata("Actor", new FixedMetadataValue(LazyDirector.GetPlugin(), actor));
        actors.add(actor);
        LazyDirector.Log(Level.INFO, "Created actor: " + actor);
        return actor;
    }

    /**
     * <p>
     *     Destroy an actor.
     * </p>
     * <p>
     *     This should be the only way to destroy an actor.
     * </p>
     * @param actor The actor to destroy
     */
    private void destroyActor(@NotNull Actor actor)
    {
        LazyDirector.Log(Level.INFO, "Destroying actor: " + actor);
        if(actor.isValid())
        {
            actor.getHostPlayer().removeMetadata("Actor", LazyDirector.GetPlugin());
        }
        actor.destroy();
        actors.remove(actor);
    }

    /**
     * <p>
     *     Get the actor corresponding to a player.
     * </p>
     * @param hostPlayer The host player
     * @return The corresponding actor, or null if the player is not an actor
     */
    public @Nullable Actor getActor(@NotNull Player hostPlayer)
    {
        for(Actor actor : actors)
        {
            if(actor.isValid() && actor.getHostPlayer().equals(hostPlayer))
            {
                return actor;
            }
        }
        return null;
    }

    /**
     * <p>
     *     Check if a player has an corresponding actor.
     * </p>
     * @param player The player
     * @return Whether the player is an actor
     */
    private boolean isActor(Player player)
    {
        return getActor(player) != null;
    }

    /**
     * <p>
     *     Check if a player should be an actor.
     * </p>
     * @param player The player
     * @return Whether the player should be an actor
     */
    private boolean shouldBeActor(Player player)
    {
        return player.isOnline() && stageWorlds.contains(player.getWorld()) && actorGameModes.contains(player.getGameMode());
    }

    /**
     * <p>
     *     Check if an actor should be an actor.
     * </p>
     * @param actor The actor
     * @return Whether the actor should be an actor
     */
    private boolean shouldBeActor(Actor actor)
    {
        return actor.isValid() && shouldBeActor(actor.getHostPlayer());
    }

    /**
     * <p>
     *     Maintain the actors list and call the update method of each actor.
     * </p>
     * <p>
     *     Called once every tick by the {@link LazyDirector}.
     * </p>
     */
    public void update()
    {
        // Remove actors that should not be actors
        for (Actor actor : actors)
        {
            if (!shouldBeActor(actor))
            {
                destroyActor(actor);
            }
        }
        // Create actors that should be actors
        for (Player player : LazyDirector.GetPlugin().getServer().getOnlinePlayers())
        {
            if (shouldBeActor(player) && !isActor(player))
            {
                createActor(player);
            }
        }
        // Update actors
        for (Actor actor : actors)
        {
            actor.update();
        }
    }
}
