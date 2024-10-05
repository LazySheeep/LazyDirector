package io.lazysheeep.lazydirector.director;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.camerashottype.CameraShotType;
import io.lazysheeep.lazydirector.events.HotspotBeingFocusedEvent;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.RandomUtils;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;
import java.util.logging.Level;

/**
 * <p>
 *     Cameraman class represents a cameraman who can control a camera to shoot a hotspot.
 * </p>
 * <p>
 *     All cameramen are managed by {@link Director}.
 *     <br>
 *     The creation of cameraman only happens when {@link Director} load the configuration.
 * </p>
 */
public class Cameraman
{
    private final String name;

    private final float minFocusSwitchTime;
    private final float maxFocusSwitchTime;

    private final int candidateMaxCount;
    private final float candidateHottestRank;
    private final float candidateHottestWeight;
    private final float candidateColdestRank;
    private final float candidateColdestWeight;

    private final Map<Class<?>, List<Pair<CameraShotType, Float>>> candidateHotspotTypes = new HashMap<>();

    public @NotNull String getName()
    {
        return name;
    }

    /**
     * <p>
     *     Construct a cameraman from a configuration node.
     * </p>
     * <p>
     *     Cameraman's configuration is immutable, you'll have to create a new one if you want to load a new configuration.
     * </p>
     * <p>
     *     Should only be called by {@link Director}.
     * </p>
     * @param configNode The configuration node of the cameraman
     * @throws ConfigurateException
     */
    Cameraman(@NotNull ConfigurationNode configNode) throws ConfigurateException
    {
        this.name = configNode.node("name").getString();
        this.minFocusSwitchTime = configNode.node("minFocusSwitchTime").getFloat();
        this.maxFocusSwitchTime = configNode.node("maxFocusSwitchTime").getFloat();

        ConfigurationNode candidateFocusesNode = configNode.node("candidateFocuses");
        this.candidateMaxCount = candidateFocusesNode.node("maxCount").getInt();
        this.candidateHottestRank = candidateFocusesNode.node("hottest").getFloat();
        this.candidateHottestWeight = candidateFocusesNode.node("hottestWeight").getFloat();
        this.candidateColdestRank = candidateFocusesNode.node("coldest").getFloat();
        this.candidateColdestWeight = candidateFocusesNode.node("coldestWeight").getFloat();

        List<? extends ConfigurationNode> candidateHotspotTypesNodes = candidateFocusesNode.node("hotspotTypes").childrenList();
        for (ConfigurationNode hotspotTypeNode : candidateHotspotTypesNodes)
        {
            try
            {
                Class<?> hotspotType = Class.forName("io.lazysheeep.lazydirector.hotspot." + hotspotTypeNode.node("type").getString());
                List<Pair<CameraShotType, Float>> shotTypes = new ArrayList<>();
                for(ConfigurationNode shotTypeNode : hotspotTypeNode.node("shotTypes").childrenList())
                {
                    String type = shotTypeNode.node("type").getString();
                    CameraShotType cameraShotType = (CameraShotType) Class.forName("io.lazysheeep.lazydirector.camerashottype." + type).getConstructor().newInstance();
                    float weight = shotTypeNode.node("weight").getFloat();
                    shotTypes.add(Pair.of(cameraShotType, weight));
                }
                candidateHotspotTypes.put(hotspotType, shotTypes);
            }
            catch (ClassNotFoundException e)
            {
                throw new ConfigurateException(hotspotTypeNode, "Failed to load hotspotTypes because " + e.getMessage());
            }
            catch (Exception e)
            {
                throw new ConfigurateException(e);
            }
        }

        LazyDirector.Log(Level.INFO,  "Created cameraman: " + name);
    }

    /**
     * <p>
     *     Destroy the cameraman.
     * </p>
     */
    public void destroy()
    {
        LazyDirector.Log(Level.INFO, "Destroying cameraman: " + name);
        // detach all outputs
        outputs.forEach(this::detachCamera);
        outputs.clear();
        // remove camera
        if (camera != null)
        {
            camera.remove();
        }
        camera = null;
        // reset focus
        focus = null;
        focusTime = 0.0f;
        // reset camera shot type
        cameraShotType = null;
    }

    private Entity camera = null;
    private Hotspot focus = null;
    private float focusTime = 0.0f;
    private CameraShotType cameraShotType = null;
    private final List<Player> outputs = new LinkedList<>();

    /**
     * <p>
     *     Create a camera entity.
     * </p>
     * @param name The name given to the camera entity
     * @param location The initial location of the camera entity
     * @return The created camera entity
     */
    private static @NotNull Entity CreateCamera(@NotNull String name, @NotNull Location location)
    {
        ArmorStand camera = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        camera.customName(Component.text(name));
        camera.setMarker(true);
        camera.setSmall(true);
        camera.setInvisible(true);
        LazyDirector.Log(Level.INFO, "Created camera " + name + " at " + location);
        return camera;
    }

