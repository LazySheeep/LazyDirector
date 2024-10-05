package io.lazysheeep.lazydirector.events;

import io.lazysheeep.lazydirector.director.Cameraman;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HotspotBeingFocusedEvent extends Event
{
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Hotspot hotspot;
    private final Cameraman cameraman;

    public Hotspot getHotspot()
    {
        return hotspot;
    }

    public Cameraman getCameraman()
    {
        return cameraman;
    }

    public HotspotBeingFocusedEvent(Hotspot hotspot, Cameraman cameraman)
    {
        this.hotspot = hotspot;
        this.cameraman = cameraman;
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList()
    {
        return HANDLER_LIST;
    }
}
