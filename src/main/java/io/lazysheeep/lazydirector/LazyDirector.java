package io.lazysheeep.lazydirector;

import co.aikar.commands.PaperCommandManager;
import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.lazysheeep.lazydirector.actor.ActorManager;
import io.lazysheeep.lazydirector.director.Cameraman;
import io.lazysheeep.lazydirector.director.Director;
import io.lazysheeep.lazydirector.heat.HeatType;
import io.lazysheeep.lazydirector.hotspot.HotspotManager;
import io.lazysheeep.lazydirector.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

import java.io.File;
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
    public static void Log(java.util.logging.Level level, String message)
    {
        instance.getLogger().log(level, "[t" + instance.getServer().getCurrentTick() + "] " + message);
    }

    /**
     * The director instance.
     */
    private final Director director = new Director();

    /**
     * <p>
     *     Get the director instance.
     * </p>
     * @return The director instance
     */
    public Director getDirector()
    {
        return director;
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
    public ActorManager getActorManager()
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
    public HotspotManager getHotspotManager()
    {
        return hotspotManager;
    }

    private final HeatEventListener heatEventListener = new HeatEventListener();

    private boolean isActive = false;

    public boolean isActive()
    {
        return isActive;
    }

    /**
     * <p>
     *     Active the plugin.
     * </p>
     * <p>
     *     Load the configurations and register events.
     * </p>
     * @param configFileName The name of the configuration file.
     */
    public void activate(String configFileName)
    {
        if(!isActive)
        {
            // load config
            Log(Level.INFO, "Loading configurations...");
            if(!loadConfig(configFileName))
            {
                Log(Level.SEVERE, "Failed to load configurations!");
                return;
            }
            // register events
            Log(Level.INFO, "Registering events...");
            Bukkit.getPluginManager().registerEvents(this, this);
            Bukkit.getPluginManager().registerEvents(heatEventListener, this);
            // set flag
            isActive = true;
        }
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
            // unregister events
            HandlerList.unregisterAll((Plugin) this);
            // destroy
            director.destroy();
            actorManager.destroy();
            hotspotManager.destroy();
            // set flag
            isActive = false;
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
        commandManager.getCommandCompletions().registerCompletion("configNames", c -> FileUtils.getAllFileNames(getDataFolder().getPath()));
        commandManager.getCommandCompletions().registerCompletion("cameramen", c -> getDirector().getAllCameramen().stream().map(Cameraman::getName).collect(Collectors.toList()));
        commandManager.registerCommand(new LazyDirectorCommand());
        // activate
        if(FileUtils.getAllFileNames(getDataFolder().getPath()).contains("default.conf"))
        {
            activate("default.conf");
        }
        else
        {
            Log(Level.WARNING, "\"default.conf\" not found, you'll have to activate LazyDirector manually.");
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
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder().path(Path.of(getDataFolder().getPath(), configFileName)).build();
        try
        {
            ConfigurationNode rootNode = loader.load();

            ConfigurationNode heatTypesNode = rootNode.node("heatTypes");
            HeatType.LoadConfig(heatTypesNode);

            ConfigurationNode hotspotManagerNode = rootNode.node("hotspotManager");
            hotspotManager.loadConfig(hotspotManagerNode);

            ConfigurationNode actorManagerNode = rootNode.node("actorManager");
            actorManager.loadConfig(actorManagerNode);

            ConfigurationNode directorNode = rootNode.node("director");
            director.loadConfig(directorNode);
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
        director.update();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerTickEnd(ServerTickEndEvent event)
    {
        director.lateUpdate();
    }

    private @Nullable FileConfiguration loadCustomConfig(@NotNull String fileName)
    {
        File configFile = new File(getDataFolder(), fileName);
        if (!configFile.exists())
        {
            return null;
        }
        return YamlConfiguration.loadConfiguration(configFile);
    }
}
