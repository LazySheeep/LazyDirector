package io.lazysheeep.lazydirector.camera;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.cameraview.CameraView;
import io.lazysheeep.lazydirector.cameraview.RawView;
import io.lazysheeep.lazydirector.events.HotspotBeingFocusedEvent;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import io.lazysheeep.lazydirector.util.RandomUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;
import java.util.logging.Level;

/**
 * <p>
 * Camera class represents a camera which can control itself to spectate a hotspot.
 * </p>
 * <p>
 * All cameras are managed by {@link CameraManager}.
 * <br>
 * The creation of camera only happens when {@link CameraManager} load a configuration.
 * </p>
 */
public class Camera
{
    private final String name;
    private final boolean cameraIsVisible;
    private final float minFocusSwitchTime;
    private final float maxFocusSwitchTime;
    private final int candidateMaxCount;
    private final float candidateHottestRank;
    private final float candidateColdestRank;

    private record CameraViewWrap(CameraView cameraView, float weight, float switchTime)
    {
    }

    private final Map<Class<?>, List<CameraViewWrap>> candidateHotspotTypes = new HashMap<>();
    private final CameraViewWrap defaultCameraViewWrap = new CameraViewWrap(new RawView(null), 1.0f, Float.MAX_VALUE);

    public @NotNull String getName()
    {
        return name;
    }

