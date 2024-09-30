package io.lazysheeep.lazydirector;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.lazysheeep.lazydirector.actor.ActorManager;
import io.lazysheeep.lazydirector.director.Director;
import io.lazysheeep.lazydirector.heat.HeatType;
import io.lazysheeep.lazydirector.hotspot.HotspotManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

public final class LazyDirector extends JavaPlugin implements Listener
{
    private static LazyDirector plugin;

    public static LazyDirector GetPlugin()
    {
        return plugin;
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
        plugin = this;

        // register command
        PluginCommand command = this.getCommand("lazydirector");
        if (command != null)
        {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
        else
        {
            getLogger().log(Level.SEVERE, "Cannot get command! Why is that happening?");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // load config
        saveDefaultConfig();
        FileConfiguration fileConfig = getConfig();
        // load heat types
        ConfigurationSection heatTypesConfigSection = fileConfig.getConfigurationSection("heatTypes");
        if (heatTypesConfigSection != null)
        {
            HeatType.RegisterHeatTypesFromConfig(heatTypesConfigSection);
        }
        else
        {
            getLogger().log(Level.SEVERE, "Syntax error in configuration file: Section \"heatTypes\" not found!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        // load hotspot manager
        ConfigurationSection hotspotManagerConfigSection = fileConfig.getConfigurationSection("hotspotManager");
        if (hotspotManagerConfigSection != null)
        {
            hotspotManager = new HotspotManager(hotspotManagerConfigSection);
        }
        else
        {
            getLogger().log(Level.SEVERE, "Syntax error in configuration file: Section \"hotspotManager\" not found!");
        }
        // load actor manager
        ConfigurationSection actorManagerConfigSection = fileConfig.getConfigurationSection("actorManager");
        if (actorManagerConfigSection != null)
        {
            actorManager = new ActorManager(actorManagerConfigSection);
        }
        else
        {
            getLogger().log(Level.SEVERE, "Syntax error in configuration file: Section \"actorManager\" not found!");
        }
        // load director
        ConfigurationSection directorConfigSection = fileConfig.getConfigurationSection("director");
        if (directorConfigSection != null)
        {
            director = new Director(directorConfigSection);
        }
        else
        {
            getLogger().log(Level.SEVERE, "Syntax error in configuration file: Section \"director\" not found!");
        }

        // register events
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(hotspotManager, this);
    }

    @Override
    public void onDisable()
    {
        HandlerList.unregisterAll((Plugin) this);
        plugin = null;
        director = null;
    }

    private FileConfiguration loadCustomConfig(String fileName)
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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args)
    {

        if (command.getName().equalsIgnoreCase("lazydirector"))
        {
            if (args.length == 0)
            {
                sender.sendMessage("Usage: /lazydirector <start|stop|attachCamera|detachCamera>");
                return true;
            }

            switch (args[0].toLowerCase())
            {
                case "start":
                    // Start logic here
                    sender.sendMessage("LazyDirector started.");
                    return true;

                case "stop":
                    // Stop logic here
                    sender.sendMessage("LazyDirector stopped.");
                    return true;

                case "attachcamera":
                    if (args.length >= 3)
                    {
                        String cameraName = args[1];
                        Player player = getServer().getPlayer(args[2]);
                        if (player != null)
                        {
                            director.getCameraman(cameraName).attachCamera(player);
                            sender.sendMessage("Camera " + cameraName + " attached to " + player.getName());
                            return true;
                        }
                        else
                        {
                            sender.sendMessage("Player not found");
                            return true;
                        }
                    }
                    else
                    {
                        sender.sendMessage("Usage: /lazydirector attachCamera <camera_name> <player_name>");
                        return true;
                    }

                case "detachcamera":
                    if (args.length >= 2)
                    {
                        Player player = getServer().getPlayer(args[1]);
                        if (player != null)
                        {
                            // Detach camera logic here
                            sender.sendMessage("Camera detached from " + player.getName());
                            return true;
                        }
                        else
                        {
                            sender.sendMessage("Player not found");
                            return true;
                        }
                    }
                    else
                    {
                        sender.sendMessage("Usage: /lazydirector detachCamera <player_name>");
                        return true;
                    }

                default:
                    sender.sendMessage("Unknown subcommand. Usage: /lazydirector <start|stop|attachCamera|detachCamera>");
                    return true;
            }
        }
        return false;
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args)
    {
        return null;
    }
}
