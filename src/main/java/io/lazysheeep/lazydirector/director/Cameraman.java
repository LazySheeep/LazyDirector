package io.lazysheeep.lazydirector.director;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.camerashottype.CameraShotType;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.RandomUtils;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Cameraman
{
    private final String name;

    public @NotNull String getName()
    {
        return name;
    }

    private final float minFocusSwitchTime;
    private final float maxFocusSwitchTime;

    private final int candidateMaxCount;
    private final float candidateHottestRank;
    private final float candidateHottestWeight;
    private final float candidateColdestRank;
    private final float candidateColdestWeight;

    private final List<Pair<CameraShotType, Float>> cameraShotTypes = new ArrayList<>();
    private CameraShotType cameraShotType;

    public Cameraman(@NotNull Map<?, ?> config)
    {
        this.name = (String) config.get("name");
        this.minFocusSwitchTime = ((Number) config.get("minFocusSwitchTime")).floatValue();
        this.maxFocusSwitchTime = ((Number) config.get("maxFocusSwitchTime")).floatValue();

        Map<?, ?> candidateFocuses = (Map<?, ?>) config.get("candidateFocuses");
        this.candidateMaxCount = ((Number) candidateFocuses.get("maxCount")).intValue();
        this.candidateHottestRank = ((Number) candidateFocuses.get("hottest")).floatValue();
        this.candidateHottestWeight = ((Number) candidateFocuses.get("hottestWeight")).floatValue();
        this.candidateColdestRank = ((Number) candidateFocuses.get("coldest")).floatValue();
        this.candidateColdestWeight = ((Number) candidateFocuses.get("coldestWeight")).floatValue();

        List<Map<?, ?>> shotTypes = (List<Map<?, ?>>) config.get("shotTypes");
        for (Map<?, ?> shotType : shotTypes)
        {
            String type = (String) shotType.get("type");
            CameraShotType cameraShotType;
            try
            {
                cameraShotType = CameraShotType.GetType(type).getConstructor().newInstance();
            }
            catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
            float weight = ((Number) shotType.get("weight")).floatValue();
            this.cameraShotTypes.add(Pair.of(cameraShotType, weight));
        }
    }

    public void destroy()
    {
        if(camera != null)
        {
            camera.remove();
        }
    }

    private ArmorStand camera = null;
    private Hotspot focus = null;
    private float focusTime = 0.0f;
    private final List<Player> outputs = new LinkedList<>();

    public void update()
    {
        // check camera
        if(camera == null || !camera.isValid())
        {
            camera = CreateCamera(name + "'s Camera", LazyDirector.GetPlugin().getHotspotManager().getAllHotspotsSorted().getFirst().getLocation());
        }

        // switch focus
        if(focus == null || !focus.isValid() || focusTime > maxFocusSwitchTime || (focusTime > minFocusSwitchTime && !getCandidateFocuses().contains(focus)))
        {
            switchFocus();
        }

        if (focus != null)
        {
            // update camera location
            cameraShotType.updateCameraLocation(camera, focus.getLocation());

            focusTime += 1.0f / LazyDirector.GetPlugin().getServer().getServerTickManager().getTickRate();
            focus.increase("hunger");
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

    private static @NotNull ArmorStand CreateCamera(@NotNull String name, @NotNull Location location)
    {
        ArmorStand camera = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        camera.customName(Component.text(name));
        camera.setMarker(true);
        camera.setSmall(true);
        camera.setInvisible(true);
        LazyDirector.GetPlugin().getLogger().log(Level.INFO, "Created camera " + name + " at " + location);
        return camera;
    }

    public void attachCamera(@NotNull Player player)
    {
        outputs.add(player);
        player.setGameMode(GameMode.SPECTATOR);
        player.setSpectatorTarget(camera);
    }

    public void detachCamera(@NotNull Player player)
    {
        outputs.remove(player);
        player.setSpectatorTarget(null);
    }

    private @NotNull List<Hotspot> getCandidateFocuses()
    {
        List<Hotspot> sortedHotspots = LazyDirector.GetPlugin().getHotspotManager().getAllHotspotsSorted();
        int hottestCandidateIndex = (int) Math.floor(candidateHottestRank * sortedHotspots.size());
        int coldestCandidateIndex = (int) Math.floor(candidateColdestRank * sortedHotspots.size());
        coldestCandidateIndex = Math.min(Math.min(coldestCandidateIndex, hottestCandidateIndex + candidateMaxCount), sortedHotspots.size());
        return sortedHotspots.subList(hottestCandidateIndex, coldestCandidateIndex);
    }

    private void switchFocus()
    {
        List<Hotspot> candidateFocuses = getCandidateFocuses();
        if(!candidateFocuses.isEmpty())
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

    private void switchShotType()
    {
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
}
