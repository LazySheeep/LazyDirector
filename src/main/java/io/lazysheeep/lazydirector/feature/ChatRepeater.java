package io.lazysheeep.lazydirector.feature;

import io.lazysheeep.lazuliui.LazuliUI;
import io.lazysheeep.lazuliui.Message;
import io.lazysheeep.lazydirector.LazyDirector;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.BroadcastMessageEvent;

public class ChatRepeater implements Listener
{
    private void repeat(Message message)
    {
        LazuliUI.setActionbarInfixWidth(32);
        LazyDirector.GetPlugin()
                    .getDirector()
                    .getAllCameramen()
                    .forEach(cameraman -> cameraman.getOutputs()
                                                   .forEach(output -> LazuliUI.sendMessage(output, message)));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncChat(AsyncChatEvent event)
    {
        TextComponent message = Component.text("<")
                                         .append(Component.text(event.getPlayer().getName()))
                                         .append(Component.text("> "))
                                         .append(event.message());
        repeat(new Message(Message.Type.ACTIONBAR_INFIX, message, Message.LoadMode.REPLACE, 180));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBroadcastMessage(BroadcastMessageEvent event)
    {
        repeat(new Message(Message.Type.ACTIONBAR_INFIX, (TextComponent) event.message(), Message.LoadMode.REPLACE, 180));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        TextComponent message = Component.text(event.getPlayer().getName(), NamedTextColor.YELLOW)
                                         .append(Component.text(" joined. Current online: ", NamedTextColor.YELLOW))
                                         .append(Component.text(event.getPlayer()
                                                                     .getServer()
                                                                     .getOnlinePlayers()
                                                                     .size(), NamedTextColor.YELLOW));
        repeat(new Message(Message.Type.ACTIONBAR_INFIX, message, Message.LoadMode.REPLACE, 180));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        TextComponent message = Component.text(event.getPlayer().getName(), NamedTextColor.YELLOW)
                                         .append(Component.text(" left. Current online: ", NamedTextColor.YELLOW))
                                         .append(Component.text(Bukkit.getServer().getOnlinePlayers().size() - 1, NamedTextColor.YELLOW));
        repeat(new Message(Message.Type.ACTIONBAR_INFIX, message, Message.LoadMode.REPLACE, 180));
    }
}
