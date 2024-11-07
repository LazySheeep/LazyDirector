package io.lazysheeep.lazydirector.events;

import io.lazysheeep.lazydirector.camera.Camera;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HotspotBeingFocusedEvent extends Event
{
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Hotspot hotspot;
    private final Camera camera;

    public Hotspot getHotspot()
    {
        return hotspot;
    }

    public Camera getCamera()
    {
        return camera;
    }

    public HotspotBeingFocusedEvent(Hotspot hotspot, Camera camera)
    {
        this.hotspot = hotspot;
        this.camera = camera;
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
