package io.lazysheeep.lazydirector.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.actor.Actor;
import io.lazysheeep.lazydirector.camera.Camera;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.localization.LocalizationManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.Locale;

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
            sender.sendMessage(LocalizationManager.GetLocalizedString("command_activate_already_activated", Locale.getDefault()));
        }
        else
        {
            sender.sendMessage(LocalizationManager.GetLocalizedString("command_activate_start", Locale.getDefault()) + configName);
            if(LazyDirector.GetPlugin().activate(configName))
            {
                sender.sendMessage(LocalizationManager.GetLocalizedString("command_activate_success", Locale.getDefault()));
            }
            else
            {
                sender.sendMessage(LocalizationManager.GetLocalizedString("command_activate_fail", Locale.getDefault()));
            }
        }
    }

    @Subcommand("shutdown")
    @Description("Shutdown LazyDirector")
    public void onShutdown(CommandSender sender)
    {
        if(!LazyDirector.GetPlugin().isActive())
        {
            sender.sendMessage(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()));
            return;
        }
        sender.sendMessage(LocalizationManager.GetLocalizedString("command_shutdown_start", Locale.getDefault()));
        LazyDirector.GetPlugin().shutdown();
        sender.sendMessage(LocalizationManager.GetLocalizedString("command_shutdown_success", Locale.getDefault()));
    }

    @Subcommand("reload")
    @Description("Reload LazyDirector")
    public void onReload(CommandSender sender)
    {
        if(!LazyDirector.GetPlugin().isActive())
        {
            sender.sendMessage(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()));
            return;
        }
        sender.sendMessage(LocalizationManager.GetLocalizedString("command_reload_start", Locale.getDefault()) + LazyDirector.GetPlugin().getRecentConfigName());
        LazyDirector.GetPlugin().shutdown();
        if(LazyDirector.GetPlugin().activate(LazyDirector.GetPlugin().getRecentConfigName()))
        {
            sender.sendMessage(LocalizationManager.GetLocalizedString("command_reload_success", Locale.getDefault()));
        }
        else
        {
            sender.sendMessage(LocalizationManager.GetLocalizedString("command_reload_fail", Locale.getDefault()));
        }

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
                sender.sendMessage(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()));
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
        @CommandCompletion("@players @cameraNames")
        public void onAttach(CommandSender sender, @Flags("other") Player output, String cameraName)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()));
                return;
            }

            Camera camera = LazyDirector.GetPlugin().getCameraManager().getCamera(cameraName);
            if(camera != null)
            {
                camera.attachOutput(output);
                sender.sendMessage("Attached " + output.getName() + " to camera " + camera.getName());
            }
            else
            {
                sender.sendMessage("Camera " + cameraName + " not found.");
            }
        }

        @Subcommand("detach")
        @Description("Detach output from any camera")
        public void onDetach(CommandSender sender, @Flags("other") Player output)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()));
                return;
            }

            LazyDirector.GetPlugin().getCameraManager().detachFromAnyCamera(output);
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
                sender.sendMessage(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()));
                return;
            }

            StringBuilder stringBuilder = new StringBuilder();
            List<Camera> cameras = LazyDirector.GetPlugin().getCameraManager().getAllCamera();
            stringBuilder.append("Total ").append(cameras.size()).append(" Cameras:");
            cameras.forEach(camera -> stringBuilder.append("\n").append(camera));
            sender.sendMessage(stringBuilder.toString());
        }

        @Subcommand("candidate")
        @Description("List current candidate hotspots of a specific camera")
        @CommandCompletion("@cameraNames")
        public void onCandidate(CommandSender sender, String cameraName)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()));
                return;
            }

            Camera camera = LazyDirector.GetPlugin().getCameraManager().getCamera(cameraName);
            if(camera != null)
            {
                StringBuilder stringBuilder = new StringBuilder();
                List<Hotspot> hotspots = camera.getCandidateFocuses();
                stringBuilder.append("Total ").append(hotspots.size()).append(" candidate hotspots for camera ").append(camera.getName()).append(":");
                hotspots.forEach(hotspot -> stringBuilder.append("\n").append(hotspot));
                sender.sendMessage(stringBuilder.toString());
            }
            else
            {
                sender.sendMessage("Camera " + cameraName + " not found.");
            }
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
                sender.sendMessage(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()));
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
            sender.sendMessage(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()));
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
                sender.sendMessage(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()));
                return;
            }

            Bukkit.getPluginManager().registerEvents(LazyDirector.GetPlugin().getChatRepeater(), LazyDirector.GetPlugin());
            sender.sendMessage(LocalizationManager.GetLocalizedString("command_chat_repeater_enabled", Locale.getDefault()));
        }

        @Subcommand("disable")
        @Description("Disable chat repeater")
        public void onDisable(CommandSender sender)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()));
                return;
            }

            HandlerList.unregisterAll(LazyDirector.GetPlugin().getChatRepeater());
            sender.sendMessage(LocalizationManager.GetLocalizedString("command_chat_repeater_disabled", Locale.getDefault()));
        }
    }
}