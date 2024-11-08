package io.lazysheeep.lazydirector;

import co.aikar.commands.PaperCommandManager;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.lazysheeep.lazydirector.actor.Actor;
import io.lazysheeep.lazydirector.actor.ActorManager;
import io.lazysheeep.lazydirector.command.LazyDirectorCommand;
import io.lazysheeep.lazydirector.command.PlayerSelectorResolver;
import io.lazysheeep.lazydirector.camera.Camera;
import io.lazysheeep.lazydirector.camera.CameraManager;
import io.lazysheeep.lazydirector.feature.ChatRepeater;
import io.lazysheeep.lazydirector.heat.HeatEventListener;
import io.lazysheeep.lazydirector.heat.HeatType;
import io.lazysheeep.lazydirector.hotspot.HotspotManager;
import io.lazysheeep.lazydirector.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * <p>
 *     The main class of the plugin.
 * </p>
 */
public final class LazyDirector extends JavaPlugin implements Listener
{
    /**
     * The singleton instance of the plugin.
     */
    private static LazyDirector instance;

    /**
     * <p>
     *     Get the singleton instance of the plugin.
     * </p>
     * @return The singleton instance of the plugin.
     */
    public static LazyDirector GetPlugin()
    {
        return instance;
    }

    /**
     * <p>
     *     A wrapper for logging.
     * </p>
     * @param level The level of the log
     * @param message The message to log
     */
    public static void Log(Level level, String message)
    {
        instance.getLogger().log(level, "[t" + instance.getServer().getCurrentTick() + "] " + message);
    }

    public static float GetServerTickRate()
    {
        return instance.getServer().getServerTickManager().getTickRate();
    }

    public static float GetServerTickDeltaTime()
    {
        return 1.0f / instance.getServer().getServerTickManager().getTickRate();
    }

    /**
     * The director instance.
     */
    private final CameraManager cameraManager = new CameraManager();
    /**
     * <p>
     *     Get the director instance.
     * </p>
     * @return The director instance
     */
    public @NotNull CameraManager getCameraManager()
    {
        return cameraManager;
    }

    /**
     * The actor manager instance.
     */
    private final ActorManager actorManager = new ActorManager();
    /**
     * <p>
     *     Get the actor manager instance.
     * </p>
     * @return The actor manager instance
     */
    public @NotNull ActorManager getActorManager()
    {
        return actorManager;
    }

    /**
     * The hotspot manager instance.
     */
    private final HotspotManager hotspotManager = new HotspotManager();
    /**
     * <p>
     *     Get the hotspot manager instance.
     * </p>
     * @return The hotspot manager instance
     */
    public @NotNull HotspotManager getHotspotManager()
    {
        return hotspotManager;
    }

    private final HeatEventListener heatEventListener = new HeatEventListener();

    private final ChatRepeater chatRepeater = new ChatRepeater();
    public @NotNull ChatRepeater getChatRepeater()
    {
        return chatRepeater;
    }

    private String recentConfigName = null;
    public @Nullable String getRecentConfigName()
    {
        return recentConfigName;
    }

    private boolean isActive = false;
    public boolean isActive()
    {
        return isActive;
    }

    /**
     * <p>
     *     Activate the plugin.
     * </p>
     * <p>
     *     Load the configurations and register events.
     * </p>
     * @param configFileName The name of the configuration file.
     */
    public boolean activate(String configFileName)
    {
        if(!isActive)
        {
            Log(Level.INFO, "Activating LazyDirector...");
            // load config
            Log(Level.INFO, "Loading configurations...");
            if(!loadConfig(configFileName))
            {
                Log(Level.SEVERE, "Failed to load configurations!");
                return false;
            }
            // register events
            Log(Level.INFO, "Registering events...");
            Bukkit.getPluginManager().registerEvents(this, this);
            Bukkit.getPluginManager().registerEvents(heatEventListener, this);
            // set flag
            isActive = true;
            Log(Level.INFO, "LazyDirector activated");
            return true;
        }
        return false;
    }

    /**
     * <p>
     *     Shutdown the plugin.
     * </p>
     * <p>
     *     Unregister events and destroy the managers.
     * </p>
     */
    public void shutdown()
    {
        if(isActive)
        {
            Log(Level.INFO, "Deactivating LazyDirector...");
            // unregister events
            HandlerList.unregisterAll((Plugin) this);
            // destroy
            cameraManager.destroy();
            actorManager.destroy();
            hotspotManager.destroy();
            // set flag
            isActive = false;
            Log(Level.INFO, "LazyDirector deactivated");
        }
    }

