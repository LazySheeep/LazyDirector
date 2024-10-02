package io.lazysheeep.lazydirector;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
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
        sender.sendMessage("Usage: /lazydirector <start|stop|attachCamera|detachCamera>");
    }

    @Subcommand("start")
    @Description("Start LazyDirector")
    public void onStart(CommandSender sender)
    {
        sender.sendMessage("LazyDirector started.");
        // Start logic here
    }

    @Subcommand("stop")
    @Description("Stop LazyDirector")
    public void onStop(CommandSender sender)
    {
        sender.sendMessage("LazyDirector stopped.");
        // Stop logic here
    }

    @Subcommand("output")
    public class OutputCommand extends BaseCommand
    {
        @Subcommand("attach")
        @Description("Attach output to camera")
        public void onAttach(CommandSender sender, @Flags("other") Player output, String cameraman)
        {
            LazyDirector.GetPlugin().getDirector().getCameraman(cameraman).attachCamera(output);
            sender.sendMessage("Attached " + output.getName() + " to cameraman " + cameraman);
        }

        @Subcommand("detach")
        @Description("Detach output from any camera")
        public void onDetach(CommandSender sender, @Flags("other") Player output)
        {
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
            List<Hotspot> hotspots = LazyDirector.GetPlugin().getHotspotManager().getAllHotspotsSorted();
            sender.sendMessage("Total " + hotspots.size() + " Hotspots:\n" + hotspots);
        }
    }
}