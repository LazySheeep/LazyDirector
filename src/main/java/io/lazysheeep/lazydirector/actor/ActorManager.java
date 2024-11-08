package io.lazysheeep.lazydirector.actor;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.localization.LocalizationManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class ActorManager
{
    private boolean askForPermission = true;
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
        askForPermission = configNode.node("askForPermission").getBoolean(true);
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
        askForPermission = true;
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
        actors.clear();
        LazyDirector.Log(Level.INFO, "Destroyed all actors");
    }

    private final List<Actor> actors = new LinkedList<>();

    /**
     * <p>
     *     Get a copy of all actors.
     * </p>
     * @return A copy of all actors
     */
    public List<Actor> getAllActors()
    {
        return new ArrayList<>(actors);
    }

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
        if(actor.isValid())
        {
            LazyDirector.Log(Level.INFO, "Destroying actor: " + actor);
            actor.getHostPlayer().removeMetadata("Actor", LazyDirector.GetPlugin());
            actor.destroy();
        }
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
        return player.isOnline() && (stageWorlds.isEmpty() || stageWorlds.contains(player.getWorld())) && actorGameModes.contains(player.getGameMode()) && getPermission(player) >= 0;
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
     *     Get whether a player has granted permission to be an actor.<br>
     *     When askForPermission is false, always return 1.
     * </p>
     * @param player The player
     * @return
     * <p>
     *     1: granted<br>
     *     0: default, haven't granted or denied<br>
     *     -1: denied
     * </p>
     */
    public int getPermission(Player player)
    {
        if(askForPermission)
        {
            if(player.getScoreboardTags().contains("LazyDirector_" + LazyDirector.GetPlugin().getRecentConfigName() + "_permission_granted"))
            {
                return 1;
            }
            else if(player.getScoreboardTags().contains("LazyDirector_" + LazyDirector.GetPlugin().getRecentConfigName() + "_permission_denied"))
            {
                return -1;
            }
            else
            {
                return 0;
            }
        }
        else return 1;
    }

    public boolean grantPermission(Player player)
    {
        if(askForPermission)
        {
            player.removeScoreboardTag("LazyDirector_" + LazyDirector.GetPlugin().getRecentConfigName() + "_permission_denied");
            player.addScoreboardTag("LazyDirector_" + LazyDirector.GetPlugin().getRecentConfigName() + "_permission_granted");
            return true;
        }
        else return false;
    }

    public boolean revokePermission(Player player)
    {
        if(askForPermission)
        {
            player.removeScoreboardTag("LazyDirector_" + LazyDirector.GetPlugin().getRecentConfigName() + "_permission_granted");
            player.addScoreboardTag("LazyDirector_" + LazyDirector.GetPlugin().getRecentConfigName() + "_permission_denied");
            return true;
        }
        else return false;
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
                // inform player
                actor.getHostPlayer().sendMessage(Component.text(LocalizationManager.GetLocalizedString("actor_manager_player_no_longer_actor", Locale.getDefault()), NamedTextColor.GRAY));

                destroyActor(actor);
            }
        }
        // Create actors that should be actors
        for (Player player : LazyDirector.GetPlugin().getServer().getOnlinePlayers())
        {
            if (shouldBeActor(player) && !isActor(player))
            {
                createActor(player);

                if(player.getScoreboardTags().contains("LazyDirector_" + LazyDirector.GetPlugin().getRecentConfigName() + "_permission_asked"))
                {
                    // inform player
                     player.sendMessage(Component.text(LocalizationManager.GetLocalizedString("actor_manager_player_become_actor", Locale.getDefault()), NamedTextColor.GRAY));
                }
                else if(askForPermission)
                {
                    // ask for permission
                    player.sendMessage(Component.text(LocalizationManager.GetLocalizedString("actor_manager_ask_for_permission_0", Locale.getDefault()), NamedTextColor.YELLOW));
                    player.sendMessage(Component.text("  [" + LocalizationManager.GetLocalizedString("actor_manager_ask_for_permission_yes", Locale.getDefault()) + "]", NamedTextColor.GREEN).clickEvent(ClickEvent.runCommand("/lazydirector permission grant")));
                    player.sendMessage(Component.text("  [" + LocalizationManager.GetLocalizedString("actor_manager_ask_for_permission_no", Locale.getDefault()) + "]", NamedTextColor.RED).clickEvent(ClickEvent.runCommand("/lazydirector permission revoke")));
                    player.sendMessage(Component.text(LocalizationManager.GetLocalizedString("actor_manager_ask_for_permission_1", Locale.getDefault()), NamedTextColor.YELLOW));
                    player.sendMessage(Component.text(LocalizationManager.GetLocalizedString("actor_manager_ask_for_permission_2", Locale.getDefault()), NamedTextColor.YELLOW));
                    player.addScoreboardTag("LazyDirector_" + LazyDirector.GetPlugin().getRecentConfigName() + "_permission_asked");
                }
            }
        }
        // Remove destroyed actors
        actors.removeIf(actor -> !actor.isValid());
        // Update actors
        for (Actor actor : actors)
        {
            actor.update();
        }
    }
}
