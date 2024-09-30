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
    protected void destroy()
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
    public String toString()
    {
        return "StaticHotspot: " + location + ", heat: " + getTotalHeat();
    }
}
