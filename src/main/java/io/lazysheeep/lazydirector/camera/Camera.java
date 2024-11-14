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

    private final float minSwitchTime;
    private final float initFocusScore;
    private final float goodFocusRewardMultiplier;
    private final float badFocusPenaltyMultiplier;

    private final int candidateMaxCount;
    private final float candidateColdestRank;

    private record CameraViewWrap(CameraView cameraView, float weight, float initScore, float goodViewReward, float badViewPenalty, float satTime, float satPenalty)
    {
    }

    private final Map<Class<?>, List<CameraViewWrap>> candidateHotspotTypes = new HashMap<>();
    private final CameraViewWrap defaultCameraViewWrap = new CameraViewWrap(new RawView(null), 1.0f, 999.0f, 0.0f, 0.0f, 999.0f, 0.0f);

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
     * @throws ConfigurateException If failed to load the configuration
     */
    Camera(@NotNull ConfigurationNode configNode) throws ConfigurateException
    {
        this.name = configNode.node("name").getString();
        this.cameraIsVisible = configNode.node("visible").getBoolean(false);

        this.minSwitchTime = configNode.node("minSwitchTime").getFloat(1.0f);
        this.initFocusScore = configNode.node("initFocusScore").getFloat(1.0f);
        this.goodFocusRewardMultiplier = configNode.node("goodFocusRewardMultiplier").getFloat(1.0f);
        this.badFocusPenaltyMultiplier = configNode.node("badFocusPenaltyMultiplier").getFloat(1.0f);

        ConfigurationNode candidateFocusesNode = configNode.node("candidateFocuses");
        this.candidateMaxCount = candidateFocusesNode.node("maxCount").getInt(Integer.MAX_VALUE);
        this.candidateColdestRank = candidateFocusesNode.node("coldestRank").getFloat(1.0f);

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
                    float initScore = cameraViewNode.node("initScore").getFloat(999.0f);
                    float goodViewReward = cameraViewNode.node("goodViewReward").getFloat(0.0f);
                    float badViewPenalty = cameraViewNode.node("badViewPenalty").getFloat(0.0f);
                    float satTime = cameraViewNode.node("satTime").getFloat(999.0f);
                    float satPenalty = cameraViewNode.node("satPenalty").getFloat(0.0f);
                    cameraViews.add(new CameraViewWrap(cameraView, weight, initScore, goodViewReward, badViewPenalty, satTime, satPenalty));
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

    private Hotspot currentFocus = null;
    private float focusTimer = 0.0f;
    private float focusScore = 0.0f;

    private CameraViewWrap currentCameraViewWrap = null;
    private float cameraViewTimer = 0.0f;
    private float cameraViewScore = 0.0f;

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
        // update timer
        focusTimer += LazyDirector.GetServerTickDeltaTime();
        cameraViewTimer += LazyDirector.GetServerTickDeltaTime();

        // check focus validity
        if (currentFocus == null || !currentFocus.isValid())
        {
            switchFocus();
        }
        // update focus score
        List<Hotspot> candidateFocuses = getCandidateFocuses();
        Hotspot lastCandidateFocus = candidateFocuses.getLast();
        List<Hotspot> nonCandidateFocuses = getNonCandidateFocuses();
        Hotspot firstNonCandidateFocus = nonCandidateFocuses.isEmpty() ? lastCandidateFocus : nonCandidateFocuses.getFirst();
        if (candidateFocuses.contains(currentFocus))
        {
            focusScore += goodFocusRewardMultiplier * (currentFocus.getHeat() - firstNonCandidateFocus.getHeat()) * LazyDirector.GetServerTickDeltaTime();
        }
        else
        {
            focusScore -= badFocusPenaltyMultiplier * (lastCandidateFocus.getHeat() - currentFocus.getHeat()) * LazyDirector.GetServerTickDeltaTime();
        }
        // switch focus
        if (focusScore <= 0.0f && Math.min(focusTimer, cameraViewTimer) > minSwitchTime)
        {
            switchFocus();
        }

        // check camera view validity
        if (currentCameraViewWrap == null)
        {
            switchCameraView();
        }
        // update camera view score
        if (currentCameraViewWrap.cameraView.isViewGood())
        {
            cameraViewScore += currentCameraViewWrap.goodViewReward * LazyDirector.GetServerTickDeltaTime();
        }
        else
        {
            cameraViewScore -= currentCameraViewWrap.badViewPenalty * LazyDirector.GetServerTickDeltaTime();
        }
        if (cameraViewTimer > currentCameraViewWrap.satTime)
        {
            cameraViewScore -= currentCameraViewWrap.satPenalty * LazyDirector.GetServerTickDeltaTime();
        }
        // switch camera view
        if (currentCameraViewWrap.cameraView.cannotFindGoodView() || (cameraViewScore <= 0.0f && Math.min(focusTimer, cameraViewTimer) > minSwitchTime))
        {
            switchCameraView();
        }

        // call event
        new HotspotBeingFocusedEvent(currentFocus, this).callEvent();

        // update camera entity and outputs only when outputs is not empty
        if (!outputs.isEmpty())
        {
            // check camera entity
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
            // update camera view
            currentCameraViewWrap.cameraView.updateCameraLocation(currentFocus);
            // update camera entity location
            cameraEntity.teleport(MathUtils.Lerp(cameraEntity.getLocation(), currentCameraViewWrap.cameraView.getCurrentCameraLocation(), 0.25f));

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
                    Objects.requireNonNull(outputPlayer.addAttachment(LazyDirector.GetPlugin(), 1200))
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
                cameraViewScore = 0.0f;
            }
            currentFocus = newFocus;
        }
        else
        {
            throw new RuntimeException("No candidate focus");
        }
        focusTimer = 0.0f;
        focusScore = initFocusScore;
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
        int hottestCandidateIndex = 0;
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

    public @NotNull List<Hotspot> getNonCandidateFocuses()
    {
        List<Hotspot> sortedHotspots = LazyDirector.GetPlugin().getHotspotManager().getAllHotspotsSorted();
        sortedHotspots.removeIf(hotspot -> !candidateHotspotTypes.containsKey(hotspot.getClass()));
        int coldestCandidateIndex = (int) Math.ceil(candidateColdestRank * sortedHotspots.size()) - 1;
        coldestCandidateIndex = Math.min(Math.min(coldestCandidateIndex, candidateMaxCount - 1), sortedHotspots.size() - 1);
        while (coldestCandidateIndex + 1 < sortedHotspots.size() && sortedHotspots.get(coldestCandidateIndex)
                                                                                  .getHeat() == sortedHotspots.get(coldestCandidateIndex + 1)
                                                                                                              .getHeat())
        {
            coldestCandidateIndex++;
        }
        return sortedHotspots.subList(coldestCandidateIndex + 1, sortedHotspots.size());
    }

    /**
     * <p>
     * Switch camera view type.
     * </p>
     */
    private void switchCameraView()
    {
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
            while(!candidateCameraViews.isEmpty())
            {
                // pick a camera view
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
                // update camera view
                currentCameraViewWrap.cameraView.updateCameraLocation(currentFocus);
                if (currentCameraViewWrap.cameraView.cannotFindGoodView())
                {
                    // if the new camera view cannot find a good view, remove it from the candidate list and try again
                    candidateCameraViews.remove(currentCameraViewWrap);
                }
                else
                {
                    break;
                }
            }
        }
        cameraViewTimer = 0.0f;
        cameraViewScore = currentCameraViewWrap.initScore;
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[").append(name).append("]\n");
        stringBuilder.append("  Focus: ").append(currentFocus).append("\n");
        stringBuilder.append("  Focus Timer: ").append(focusTimer).append("\n");
        stringBuilder.append("  Focus Score: ").append(focusScore).append("\n");
        stringBuilder.append("  Camera View: ").append(currentCameraViewWrap.cameraView).append("\n");
        stringBuilder.append("  Camera View Timer: ").append(cameraViewTimer).append("\n");
        stringBuilder.append("  Camera View Score: ").append(cameraViewScore).append("\n");
        stringBuilder.append("  Outputs:");
        outputs.forEach(output -> stringBuilder.append(" ").append(output.getName()));
        return stringBuilder.toString();
    }
}
