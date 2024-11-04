package io.lazysheeep.lazydirector.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.actor.Actor;
import io.lazysheeep.lazydirector.director.Cameraman;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.List;

@CommandAlias("lazydirector")
@CommandPermission("op")
public class LazyDirectorCommand extends BaseCommand
{

    @Default
    @Description("LazyDirector main command")
    public void onDefault(CommandSender sender)
    {
        sender.sendMessage("usage: /lazydirector [activate|shutdown|actor|output|camera|hotspot]");
    }

    @Subcommand("activate")
    @Description("Activate LazyDirector")
    @CommandCompletion("@configNames")
    public void onActivate(CommandSender sender, String configName)
    {
        if(LazyDirector.GetPlugin().isActive())
        {
            sender.sendMessage("LazyDirector is already activated. Please shutdown first if you want to load another config.");
        }
        else
        {
            if(LazyDirector.GetPlugin().activate(configName))
            {
                sender.sendMessage("LazyDirector activated.");
            }
            else
            {
                sender.sendMessage("Failed to activate LazyDirector.");
            }
        }
    }

    @Subcommand("shutdown")
    @Description("Shutdown LazyDirector")
    public void onShutdown(CommandSender sender)
    {
        if(!LazyDirector.GetPlugin().isActive())
        {
            sender.sendMessage("LazyDirector is not activated.");
            return;
        }
        LazyDirector.GetPlugin().shutdown();
        sender.sendMessage("LazyDirector has been shutdown");
    }

    @Subcommand("actor")
    public class ActorCommand extends BaseCommand
    {
        @Subcommand("list")
        @Description("List all actors")
        public void onList(CommandSender sender)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage("LazyDirector is not activated.");
                return;
            }

            StringBuilder stringBuilder = new StringBuilder();
            List<Actor> actors = LazyDirector.GetPlugin().getActorManager().getAllActors();
            stringBuilder.append("Total ").append(actors.size()).append(" Actors:\n");
            actors.forEach(actor -> stringBuilder.append(actor).append(" "));
            sender.sendMessage(stringBuilder.toString());
        }
    }

    @Subcommand("output")
    public class OutputCommand extends BaseCommand
    {
        @Subcommand("attach")
        @Description("Attach output to camera")
        @CommandCompletion("@players @cameramen")
        public void onAttach(CommandSender sender, @Flags("other") Player output, String cameramanName)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage("LazyDirector is not activated.");
                return;
            }

            Cameraman cameraman = LazyDirector.GetPlugin().getDirector().getCameraman(cameramanName);
            if(cameraman != null)
            {
                cameraman.attachCamera(output);
                sender.sendMessage("Attached " + output.getName() + " to cameraman " + cameraman.getName());
            }
            else
            {
                sender.sendMessage("Cameraman " + cameramanName + " not found.");
            }
        }

        @Subcommand("detach")
        @Description("Detach output from any camera")
        public void onDetach(CommandSender sender, @Flags("other") Player output)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage("LazyDirector is not activated.");
                return;
            }

            LazyDirector.GetPlugin().getDirector().detachFromAnyCamera(output);
            sender.sendMessage("Detached " + output.getName() + " from any camera");
        }
    }

    @Subcommand("camera")
    public class CameraCommand extends BaseCommand
    {
        @Subcommand("list")
        @Description("List all cameras")
        public void onList(CommandSender sender)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage("LazyDirector is not activated.");
                return;
            }

            StringBuilder stringBuilder = new StringBuilder();
            List<Cameraman> cameramen = LazyDirector.GetPlugin().getDirector().getAllCameramen();
            stringBuilder.append("Total ").append(cameramen.size()).append(" Cameramen:");
            cameramen.forEach(cameraman -> stringBuilder.append("\n").append(cameraman));
            sender.sendMessage(stringBuilder.toString());
        }
    }

    @Subcommand("hotspot")
    public class HotspotCommand extends BaseCommand
    {
        @Subcommand("list")
        @Description("List all hotspots")
        public void onList(CommandSender sender)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage("LazyDirector is not activated.");
                return;
            }

            StringBuilder stringBuilder = new StringBuilder();
            List<Hotspot> hotspots = LazyDirector.GetPlugin().getHotspotManager().getAllHotspotsSorted();
            stringBuilder.append("Total ").append(hotspots.size()).append(" Hotspots:\n");
            hotspots.forEach(hotspot -> stringBuilder.append(hotspot).append("\n"));
            sender.sendMessage(stringBuilder.toString());
        }
    }

    @Subcommand("heat")
    @Description("Increase the heat of player")
    @CommandCompletion("@players @heatTypes")
    public void onHeat(CommandSender sender, Player[] playerSelector, String heatType)
    {
        if(!LazyDirector.GetPlugin().isActive())
        {
            sender.sendMessage("LazyDirector is not activated.");
            return;
        }

        if(playerSelector.length == 0)
        {
            sender.sendMessage("No player found.");
            return;
        }

        for(Player player : playerSelector)
        {
            switch (LazyDirector.GetPlugin().heat(player, heatType))
            {
                case 0:
                    sender.sendMessage("Heated " + player.getName());
                    break;
                case 1:
                    sender.sendMessage("Heat type " + heatType + " not found.");
                    break;
                case 2:
                    sender.sendMessage("Player " + player.getName() + " is not an actor.");
                    break;
            }
        }
    }

    @Subcommand("chatRepeater")
    @Description("Manage chat repeater")
    public class ChatRepeaterCommand extends BaseCommand
    {
        @Subcommand("enable")
        @Description("Enable chat repeater")
        public void onEnable(CommandSender sender)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage("LazyDirector is not activated.");
                return;
            }

            Bukkit.getPluginManager().registerEvents(LazyDirector.GetPlugin().getChatRepeater(), LazyDirector.GetPlugin());
            sender.sendMessage("Chat repeater enabled.");
        }

        @Subcommand("disable")
        @Description("Disable chat repeater")
        public void onDisable(CommandSender sender)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage("LazyDirector is not activated.");
                return;
            }

            HandlerList.unregisterAll(LazyDirector.GetPlugin().getChatRepeater());
            sender.sendMessage("Chat repeater disabled.");
        }
    }
}