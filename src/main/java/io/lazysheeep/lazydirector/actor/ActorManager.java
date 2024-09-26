package io.lazysheeep.lazydirector.actor;

import io.lazysheeep.lazydirector.LazyDirector;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ActorManager
{
    private static final List<Actor> actors = new LinkedList<>();

    private final List<World> stageWorlds = new ArrayList<>();
    private final List<GameMode> actorGameModes = Arrays.asList(GameMode.SURVIVAL, GameMode.ADVENTURE);

    public ActorManager(FileConfiguration fileConfig)
    {
        fileConfig.getStringList("stageWorlds").forEach(worldName -> {
            World world = LazyDirector.getPlugin().getServer().getWorld(worldName);
            if (world != null)
            {
                stageWorlds.add(world);
            }
            else
            {
                LazyDirector.getPlugin().getLogger().warning("World not found: " + worldName);
                LazyDirector.getPlugin().getLogger().warning("Existing worlds: " + LazyDirector.getPlugin().getServer().getWorlds());
            }
        });
    }

    private Actor createActor(@NotNull Player player)
    {
        Actor actor = new Actor(player);
        player.setMetadata("Actor", new FixedMetadataValue(LazyDirector.getPlugin(), actor));
        actors.add(actor);
        return actor;
    }

    public Actor getActor(Player player)
    {
        for(MetadataValue metaData : player.getMetadata("Actor"))
        {
            if(metaData.getOwningPlugin() == LazyDirector.getPlugin() && metaData.value() instanceof Actor actor)
                return actor;
        }
        return null;
    }

    private void destroyActor(Actor actor)
    {
        actor.hostPlayer.removeMetadata("Actor", LazyDirector.getPlugin());
        actor.destroy();
        actors.remove(actor);
    }

    public void update()
    {
        // Remove actors that should not be actors
        for(Actor actor : actors)
        {
            if(!shouldBeActor(actor.hostPlayer))
            {
                destroyActor(actor);
            }
        }
        // Create actors that should be actors
        for(Player player : LazyDirector.getPlugin().getServer().getOnlinePlayers())
        {
            if(shouldBeActor(player) && !isActor(player))
            {
                createActor(player);
            }
        }
        // Update actors
        for(Actor actor : actors)
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
        return stageWorlds.contains(player.getWorld()) && actorGameModes.contains(player.getGameMode()) && !player.isOp();
    }
}
