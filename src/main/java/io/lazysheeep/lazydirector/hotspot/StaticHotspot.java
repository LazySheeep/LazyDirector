package io.lazysheeep.lazydirector.hotspot;

import io.lazysheeep.lazydirector.LazyDirector;
import org.bukkit.Location;
import org.bukkit.World;
import org.spongepowered.configurate.ConfigurationNode;

public class StaticHotspot extends Hotspot
{
    private Location location;

    StaticHotspot(Location location, float heat)
    {
        this.location = location;
        increase("static", heat);
    }

    StaticHotspot(ConfigurationNode configNode)
    {
        ConfigurationNode locationNode = configNode.node("location");
        World world = LazyDirector.GetPlugin().getServer().getWorld(locationNode.node("world").getString("no_value"));
        double x = locationNode.node("x").getDouble(0.0);
        double y = locationNode.node("y").getDouble(0.0);
        double z = locationNode.node("z").getDouble(0.0);
        float pitch = locationNode.node("pitch").getFloat(0.0f);
        float yaw = locationNode.node("yaw").getFloat(0.0f);
        this.location = new Location(world, x, y, z, yaw, pitch);
        float heat = configNode.node("heat").getFloat(0.0f);
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
