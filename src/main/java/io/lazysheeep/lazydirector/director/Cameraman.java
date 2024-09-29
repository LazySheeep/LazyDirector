package io.lazysheeep.lazydirector.director;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.camerashottype.CameraShotType;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.List;

public class Cameraman
{
    public final String name;
    public List<Player> outputs = new LinkedList<>();
    public ArmorStand camera;
    private Hotspot focus;
    private CameraShotType cameraShotType;

    public Cameraman(String name, CameraShotType cameraShotType)
    {
        this.name = name;
        this.cameraShotType = cameraShotType;
    }

    private void createCamera(Location location)
    {
        camera = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        camera.customName(Component.text(this.name + "'s Camera"));
        camera.setMarker(true);
        camera.setSmall(true);
        camera.setInvisible(true);

        outputs.forEach(player -> player.setSpectatorTarget(camera));
    }

    public void attachCamera(Player player)
    {
        outputs.add(player);
        player.setGameMode(GameMode.SPECTATOR);
        player.setSpectatorTarget(camera);
    }

    public void detachCamera(Player player)
    {
        outputs.remove(player);
        player.setSpectatorTarget(null);
    }

    public void setFocus(Hotspot hotspot)
    {
        focus = hotspot;
    }

    public void update()
    {
        if(focus != null)
        {
            if(camera == null)
            {
                createCamera(focus.getLocation());
            }

            cameraShotType.updateCameraLocation(camera, focus.getLocation());

            for(Player player : outputs)
            {
                if(player.getSpectatorTarget() == null)
                {
                    LazyDirector.getDirector().switchCameraman(player, this);
                }
            }
        }
    }

    public static void LookAt(Location origin, Location target)
    {
        double dx = target.getX() - origin.getX();
        double dy = target.getY() - origin.getY();
        double dz = target.getZ() - origin.getZ();
        origin.setDirection(new Vector(dx, dy, dz));
    }
}
