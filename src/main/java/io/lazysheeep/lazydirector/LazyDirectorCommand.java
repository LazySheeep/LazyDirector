package io.lazysheeep.lazydirector;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import io.lazysheeep.lazydirector.actor.Actor;
import io.lazysheeep.lazydirector.director.Cameraman;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

            List<Actor> actors = LazyDirector.GetPlugin().getActorManager().getAllActors();
            sender.sendMessage("Total " + actors.size() + " Actors:\n" + actors);
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

            List<Cameraman> cameras = LazyDirector.GetPlugin().getDirector().getAllCameramen();
            sender.sendMessage("Total " + cameras.size() + " Cameras:\n" + cameras);
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

            List<Hotspot> hotspots = LazyDirector.GetPlugin().getHotspotManager().getAllHotspotsSorted();
            sender.sendMessage("Total " + hotspots.size() + " Hotspots:\n" + hotspots);
        }
    }
}