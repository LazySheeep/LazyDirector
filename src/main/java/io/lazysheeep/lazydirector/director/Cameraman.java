package io.lazysheeep.lazydirector.director;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.camerashottype.BirdsEyeView;
import io.lazysheeep.lazydirector.camerashottype.CameraView;
import io.lazysheeep.lazydirector.events.HotspotBeingFocusedEvent;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import io.lazysheeep.lazydirector.util.RandomUtils;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;
import java.util.logging.Level;

/**
 * <p>
 * Cameraman class represents a cameraman who can control a camera to shoot a hotspot.
 * </p>
 * <p>
 * All cameramen are managed by {@link Director}.
 * <br>
 * The creation of cameraman only happens when {@link Director} load the configuration.
 * </p>
 */
public class Cameraman
{
    private final String name;

    private final float minFocusSwitchTime;
    private final float maxFocusSwitchTime;

    private final int candidateMaxCount;
    private final float candidateHottestRank;
    private final float candidateColdestRank;

    private final Map<Class<?>, List<Pair<CameraView, Float>>> candidateHotspotTypes = new HashMap<>();
    private final CameraView defaultCameraView = new BirdsEyeView();

    public @NotNull String getName()
    {
        return name;
    }

    /**
     * <p>
     * Construct a cameraman from a configuration node.
     * </p>
     * <p>
     * Cameraman's configuration is immutable, you'll have to create a new one if you want to load a new configuration.
     * </p>
     * <p>
     * Should only be called by {@link Director}.
     * </p>
     *
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
        this.candidateColdestRank = candidateFocusesNode.node("coldest").getFloat();

        List<? extends ConfigurationNode> candidateHotspotTypesNodes = candidateFocusesNode.node("hotspotTypes")
                                                                                           .childrenList();
        for (ConfigurationNode hotspotTypeNode : candidateHotspotTypesNodes)
        {
            try
            {
                Class<?> hotspotType = Class.forName("io.lazysheeep.lazydirector.hotspot." + hotspotTypeNode.node("type")
                                                                                                            .getString() + "Hotspot");
                List<Pair<CameraView, Float>> cameraViews = new ArrayList<>();
                for (ConfigurationNode cameraViewNode : hotspotTypeNode.node("cameraViews").childrenList())
                {
                    String type = cameraViewNode.node("type").getString();
                    CameraView cameraView = (CameraView) Class.forName("io.lazysheeep.lazydirector.camerashottype." + type + "View")
                                                              .getConstructor()
                                                              .newInstance();
                    float weight = cameraViewNode.node("weight").getFloat();
                    cameraViews.add(Pair.of(cameraView, weight));
                }
                candidateHotspotTypes.put(hotspotType, cameraViews);
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

        LazyDirector.Log(Level.INFO, "Created cameraman: " + name);
    }

    /**
     * <p>
     * Destroy the cameraman.
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
        currentCameraView = null;
    }

    private Entity camera = null;
    private Hotspot focus = null;
    private float focusTime = 0.0f;
    private CameraView currentCameraView = null;
    private final List<Player> outputs = new LinkedList<>();

    /**
     * <p>
     * Create a camera entity.
     * </p>
     *
     * @param name     The name given to the camera entity
     * @param location The initial location of the camera entity
     * @return The created camera entity
     */
    private static @Nullable Entity CreateCamera(@NotNull String name, @NotNull Location location)
    {
        ArmorStand camera = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        if (camera.isValid())
        {
            camera.customName(Component.text(name));
            camera.addScoreboardTag("LazyDirector.Cameraman.Camera");
            camera.setMarker(true);
            camera.setSmall(true);
            camera.setInvisible(true);
            LazyDirector.Log(Level.INFO, "Created camera " + name + " at " + location);
            return camera;
        }
        else
        {
            camera.remove();
            LazyDirector.Log(Level.WARNING, "Failed to create camera " + name + " at " + location);
            return null;
        }
    }

    /**
     * <p>
     * Attach a player to the camera.
     * </p>
     *
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
     * Detach a player from the camera.
     * </p>
     *
     * @param outputPlayer The player to detach from the camera
     */
    public void detachCamera(@NotNull Player outputPlayer)
    {
        if (outputs.contains(outputPlayer))
        {
            outputs.remove(outputPlayer);
            outputPlayer.setSpectatorTarget(null);
        }
    }

