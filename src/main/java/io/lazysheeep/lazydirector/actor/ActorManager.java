package io.lazysheeep.lazydirector.actor;

import io.lazysheeep.lazydirector.LazyDirector;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

public class ActorManager
{
    private static final List<Actor> actors = new LinkedList<>();

    private final List<World> stageWorlds = new ArrayList<>();
    private final List<GameMode> actorGameModes = new ArrayList<>();

    public ActorManager() {}

    public ActorManager loadConfig(ConfigurationNode configNode) throws ConfigurateException
    {
        stageWorlds.clear();
        actorGameModes.clear();

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

    private Actor createActor(@NotNull Player player)
    {
        Actor actor = new Actor(player);
        player.setMetadata("Actor", new FixedMetadataValue(LazyDirector.GetPlugin(), actor));
        actors.add(actor);
        LazyDirector.GetPlugin().getLogger().log(Level.INFO, "Created actor: " + actor);
        return actor;
    }

    private void destroyActor(Actor actor)
    {
        LazyDirector.GetPlugin().getLogger().log(Level.INFO, "Destroying actor: " + actor);
        actor.hostPlayer.removeMetadata("Actor", LazyDirector.GetPlugin());
        actor.destroy();
        actors.remove(actor);
    }

    public Actor getActor(Player player)
    {
        if (!player.isOnline())
            return null;

        for (MetadataValue metaData : player.getMetadata("Actor"))
        {
            if (metaData.getOwningPlugin() == LazyDirector.GetPlugin() && metaData.value() instanceof Actor actor)
                return actor;
        }

        return null;
    }

    public void update()
    {
        // Remove actors that should not be actors
        for (Actor actor : actors)
        {
            if (!shouldBeActor(actor.hostPlayer))
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

    private boolean isActor(Player player)
    {
        return getActor(player) != null;
    }

    private boolean shouldBeActor(Actor actor)
    {
        return shouldBeActor(actor.hostPlayer);
    }

    private boolean shouldBeActor(Player player)
    {
        return player.isOnline() && stageWorlds.contains(player.getWorld()) && actorGameModes.contains(player.getGameMode());
    }
}
