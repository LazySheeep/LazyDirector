package io.lazysheeep.lazydirector.hotspot;

import org.bukkit.Location;

public class StaticHotspot extends Hotspot
{
    private Location location;

    StaticHotspot(Location location, float heat)
    {
        this.location = location;
        increase("static", heat);
    }

    @Override
    protected void additionalDestroy()
    {
        location = null;
    }

    @Override
    public boolean isValid()
    {
        return location != null;
    }

    @Override
    protected void additionalUpdate()
    {
        // do nothing
    }

    @Override
    public Location getLocation()
    {
        return location.clone();
    }

    @Override
    protected String additionalToString()
    {
        return "location=" + location;
    }
}