    /**
     * <p>
     * Update the camera, output players, and switch focus when needed.
     * </p>
     * <p>
     * This method is called once every tick by {@link Director}.
     * </p>
     */
    public void update()
    {
        // check camera
        if (camera == null || !camera.isValid())
        {
            camera = CreateCamera("LazyDirector.Cameraman:" + name + ".Camera", LazyDirector.GetPlugin()
                                                                                            .getHotspotManager()
                                                                                            .getDefaultHotspot()
                                                                                            .getLocation());
        }

        // switch focus
        if (focus == null || !focus.isValid() || focusTime > maxFocusSwitchTime || (focusTime > minFocusSwitchTime && !getCandidateFocuses().contains(focus)))
        {
            switchFocus();
            switchCameraView();
        }

        // update focus time
        focusTime += 1.0f / LazyDirector.GetPlugin().getServer().getServerTickManager().getTickRate();
        // call event
        new HotspotBeingFocusedEvent(focus, this).callEvent();

        // update camera location
        Location nextCameraLocation = currentCameraView.updateCameraLocation(focus);
        if (nextCameraLocation != null)
        {
            camera.teleport(MathUtils.Lerp(camera.getLocation(), nextCameraLocation, 0.25f));
        }
        else
        {
            switchFocus();
            switchCameraView();
        }

        // clear invalid outputs
        outputs.removeIf(output -> !output.isOnline());

        /*// switch cameraman for output players if they break away
        for (Player player : outputs)
        {
            if (player.getSpectatorTarget() == null)
            {
                LazyDirector.GetPlugin().getDirector().switchCameraman(player, this);
            }
        }*/

        // update outputs
        for (Player output : outputs)
        {
            // BUG: MC-157812 (https://bugs.mojang.com/browse/MC-157812)
            if (!output.isChunkSent(camera.getChunk()) || MathUtils.Distance(output.getLocation(), camera.getLocation()) > 64.0d)
            {
                LazyDirector.Log(Level.INFO, "Sending chunk to " + output.getName());
                output.setGameMode(GameMode.SPECTATOR);
                output.setSpectatorTarget(null);
                output.teleport(camera.getLocation());
            }
            else
            {
                output.setGameMode(GameMode.SPECTATOR);
                output.setSpectatorTarget(camera);
            }
        }
    }

    public void lateUpdate()
    {

    }

    /**
     * <p>
     * Switch the focus.
     * </p>
     */
    private void switchFocus()
    {
        List<Hotspot> candidateFocuses = getCandidateFocuses();
        if (!candidateFocuses.isEmpty())
        {
            focus = RandomUtils.PickOne(candidateFocuses);
        }
        else
        {
            throw new RuntimeException("No candidate focus");
        }
        focusTime = 0.0f;
    }

    /**
     * <p>
     * Get the list of candidate focuses.
     * </p>
     *
     * @return The list of candidate focuses
     */
    private @NotNull List<Hotspot> getCandidateFocuses()
    {
        List<Hotspot> sortedHotspots = LazyDirector.GetPlugin().getHotspotManager().getAllHotspotsSorted();
        sortedHotspots.removeIf(hotspot -> !candidateHotspotTypes.containsKey(hotspot.getClass()));
        int hottestCandidateIndex = (int) Math.floor(candidateHottestRank * sortedHotspots.size());
        int coldestCandidateIndex = (int) Math.floor(candidateColdestRank * sortedHotspots.size());
        coldestCandidateIndex = Math.min(Math.min(coldestCandidateIndex, hottestCandidateIndex + candidateMaxCount), sortedHotspots.size());
        List<Hotspot> candidateFocus = sortedHotspots.subList(hottestCandidateIndex, coldestCandidateIndex);
        // return the default hotspot if no candidate focus
        return candidateFocus.isEmpty() ? Collections.singletonList(LazyDirector.GetPlugin()
                                                                                .getHotspotManager()
                                                                                .getDefaultHotspot()) : candidateFocus;
    }

    /**
     * <p>
     * Switch the shot type.
     * </p>
     */
    private void switchCameraView()
    {
        if (currentCameraView != null)
        {
            currentCameraView.reset();
        }

        if(focus == LazyDirector.GetPlugin().getHotspotManager().getDefaultHotspot())
        {
            currentCameraView = defaultCameraView;
        }
        else
        {
            List<Pair<CameraView, Float>> candidateCameraViews = candidateHotspotTypes.get(focus.getClass());
            float totalWeight = (float) candidateCameraViews.stream().mapToDouble(Pair::getRight).sum();
            float random = (float) (Math.random() * totalWeight);
            float sum = 0.0f;
            for (Pair<CameraView, Float> pair : candidateCameraViews)
            {
                sum += pair.getRight();
                if (random < sum)
                {
                    currentCameraView = pair.getLeft();
                    break;
                }
            }
        }
    }

    @Override
    public String toString()
    {
        return "{name=" + name + ",focus=" + focus + ",focusTime=" + focusTime + ",cameraShotType=" + currentCameraView + ",outputs=" + outputs + "}";
    }
}
