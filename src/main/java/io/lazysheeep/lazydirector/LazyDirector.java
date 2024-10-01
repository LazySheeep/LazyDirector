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
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.File;
import java.nio.file.Path;
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
        getLogger().log(Level.INFO, "Registering commands");
        PluginCommand command = this.getCommand("lazydirector");
        if (command != null)
        {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
        else
        {
            getLogger().log(Level.SEVERE, "Failed to register command");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

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
        plugin = null;
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
