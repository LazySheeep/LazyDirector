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

public final class LazyDirector extends JavaPlugin implements Listener
{
    private static LazyDirector instance;

    public static LazyDirector GetPlugin()
    {
        return instance;
    }

    private Director director;

    public Director getDirector()
    {
        return director;
    }

    private ActorManager actorManager;

    public ActorManager getActorManager()
    {
        return actorManager;
    }

    private HotspotManager hotspotManager;

    public HotspotManager getHotspotManager()
    {
        return hotspotManager;
    }

    @Override
    public void onEnable()
    {
        instance = this;

        // register command
        getLogger().log(Level.INFO, "Registering commands");
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new LazyDirectorCommand());

        // load config
        getLogger().log(Level.INFO, "Loading configuration...");
        if(!loadConfig())
        {
            getLogger().log(Level.SEVERE, "Failed to load configuration!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // register events
        getLogger().log(Level.INFO, "Registering events...");
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(hotspotManager, this);
    }

    @Override
    public void onDisable()
    {
        HandlerList.unregisterAll((Plugin) this);
        instance = null;
        director = null;
    }

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
            getLogger().log(Level.SEVERE, "An error occurred while loading configuration at " + e.getMessage());
            return false;
        }
        catch (Exception e)
        {
            getLogger().log(Level.SEVERE, "An error occurred while loading configuration: " + e.getMessage());
            return false;
        }
        return true;
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerTickStart(ServerTickStartEvent event)
    {
        actorManager.update();
        hotspotManager.update();
        director.update();
    }
}
