package io.lazysheeep.lazydirector;

import io.lazysheeep.lazydirector.hotspot.Hotspot;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class Cameraman
{
    public final String name;
    public List<Player> cameras = new LinkedList<>();
    private Location location;
    private Hotspot focus;

    public Cameraman(String name)
    {
        this.name = name;
    }

    public void attachCamera(Player player)
    {
        cameras.add(player);
    }

    public void detachCamera(Player player)
    {
        cameras.remove(player);
    }

    public void setFocus(Hotspot hotspot)
    {
        focus = hotspot;
        location = focus.getLocation();
    }

    public void update()
    {
        if(focus != null)
        {
            location = focus.getLocation();
            location.add(5.0f, 5.0f, 5.0f);
            LookAt(location, focus.getLocation());
        }

        for(Player camera : cameras)
        {
            camera.setGameMode(GameMode.SPECTATOR);
            camera.teleport(location);
        }
    }

    public static void LookAt(Location origin, Location target)
    {
        double dx = target.getX() - origin.getX();
        double dy = target.getY() - origin.getY();
        double dz = target.getZ() - origin.getZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double pitch = Math.asin(dy / distance);
        double yaw = Math.atan2(dz, dx);
        origin.setYaw((float) Math.toDegrees(yaw) - 90.0f);
        origin.setPitch((float) -Math.toDegrees(pitch));
    }
}