    /**
     * <p>
     *     Attach a player to the camera.
     * </p>
     * @param outputPlayer The player to attach to the camera
     */
    public void attachCamera(@NotNull Player outputPlayer)
    {
        LazyDirector.GetPlugin().getDirector().detachFromAnyCamera(outputPlayer);
        outputs.add(outputPlayer);
        outputPlayer.setGameMode(GameMode.SPECTATOR);
        outputPlayer.setSpectatorTarget(camera);
    }

    /**
     * <p>
     *     Detach a player from the camera.
     * </p>
     * @param outputPlayer The player to detach from the camera
     */
    public void detachCamera(@NotNull Player outputPlayer)
    {
        if(outputs.contains(outputPlayer))
        {
            outputs.remove(outputPlayer);
            outputPlayer.setSpectatorTarget(null);
        }
    }

    /**
     * <p>
     *     Update the camera, output players, and switch focus when needed.
     * </p>
     * <p>
     *     This method is called once every tick by {@link Director}.
     * </p>
     */
    public void update()
    {
        // check camera
        if (camera == null || !camera.isValid())
        {
            camera = CreateCamera(name + "'s Camera", LazyDirector.GetPlugin()
                                                                  .getHotspotManager()
                                                                  .getAllHotspotsSorted()
                                                                  .getFirst()
                                                                  .getLocation());
        }

        // switch focus
        if (focus == null || !focus.isValid() || focusTime > maxFocusSwitchTime || (focusTime > minFocusSwitchTime && !getCandidateFocuses().contains(focus)))
        {
            switchFocus();
        }

        // clear invalid outputs
        outputs.removeIf(output -> !output.isOnline() || output.getSpectatorTarget() != camera);

        if (focus != null)
        {
            // update camera location
            cameraShotType.updateCameraLocation(camera, focus.getLocation());
            // if output player is too far from camera location, teleport them
            for (Player output : outputs)
            {
                if (output.getLocation().distance(camera.getLocation()) > 64.0f)
                {
                    output.teleport(camera.getLocation());
                }
            }
            // update focus time
            focusTime += 1.0f / LazyDirector.GetPlugin().getServer().getServerTickManager().getTickRate();
            // call event
            new HotspotBeingFocusedEvent(focus, this).callEvent();
        }

        // switch cameraman for output players if they break away
        for (Player player : outputs)
        {
            if (player.getSpectatorTarget() == null)
            {
                LazyDirector.GetPlugin().getDirector().switchCameraman(player, this);
            }
        }
    }

    /**
     * <p>
     *     Switch the focus.
     * </p>
     */
    private void switchFocus()
    {
        List<Hotspot> candidateFocuses = getCandidateFocuses();
        if (!candidateFocuses.isEmpty())
        {
            focus = RandomUtils.RandomPickOne(candidateFocuses);
            switchShotType();
        }
        else
        {
            focus = null;
        }
        focusTime = 0.0f;
    }

    /**
     * <p>
     *     Get the list of candidate focuses.
     * </p>
     * @return The list of candidate focuses
     */
    private @NotNull List<Hotspot> getCandidateFocuses()
    {
        List<Hotspot> sortedHotspots = LazyDirector.GetPlugin().getHotspotManager().getAllHotspotsSorted();
        sortedHotspots.removeIf(hotspot -> !candidateHotspotTypes.containsKey(hotspot.getClass()));
        int hottestCandidateIndex = (int) Math.floor(candidateHottestRank * sortedHotspots.size());
        int coldestCandidateIndex = (int) Math.floor(candidateColdestRank * sortedHotspots.size());
        coldestCandidateIndex = Math.min(Math.min(coldestCandidateIndex, hottestCandidateIndex + candidateMaxCount), sortedHotspots.size());
        return sortedHotspots.subList(hottestCandidateIndex, coldestCandidateIndex);
    }

    /**
     * <p>
     *     Switch the shot type.
     * </p>
     */
    private void switchShotType()
    {
        List<Pair<CameraShotType, Float>> cameraShotTypes = candidateHotspotTypes.get(focus.getClass());
        float totalWeight = (float) cameraShotTypes.stream().mapToDouble(Pair::getRight).sum();
        float random = (float) (Math.random() * totalWeight);
        float sum = 0.0f;
        for (Pair<CameraShotType, Float> pair : cameraShotTypes)
        {
            sum += pair.getRight();
            if (random < sum)
            {
                cameraShotType = pair.getLeft();
                break;
            }
        }
    }

    public static void LookAt(@NotNull Location origin, @NotNull Location target)
    {
        double dx = target.getX() - origin.getX();
        double dy = target.getY() - origin.getY();
        double dz = target.getZ() - origin.getZ();
        origin.setDirection(new Vector(dx, dy, dz));
    }

    @Override
    public String toString()
    {
        return "{name=" + name + ",focus=" + focus + ",focusTime=" + focusTime + ",cameraShotType=" + cameraShotType + ",outputs=" + outputs + "}";
    }
}
