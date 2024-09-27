package io.lazysheeep.lazydirector;

import io.lazysheeep.lazydirector.cameramovement.CMPOverlook;
import io.lazysheeep.lazydirector.cameramovement.CameraMovementPattern;
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
    private CameraMovementPattern cameraMovementPattern;

    public Cameraman(String name)
    {
        this.name = name;
    }

    private void createCamera(Location location)
    {
        camera = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        camera.customName(Component.text(this.name + "'s Camera"));
        camera.setMarker(true);
        camera.setSmall(true);
        camera.setInvisible(true);

        cameraMovementPattern = new CMPOverlook();
    }

    public void attachCamera(Player player)
    {
        outputs.add(player);
    }

    public void detachCamera(Player player)
    {
        outputs.remove(player);
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

            cameraMovementPattern.updateCameraLocation(camera, focus.getLocation());

            for(Player player : outputs)
            {
                player.setGameMode(GameMode.SPECTATOR);
                player.setSpectatorTarget(camera);
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
