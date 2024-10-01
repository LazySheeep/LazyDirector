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
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;

public class Cameraman
{
    private String name;

    public @NotNull String getName()
    {
        return name;
    }

    private float minFocusSwitchTime;
    private float maxFocusSwitchTime;

    private int candidateMaxCount;
    private float candidateHottestRank;
    private float candidateHottestWeight;
    private float candidateColdestRank;
    private float candidateColdestWeight;

    private final Map<Class<?>, List<Pair<CameraShotType, Float>>> candidateHotspotTypes = new HashMap<>();

    public Cameraman() {}

    public Cameraman loadConfig(@NotNull ConfigurationNode configNode) throws ConfigurateException
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
        return this;
    }

    public void destroy()
    {
        if (camera != null)
        {
            camera.remove();
        }
    }

    private ArmorStand camera = null;
    private Hotspot focus = null;
    private float focusTime = 0.0f;
    private CameraShotType cameraShotType = null;
    private final List<Player> outputs = new LinkedList<>();

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
            // update hunger
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
        LazyDirector.GetPlugin().getDirector().detachFromAnyCamera(player);
        outputs.add(player);
        player.setGameMode(GameMode.SPECTATOR);
        player.setSpectatorTarget(camera);
    }

    public void detachCamera(@NotNull Player player)
    {
        if(outputs.contains(player))
        {
            outputs.remove(player);
            player.setSpectatorTarget(null);
        }
    }

    private @NotNull List<Hotspot> getCandidateFocuses()
    {
        List<Hotspot> sortedHotspots = LazyDirector.GetPlugin().getHotspotManager().getAllHotspotsSorted();
        sortedHotspots.removeIf(hotspot -> !candidateHotspotTypes.containsKey(hotspot.getClass()));
        int hottestCandidateIndex = (int) Math.floor(candidateHottestRank * sortedHotspots.size());
        int coldestCandidateIndex = (int) Math.floor(candidateColdestRank * sortedHotspots.size());
        coldestCandidateIndex = Math.min(Math.min(coldestCandidateIndex, hottestCandidateIndex + candidateMaxCount), sortedHotspots.size());
        return sortedHotspots.subList(hottestCandidateIndex, coldestCandidateIndex);
    }

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
