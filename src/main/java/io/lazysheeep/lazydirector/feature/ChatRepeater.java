package io.lazysheeep.lazydirector.feature;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.lazysheeep.lazydirector.LazyDirector;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatRepeater implements Listener
{
    private final float messageTime = 9.0f;
    private final float idleTimeThreshold = 15.0f;

    private float idleTime = 0.0f;
    private float currentMessageTime = 0.0f;
    private Component currentMessage = null;

    private void repeat(Component message)
    {
        currentMessage = message;
    }

    private void sendActionBar(Component message)
    {
        LazyDirector.GetPlugin()
                    .getDirector()
                    .getAllCameramen()
                    .forEach(cameraman -> cameraman.getOutputs().forEach(output -> output.sendActionBar(message)));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerTickStart(ServerTickStartEvent event)
    {
        if (currentMessage != null)
        {
            sendActionBar(currentMessage);
            currentMessageTime += LazyDirector.GetServerTickDeltaTime();
            idleTime = 0.0f;
            if (currentMessageTime >= messageTime - 2.0f)
            {
                currentMessage = null;
                currentMessageTime = 0.0f;
            }
        }
        else
        {
            idleTime += LazyDirector.GetServerTickDeltaTime();
        }

        if (idleTime >= idleTimeThreshold)
        {
            idleTime = 0.0f;
            repeat(Component.text("Current Online: ", NamedTextColor.AQUA)
                            .append(Component.text(LazyDirector.GetPlugin()
                                                               .getActorManager()
                                                               .getAllActors()
                                                               .size(), NamedTextColor.GREEN)));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncChat(AsyncChatEvent event)
    {
        TextComponent message = Component.text("<", NamedTextColor.GRAY)
                                         .append(Component.text(event.getPlayer().getName(), NamedTextColor.GREEN))
                                         .append(Component.text("> ", NamedTextColor.GRAY))
                                         .append(event.message().color(NamedTextColor.WHITE));
        repeat(message);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        TextComponent message = Component.text(event.getPlayer().getName(), NamedTextColor.GREEN)
                                         .append(Component.text(" joined the game.", NamedTextColor.YELLOW));
        repeat(message);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        TextComponent message = Component.text(event.getPlayer().getName(), NamedTextColor.GREEN)
                                         .append(Component.text(" left the game.", NamedTextColor.YELLOW));
        repeat(message);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event)
    {
        var display = event.getAdvancement().getDisplay();
        if(display != null && display.doesAnnounceToChat())
        {
            TextComponent message = Component.text(event.getPlayer().getName(), NamedTextColor.GREEN)
                                             .append(Component.text(" has made the advancement ", NamedTextColor.AQUA))
                                             .append(event.getAdvancement().displayName());
            repeat(message);
        }
    }
}