    /**
     * <p>
     * Construct a camera from a configuration node.
     * </p>
     * <p>
     * Camera's configuration is immutable, you'll have to create a new one if you want to load a new configuration.
     * </p>
     * <p>
     * Should only be called by {@link CameraManager}.
     * </p>
     *
     * @param configNode The configuration node of the camera
     * @throws ConfigurateException
     */
    Camera(@NotNull ConfigurationNode configNode) throws ConfigurateException
    {
        this.name = configNode.node("name").getString();
        this.cameraIsVisible = configNode.node("visible").getBoolean(false);
        this.minFocusSwitchTime = configNode.node("minFocusSwitchTime").getFloat(0.0f);
        this.maxFocusSwitchTime = configNode.node("maxFocusSwitchTime").getFloat(Float.MAX_VALUE);

        ConfigurationNode candidateFocusesNode = configNode.node("candidateFocuses");
        this.candidateMaxCount = candidateFocusesNode.node("maxCount").getInt(Integer.MAX_VALUE);
        this.candidateHottestRank = candidateFocusesNode.node("hottest").getFloat(0.0f);
        this.candidateColdestRank = candidateFocusesNode.node("coldest").getFloat(1.0f);

        List<? extends ConfigurationNode> candidateHotspotTypesNodes = candidateFocusesNode.node("hotspotTypes")
                                                                                           .childrenList();
        for (ConfigurationNode hotspotTypeNode : candidateHotspotTypesNodes)
        {
            try
            {
                Class<?> hotspotType = Class.forName("io.lazysheeep.lazydirector.hotspot." + hotspotTypeNode.node("type")
                                                                                                            .getString() + "Hotspot");
                List<CameraViewWrap> cameraViews = new ArrayList<>();
                for (ConfigurationNode cameraViewNode : hotspotTypeNode.node("cameraViews").childrenList())
                {
                    CameraView cameraView = CameraView.CreateCameraView(cameraViewNode.node("type"));
                    float weight = cameraViewNode.node("weight").getFloat(1.0f);
                    float switchTime = cameraViewNode.node("switchTime").getFloat(Float.MAX_VALUE);
                    cameraViews.add(new CameraViewWrap(cameraView, weight, switchTime));
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

        LazyDirector.Log(Level.INFO, "Created camera: " + name);
    }

    /**
     * <p>
     * Destroy the camera.
     * </p>
     */
    public void destroy()
    {
        LazyDirector.Log(Level.INFO, "Destroying camera: " + name);
        // detach all outputs
        outputs.forEach(this::detachOutput);
        outputs.clear();
        // remove camera
        if (cameraEntity != null)
        {
            cameraEntity.remove();
        }
        cameraEntity = null;
        // reset focus
        currentFocus = null;
        focusTimer = 0.0f;
        // reset camera shot type
        currentCameraViewWrap = null;
        cameraViewTimer = 0.0f;
    }

    private Entity cameraEntity = null;
    private Hotspot currentFocus = null;
    private float focusTimer = 0.0f;
    private CameraViewWrap currentCameraViewWrap = null;
    private float cameraViewTimer = 0.0f;
    private final List<Player> outputs = new LinkedList<>();

    public List<Player> getOutputs()
    {
        return Collections.unmodifiableList(outputs);
    }

    /**
     * <p>
     * Create a camera entity.
     * </p>
     *
     * @param name     The name given to the camera entity
     * @param location The initial location of the camera entity
     * @return The created camera entity
     */
    private static @Nullable Entity CreateCameraEntity(@NotNull String name, @NotNull Location location, boolean visible)
    {
        if (location.getChunk().isLoaded())
        {
            ItemDisplay newCamera = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
            if (newCamera.isValid())
            {
                newCamera.customName(Component.text(name));
                newCamera.addScoreboardTag("LazyDirector.Camera.CameraEntity");
                if (visible)
                {
                    PlayerProfile headProfile = Bukkit.createProfile(UUID.randomUUID());
                    headProfile.setProperty(new ProfileProperty("textures", "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDc5OWJhMjI3ZjFmMjViMjg3ZjdkNzgxNGU1MjY0ZGNlMmNkNjk5ZTVkMWViZjU2MmY1ZWVkOTBiMDU4MTlhOCJ9fX0="));
                    ItemStack cameraHead = new ItemStack(Material.PLAYER_HEAD);
                    cameraHead.editMeta(meta -> ((SkullMeta) meta).setPlayerProfile(headProfile));
                    newCamera.setItemStack(cameraHead);
                    newCamera.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                    newCamera.setBrightness(new Display.Brightness(15, 15));
                }
                else
                {
                    newCamera.setInvisible(true);
                }
                newCamera.setTeleportDuration(4);
                LazyDirector.Log(Level.INFO, "Created camera entity " + name + " at " + location);
                return newCamera;
            }
            else
            {
                newCamera.remove();
                LazyDirector.Log(Level.WARNING, "Failed to create camera entity " + name + " at " + location);
                return null;
            }
        }
        else
        {
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
    public void attachOutput(@NotNull Player outputPlayer)
    {
        LazyDirector.GetPlugin().getCameraManager().detachFromAnyCamera(outputPlayer);
        outputs.add(outputPlayer);
        outputPlayer.setGameMode(GameMode.SPECTATOR);
        outputPlayer.setSpectatorTarget(cameraEntity);
    }

    /**
     * <p>
     * Detach a player from the camera.
     * </p>
     *
     * @param outputPlayer The player to detach from the camera
     */
    public void detachOutput(@NotNull Player outputPlayer)
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
     * This method is called once every tick by {@link CameraManager}.
     * </p>
     */
    public void update()
    {
        // switch focus
        if (currentFocus == null || !currentFocus.isValid() || focusTimer > maxFocusSwitchTime || (focusTimer > minFocusSwitchTime && !getCandidateFocuses().contains(currentFocus)))
        {
            switchFocus();
        }

        // switch camera view
        if (currentCameraViewWrap == null || cameraViewTimer > currentCameraViewWrap.switchTime)
        {
            switchCameraView();
        }

        // update timer
        focusTimer += LazyDirector.GetServerTickDeltaTime();
        cameraViewTimer += LazyDirector.GetServerTickDeltaTime();

        // call event
        new HotspotBeingFocusedEvent(currentFocus, this).callEvent();

        if (!outputs.isEmpty())
        {
            // check camera
            if (cameraEntity == null || !cameraEntity.isValid())
            {
                cameraEntity = CreateCameraEntity("LazyDirector.Camera:" + name + ".CameraEntity", LazyDirector.GetPlugin()
                                                                                                            .getHotspotManager()
                                                                                                            .getDefaultHotspot()
                                                                                                            .getLocation(), cameraIsVisible);
                if (cameraEntity == null)
                {
                    return;
                }
            }

            // update camera location
            Location nextCameraLocation = currentCameraViewWrap.cameraView.updateCameraLocation(currentFocus);
            if (nextCameraLocation == null)
            {
                LazyDirector.Log(Level.INFO, "nextCameraLocation == null");
                switchCameraView();
            }
            else
            {
                cameraEntity.teleport(MathUtils.Lerp(cameraEntity.getLocation(), nextCameraLocation, 0.25f));
            }

            // clear invalid outputs
            outputs.removeIf(output -> !output.isOnline());

            // update outputs
            for (Player outputPlayer : outputs)
            {
                // BUG: MC-157812 (https://bugs.mojang.com/browse/MC-157812)
                if (!outputPlayer.isChunkSent(cameraEntity.getChunk()) || MathUtils.Distance(outputPlayer.getLocation(), cameraEntity.getLocation()) > 64.0d)
                {
                    // LazyDirector.Log(Level.INFO, "Waiting for chunk to be sent to " + outputPlayer.getName());
                    outputPlayer.setGameMode(GameMode.SPECTATOR);
                    outputPlayer.setSpectatorTarget(null);
                    outputPlayer.teleport(cameraEntity.getLocation());
                }
                else
                {
                    outputPlayer.setGameMode(GameMode.SPECTATOR);
                    outputPlayer.setSpectatorTarget(cameraEntity);
                }
                // avoid being kicked by Essentials because of afk
                if (!outputPlayer.hasPermission("essentials.afk.kickexempt"))
                {
                    outputPlayer.addAttachment(LazyDirector.GetPlugin(), 1200)
                                .setPermission("essentials.afk.kickexempt", true);
                }
            }
        }
    }

    /**
     * <p>
     * Switch the focus.
     * </p>
     */
    private void switchFocus()
    {
        if (currentFocus == LazyDirector.GetPlugin().getHotspotManager().getDefaultHotspot())
        {
            currentCameraViewWrap = null;
        }

        List<Hotspot> candidateFocuses = getCandidateFocuses();
        if (!candidateFocuses.isEmpty())
        {
            if (candidateFocuses.size() > 1)
            {
                candidateFocuses.remove(currentFocus);
            }
            Hotspot newFocus = RandomUtils.PickOne(candidateFocuses);
            if (currentFocus == null || newFocus.getClass() != currentFocus.getClass())
            {
                currentCameraViewWrap = null;
            }
            currentFocus = newFocus;
        }
        else
        {
            throw new RuntimeException("No candidate focus");
        }
        focusTimer = 0.0f;
    }

    /**
     * <p>
     * Get the list of candidate focuses.
     * </p>
     *
     * @return The list of candidate focuses
     */
    public @NotNull List<Hotspot> getCandidateFocuses()
    {
        List<Hotspot> sortedHotspots = LazyDirector.GetPlugin().getHotspotManager().getAllHotspotsSorted();
        sortedHotspots.removeIf(hotspot -> !candidateHotspotTypes.containsKey(hotspot.getClass()));
        int hottestCandidateIndex = (int) Math.floor(candidateHottestRank * sortedHotspots.size());
        int coldestCandidateIndex = (int) Math.ceil(candidateColdestRank * sortedHotspots.size()) - 1;
        coldestCandidateIndex = Math.min(Math.min(coldestCandidateIndex, hottestCandidateIndex + candidateMaxCount - 1), sortedHotspots.size() - 1);
        while (coldestCandidateIndex + 1 < sortedHotspots.size() && sortedHotspots.get(coldestCandidateIndex)
                                                                                  .getHeat() == sortedHotspots.get(coldestCandidateIndex + 1)
                                                                                                              .getHeat())
        {
            coldestCandidateIndex++;
        }
        List<Hotspot> candidateFocus = sortedHotspots.subList(hottestCandidateIndex, coldestCandidateIndex + 1);
        // return the default hotspot if no candidate focus
        return candidateFocus.isEmpty() ? Collections.singletonList(LazyDirector.GetPlugin()
                                                                                .getHotspotManager()
                                                                                .getDefaultHotspot()) : candidateFocus;
    }

    /**
     * <p>
     * Switch camera view type.
     * </p>
     */
    private void switchCameraView()
    {
        if (currentCameraViewWrap != null)
        {
            currentCameraViewWrap.cameraView.reset();
        }
        if (currentFocus == LazyDirector.GetPlugin().getHotspotManager().getDefaultHotspot())
        {
            currentCameraViewWrap = defaultCameraViewWrap;
        }
        else
        {
            List<CameraViewWrap> candidateCameraViews = new ArrayList<>(candidateHotspotTypes.get(currentFocus.getClass()));
            if (candidateCameraViews.size() > 1)
            {
                candidateCameraViews.remove(currentCameraViewWrap);
            }
            float totalWeight = candidateCameraViews.stream()
                                                    .map(cameraViewWrap -> cameraViewWrap.weight)
                                                    .reduce(0.0f, Float::sum);
            float random = RandomUtils.NextFloat(0.0f, totalWeight);
            float sum = 0.0f;
            for (CameraViewWrap cameraViewWrap : candidateCameraViews)
            {
                sum += cameraViewWrap.weight;
                if (random <= sum)
                {
                    currentCameraViewWrap = cameraViewWrap;
                    break;
                }
            }
        }
        cameraViewTimer = 0.0f;
        if(currentCameraViewWrap == null)
        {
            LazyDirector.Log(Level.WARNING, "Failed to switch camera view");
        }
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[").append(name).append("]\n");
        stringBuilder.append("  Focus: ").append(currentFocus).append("\n");
        stringBuilder.append("  Focus Timer: ").append(focusTimer).append("\n");
        stringBuilder.append("  Camera View: ").append(currentCameraViewWrap).append("\n");
        stringBuilder.append("  Camera View Timer: ").append(cameraViewTimer).append("\n");
        stringBuilder.append("  Outputs:");
        outputs.forEach(output -> stringBuilder.append(" ").append(output.getName()));
        return stringBuilder.toString();
    }
}
