package io.lazysheeep.lazydirector;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

public final class LazyDirector extends JavaPlugin
{
    private static LazyDirector plugin;

    public static LazyDirector getPlugin()
    {
        return plugin;
    }

    private static Director director;

    public static Director getDirector()
    {
        return director;
    }

    @Override
    public void onEnable()
    {
        plugin = this;

        // register command
        PluginCommand command = this.getCommand("lazydirector");
        if(command != null)
        {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
        else
        {
            getLogger().log(Level.SEVERE, "Can't get command! There must be something wrong!");
        }

        director = new Director();
    }

    @Override
    public void onDisable()
    {
        plugin = null;

        director.Stop();
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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args)
    {
        if(args.length >= 1) switch (args[0])
        {
            case "loadConfig" ->
            {
                if(args.length >= 2)
                {
                    FileConfiguration fileConfig = loadCustomConfig(args[1] + ".yml");
                    if(fileConfig != null)
                    {
                        director.LoadConfig(fileConfig);
                        return true;
                    }
                    getLogger().log(Level.WARNING, "Config file " + args[1] + ".yml not found!");
                    return false;
                }
                return false;
            }
            case "start" ->
            {
                getLogger().log(Level.INFO, "Starting director...");
                director.Start();
                return true;
            }
            case "stop" ->
            {
                getLogger().log(Level.INFO, "Stopping director...");
                director.Stop();
                return true;
            }
            case "attachCamera" ->
            {
                if(args.length >= 2)
                {
                    Player player = Bukkit.getPlayerExact(args[1]);
                    if(player != null)
                    {
                        director.getCameraman().attachCamera(player);
                        return true;
                    }
                    getLogger().log(Level.WARNING, "Player " + args[1] + " not found!");
                    return false;
                }
                return false;
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
