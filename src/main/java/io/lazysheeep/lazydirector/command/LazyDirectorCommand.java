package io.lazysheeep.lazydirector.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.actor.Actor;
import io.lazysheeep.lazydirector.camera.Camera;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.localization.LocalizationManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.Locale;

@CommandAlias("lazydirector")
public class LazyDirectorCommand extends BaseCommand
{

    @Default
    @Description("LazyDirector main command")
    @CommandPermission("op")
    public void onDefault(CommandSender sender)
    {
        sender.sendMessage("usage: /lazydirector [activate|shutdown|actor|output|camera|hotspot]");
    }

    @Subcommand("activate")
    @Description("Activate LazyDirector")
    @CommandPermission("op")
    @CommandCompletion("@configNames")
    public void onActivate(CommandSender sender, String configName)
    {
        if(LazyDirector.GetPlugin().isActive())
        {
            sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_activate_already_activated", Locale.getDefault()), NamedTextColor.YELLOW));
        }
        else
        {
            sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_activate_start", Locale.getDefault()) + configName, NamedTextColor.AQUA));
            if(LazyDirector.GetPlugin().activate(configName))
            {
                sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_activate_success", Locale.getDefault()), NamedTextColor.GREEN));
            }
            else
            {
                sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_activate_fail", Locale.getDefault()), NamedTextColor.RED));
            }
        }
    }

