package io.lazysheeep.lazydirector;

import co.aikar.commands.PaperCommandManager;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.lazysheeep.lazydirector.actor.ActorManager;
import io.lazysheeep.lazydirector.director.Director;
import io.lazysheeep.lazydirector.heat.HeatType;
import io.lazysheeep.lazydirector.hotspot.HotspotManager;
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
import java.util.logging.Level;

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
        instance.getLogger().log(level, message);
    }

    /**
     * The director instance.
     */
    private Director director;

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
    private ActorManager actorManager;

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
    private HotspotManager hotspotManager;

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

    @Override
    public void onEnable()
    {
        instance = this;

        // register command
        Log(Level.INFO, "Registering commands...");
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new LazyDirectorCommand());

        // load config
        Log(Level.INFO, "Loading configurations...");
        if(!loadConfig())
        {
            Log(Level.SEVERE, "Failed to load configurations!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // register events
        Log(Level.INFO, "Registering events...");
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(hotspotManager, this);
    }

    @Override
    public void onDisable()
    {
        HandlerList.unregisterAll((Plugin) this);
        director.destroy();
        director = null;
        actorManager.destroy();
        actorManager = null;
        hotspotManager.destroy();
        hotspotManager = null;
        instance = null;
    }

    /**
     * <p>
     *     Load the configurations.
     * </p>
     * @return True if the configuration is loaded successfully, false otherwise.
     */
    private boolean loadConfig()
    {
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder().path(Path.of(getDataFolder().getPath(), "config.conf")).build();
        try
        {
            ConfigurationNode rootNode = loader.load();

            ConfigurationNode heatTypesNode = rootNode.node("heatTypes");
            HeatType.LoadConfig(heatTypesNode);

            ConfigurationNode hotspotManagerNode = rootNode.node("hotspotManager");
            hotspotManager = new HotspotManager().loadConfig(hotspotManagerNode);

            ConfigurationNode actorManagerNode = rootNode.node("actorManager");
            actorManager = new ActorManager().loadConfig(actorManagerNode);

            ConfigurationNode directorNode = rootNode.node("director");
            director = new Director().loadConfig(directorNode);
        }
        catch (ConfigurateException e)
        {
            Log(Level.SEVERE, "An error occurred while loading configuration at " + e.getMessage());
            return false;
        }
        catch (Exception e)
        {
            Log(Level.SEVERE, "An error occurred while loading configuration: " + e.getMessage());
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