    @Override
    public void onEnable()
    {
        // set instance
        instance = this;
        // register command
        Log(Level.INFO, "Registering commands...");
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.getCommandCompletions().registerCompletion("configNames", c -> FileUtils.getAllFileNames(getDataFolder().getPath(), ".conf", false));
        commandManager.getCommandCompletions().registerCompletion("cameraNames", c -> getCameraManager().getAllCamera().stream().map(Camera::getName).collect(Collectors.toList()));
        commandManager.getCommandCompletions().registerCompletion("heatTypes", c -> HeatType.values().stream().map(HeatType::getName).collect(Collectors.toList()));
        commandManager.getCommandContexts().registerContext(Player[].class, new PlayerSelectorResolver());
        commandManager.registerCommand(new LazyDirectorCommand());
        // if config folder does not exist, create it and copy the default config
        if(!getDataFolder().exists())
        {
            getDataFolder().mkdir();
            saveResource("example.conf", false);
        }
        // activate
        if(FileUtils.getAllFileNames(getDataFolder().getPath()).contains("default.conf"))
        {
            activate("default.conf");
        }
        else
        {
            Log(Level.WARNING, "\"default.conf\" not found, you will have to activate LazyDirector manually.");
        }
    }

    @Override
    public void onDisable()
    {
        // shutdown
        shutdown();
        // set instance to null
        instance = null;
    }

    /**
     * <p>
     *     Load the configurations.
     * </p>
     * @return True if the configuration is loaded successfully, false otherwise.
     */
    private boolean loadConfig(String configFileName)
    {
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder().path(Path.of(getDataFolder().getPath(), configFileName + ".conf")).build();
        try
        {
            ConfigurationNode rootNode = loader.load();
            if(rootNode.empty())
            {
                Log(Level.SEVERE, "Empty configuration");
                return false;
            }

            ConfigurationNode heatTypesNode = rootNode.node("heatTypes");
            HeatType.LoadConfig(heatTypesNode);

            ConfigurationNode hotspotManagerNode = rootNode.node("hotspotManager");
            hotspotManager.loadConfig(hotspotManagerNode);

            ConfigurationNode actorManagerNode = rootNode.node("actorManager");
            actorManager.loadConfig(actorManagerNode);

            ConfigurationNode directorNode = rootNode.node("cameraManager");
            cameraManager.loadConfig(directorNode);
        }
        catch (ConfigurateException e)
        {
            Log(Level.SEVERE, "An error occurred while loading configuration at " + e.getMessage());
            return false;
        }
        catch (Exception e)
        {
            Log(Level.SEVERE, "An error occurred while loading configuration: " + Arrays.toString(e.getStackTrace()));
            return false;
        }
        recentConfigName = configFileName;
        return true;
    }

    /**
     * <p>
     *     Call the update method of the actor manager, hotspot manager, and director.
     * </p>
     * @param event The {@link ServerTickStartEvent} event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerTickStart(ServerTickStartEvent event)
    {
        actorManager.update();
        hotspotManager.update();
        cameraManager.update();
    }

    /**
     * <p>
     *     Heat a player.
     * </p>
     * @param player The player to heat
     * @param heatType The name of the heat type
     * @param multiplier The multiplier to apply to the heat increment
     * @return
     * <p>
     *     0 if the actor is heated successfully
     *     <br/>
     *     1 if the heat type is unknown
     *     <br/>
     *     2 if the player is not an actor
     *     <br/>
     *     3 if the plugin is not activated
     * </p>
     */
    public int heat(Player player, String heatType, float multiplier)
    {
        if(isActive())
        {
            Actor actor = getActorManager().getActor(player);
            if(actor != null)
            {
                return actor.heat(heatType, multiplier);
            }
            else
            {
                return 2;
            }
        }
        else
        {
            return 3;
        }
    }

    /**
     * <p>
     *     Heat a player if it is an actor.
     * </p>
     * @param player The player to heat
     * @param heatType The name of the heat type
     * @return
     * <p>
     *     0 if the actor is heated successfully
     *     <br/>
     *     1 if the heat type is unknown
     *     <br/>
     *     2 if the player is not an actor
     *     <br/>
     *     3 if the plugin is not activated
     * </p>
     */
    public int heat(Player player, String heatType)
    {
        return heat(player, heatType, 1.0f);
    }
}