    @Subcommand("shutdown")
    @Description("Shutdown LazyDirector")
    @CommandPermission("op")
    public void onShutdown(CommandSender sender)
    {
        if(!LazyDirector.GetPlugin().isActive())
        {
            sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()), NamedTextColor.YELLOW));
            return;
        }
        sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_shutdown_start", Locale.getDefault()), NamedTextColor.AQUA));
        LazyDirector.GetPlugin().shutdown();
        sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_shutdown_success", Locale.getDefault()), NamedTextColor.GREEN));
    }

    @Subcommand("reload")
    @Description("Reload LazyDirector")
    @CommandPermission("op")
    public void onReload(CommandSender sender)
    {
        if(!LazyDirector.GetPlugin().isActive())
        {
            sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()), NamedTextColor.YELLOW));
            return;
        }
        sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_reload_start", Locale.getDefault()) + LazyDirector.GetPlugin().getRecentConfigName(), NamedTextColor.AQUA));
        LazyDirector.GetPlugin().shutdown();
        if(LazyDirector.GetPlugin().activate(LazyDirector.GetPlugin().getRecentConfigName()))
        {
            sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_reload_success", Locale.getDefault()), NamedTextColor.GREEN));
        }
        else
        {
            sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_reload_fail", Locale.getDefault()), NamedTextColor.RED));
        }

    }

    @Subcommand("actor")
    @CommandPermission("op")
    public class ActorCommand extends BaseCommand
    {
        @Subcommand("list")
        @Description("List all actors")
        public void onList(CommandSender sender)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()), NamedTextColor.YELLOW));
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
    @CommandPermission("op")
    public class OutputCommand extends BaseCommand
    {
        @Subcommand("attach")
        @Description("Attach output to camera")
        @CommandCompletion("@players @cameraNames")
        public void onAttach(CommandSender sender, @Flags("other") Player output, String cameraName)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()), NamedTextColor.YELLOW));
                return;
            }

            Camera camera = LazyDirector.GetPlugin().getCameraManager().getCamera(cameraName);
            if(camera != null)
            {
                camera.attachOutput(output);
                sender.sendMessage(Component.text("Attached " + output.getName() + " to camera " + camera.getName(), NamedTextColor.AQUA));
            }
            else
            {
                sender.sendMessage(Component.text("Camera " + cameraName + " not found.", NamedTextColor.RED));
            }
        }

        @Subcommand("detach")
        @Description("Detach output from any camera")
        public void onDetach(CommandSender sender, @Flags("other") Player output)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()), NamedTextColor.YELLOW));
                return;
            }

            LazyDirector.GetPlugin().getCameraManager().detachFromAnyCamera(output);
            sender.sendMessage(Component.text("Detached " + output.getName() + " from any camera", NamedTextColor.AQUA));
        }
    }

    @Subcommand("camera")
    @CommandPermission("op")
    public class CameraCommand extends BaseCommand
    {
        @Subcommand("list")
        @Description("List all cameras")
        public void onList(CommandSender sender)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()), NamedTextColor.YELLOW));
                return;
            }

            StringBuilder stringBuilder = new StringBuilder();
            List<Camera> cameras = LazyDirector.GetPlugin().getCameraManager().getAllCameras();
            stringBuilder.append("Total ").append(cameras.size()).append(" Cameras:");
            cameras.forEach(camera -> stringBuilder.append("\n").append(camera));
            sender.sendMessage(stringBuilder.toString());
        }

        @Subcommand("info")
        @Description("Check the info of a specific camera")
        @CommandCompletion("@cameraNames")
        public void onInfo(CommandSender sender, String cameraName)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()), NamedTextColor.YELLOW));
                return;
            }

            Camera camera = LazyDirector.GetPlugin().getCameraManager().getCamera(cameraName);
            if(camera != null)
            {
                StringBuilder stringBuilder = new StringBuilder();
                List<Hotspot> hotspots = camera.getCandidateFocuses();
                stringBuilder.append("Camera ").append(camera).append("\n").append(hotspots.size()).append(" candidate hotspots:");
                hotspots.forEach(hotspot -> stringBuilder.append("\n").append(hotspot));
                sender.sendMessage(stringBuilder.toString());
            }
            else
            {
                sender.sendMessage(Component.text("Camera " + cameraName + " not found.", NamedTextColor.RED));
            }
        }

        @Subcommand("lock")
        @Description("Lock a camera to a specific actor")
        @CommandCompletion("@cameraNames @actorNames")
        public void onLock(CommandSender sender, String cameraName, String actorName)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()), NamedTextColor.YELLOW));
                return;
            }

            Camera camera = LazyDirector.GetPlugin().getCameraManager().getCamera(cameraName);
            if(camera != null)
            {
                Player player = Bukkit.getPlayer(actorName);
                if(player == null)
                {
                    sender.sendMessage(Component.text("Actor " + actorName + " not found.", NamedTextColor.RED));
                    return;
                }
                Actor actor = LazyDirector.GetPlugin().getActorManager().getActor(player);
                if(actor == null)
                {
                    sender.sendMessage(Component.text("Actor " + actorName + " not found.", NamedTextColor.RED));
                    return;
                }
                Hotspot hotspot = actor.getActorHotspot();
                camera.lockOnFocus(hotspot);
                sender.sendMessage(Component.text("Locked camera " + camera.getName() + " to actor " + actor.getPlayer().getName(), NamedTextColor.AQUA));
            }
            else
            {
                sender.sendMessage(Component.text("Camera " + cameraName + " not found.", NamedTextColor.RED));
            }
        }

        @Subcommand("unlock")
        @Description("Unlock a camera from its locked actor")
        @CommandCompletion("@cameraNames")
        public void onUnlock(CommandSender sender, String cameraName)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()), NamedTextColor.YELLOW));
                return;
            }

            Camera camera = LazyDirector.GetPlugin().getCameraManager().getCamera(cameraName);
            if(camera != null)
            {
                camera.lockOnFocus(null);
                sender.sendMessage(Component.text("Unlocked camera " + camera.getName(), NamedTextColor.AQUA));
            }
            else
            {
                sender.sendMessage(Component.text("Camera " + cameraName + " not found.", NamedTextColor.RED));
            }
        }
    }

    @Subcommand("hotspot")
    @CommandPermission("op")
    public class HotspotCommand extends BaseCommand
    {
        @Subcommand("list")
        @Description("List all hotspots")
        public void onList(CommandSender sender)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()), NamedTextColor.YELLOW));
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
    @CommandPermission("op")
    @CommandCompletion("@players @heatTypes")
    public void onHeat(CommandSender sender, Player[] playerSelector, String heatType)
    {
        if(!LazyDirector.GetPlugin().isActive())
        {
            sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()), NamedTextColor.YELLOW));
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
                    sender.sendMessage("Heated " + player.getName() + "(" + heatType + " is " + LazyDirector.GetPlugin().getHeat(player, heatType));
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

    @Subcommand("permission")
    @Description("Manage permission")
    @CommandPermission("")
    public class PermissionCommand extends BaseCommand
    {
        @Subcommand("grant")
        @Description("Grant your permission to LazyDirector")
        public void onGrant(Player senderPlayer)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                senderPlayer.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()), NamedTextColor.YELLOW));
                return;
            }
            if(LazyDirector.GetPlugin().getActorManager().grantPermission(senderPlayer))
            {
                senderPlayer.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_permission_granted", Locale.getDefault()), NamedTextColor.AQUA));
            }
            else
            {
                senderPlayer.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_permission_not_enabled", Locale.getDefault()), NamedTextColor.YELLOW));
            }
        }

        @Subcommand("revoke")
        @Description("Revoke your permission to LazyDirector")
        public void onRevoke(Player senderPlayer)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                senderPlayer.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()), NamedTextColor.YELLOW));
                return;
            }
            if(LazyDirector.GetPlugin().getActorManager().revokePermission(senderPlayer))
            {
                senderPlayer.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_permission_revoked", Locale.getDefault()), NamedTextColor.AQUA));
            }
            else
            {
                senderPlayer.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_permission_not_enabled", Locale.getDefault()), NamedTextColor.YELLOW));
            }
        }
    }

    @Subcommand("chatRepeater")
    @Description("Manage chat repeater")
    @CommandPermission("op")
    public class ChatRepeaterCommand extends BaseCommand
    {
        @Subcommand("enable")
        @Description("Enable chat repeater")
        public void onEnable(CommandSender sender)
        {
            if(!LazyDirector.GetPlugin().isActive())
            {
                sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()), NamedTextColor.YELLOW));
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
                sender.sendMessage(Component.text(LocalizationManager.GetLocalizedString("command_not_activated", Locale.getDefault()), NamedTextColor.YELLOW));
                return;
            }

            HandlerList.unregisterAll(LazyDirector.GetPlugin().getChatRepeater());
            sender.sendMessage(LocalizationManager.GetLocalizedString("command_chat_repeater_disabled", Locale.getDefault()));
        }
    }
}