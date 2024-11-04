package io.lazysheeep.lazydirector.hotspot;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.director.Cameraman;
import io.lazysheeep.lazydirector.heat.Heat;
import io.lazysheeep.lazydirector.heat.HeatType;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * <p>
 *     Hotspot is the container of heats.
 *     It is a point of interest in the world that can be heated up by actor activities,
 *     and can be focused on by the {@link Cameraman}.
 * </p>
 * <p>
 *     This is the base class of all the hotspots, dealing with the common heat management behaviors.
 * </p>
 */
public abstract class Hotspot implements Comparable<Hotspot>
{
    Hotspot() {}

    /**
     * Destroy the hotspot.
     */
    final void destroy()
    {
        additionalDestroy();
        heats.clear();
    }

    /**
     * Additional destroy process for the subclass.
     */
    protected abstract void additionalDestroy();

    /**
     * <p>
     *     Check if the hotspot is valid.
     * </p>
     * <p>
     *     The hotspot should be valid after creation, and become invalid after being destroyed.
     * </p>
     * @return Whether the hotspot is valid
     */
    public abstract boolean isValid();

    private final List<Heat> heats = new ArrayList<>();

    /**
     * <p>
     *     Get the total heat value of the hotspot.
     * </p>
     * @return The heat of the hotspot
     */
    public float getHeat()
    {
        float totalHeat = 0;
        for(Heat heat : heats)
        {
            if(heat != null)
            {
                totalHeat += heat.getValue();
            }
        }
        return totalHeat;
    }

    /**
     * <p>
     *     Get the heat value of the specified heat type.
     * </p>
     * @param heatTypeName The name of the heat type
     * @return The heat of the specified type
     */
    public final float getHeat(String heatTypeName)
    {
        HeatType type = HeatType.valueOf(heatTypeName);
        if(type == null)
        {
            LazyDirector.Log(Level.WARNING, "Unknown heat type: " + heatTypeName);
            return 0.0f;
        }
        for(Heat heat : heats)
        {
            if(heat.getType().equals(type))
            {
                return heat.getValue();
            }
        }
        return 0.0f;
    }

    /**
     * <p>
     *     Get the location of the hotspot.
     * </p>
     * @return The location of the hotspot
     */
    public abstract Location getLocation();

    /**
     * <p>
     *     Increase the heat with the specified heat type.
     * </p>
     * @param heatTypeName The name of the heat type
     * @return
     * <p>
     *     0 if the heat is increased successfully
     *     <br/>
     *     1 if the heat type is unknown
     * </p>
     */
    public int heat(String heatTypeName)
    {
        return heat(heatTypeName, 1.0f);
    }

    /**
     * <p>
     *     Increase the heat with the specified heat type and multiplier.
     * </p>
     * @param heatTypeName The name of the heat type
     * @param multiplier The multiplier to apply to the heat increment
     * @return
     * <p>
     *     0 if the heat is increased successfully
     *     <br/>
     *     1 if the heat type is unknown
     * </p>
     */
    public int heat(String heatTypeName, float multiplier)
    {
        // Get the heat type
        HeatType type = HeatType.valueOf(heatTypeName);
        if(type == null)
        {
            LazyDirector.Log(Level.WARNING, "Unknown heat type: " + heatTypeName);
            return 1;
        }
        // Increase the heat of the specified type
        for(Heat heat : heats)
        {
            if(heat.getType().equals(type))
            {
                heat.heat(multiplier);
                return 0;
            }
        }
        // If not exists, create a new heat of the specified type and increase it
        Heat heat = new Heat(type);
        heats.add(heat);
        heat.heat(multiplier);
        return 0;
    }

    /**
     * <p>
     *     Call the additional update method and cool down all the heats.
     * </p>
     * <p>
     *     This method is called every tick by {@link HotspotManager}.
     * </p>
     */
    final void update()
    {
        additionalUpdate();

        for(Heat heat : heats)
        {
            heat.coolDown();
        }

        heats.removeIf(heat -> heat.getValue() <= 0 && heat.getType().getCoolingRate() > 0);
    }

    /**
     * Additional update process for the subclass.
     */
    protected abstract void additionalUpdate();

    @Override
    public int compareTo(@NotNull Hotspot other)
    {
        return Float.compare(getHeat(), other.getHeat());
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        var className = this.getClass().toString().split("\\.");
        stringBuilder.append("[").append(className[className.length - 1]).append("]\n");
        stringBuilder.append("  ").append(additionalToString()).append("\n");
        stringBuilder.append("  ").append("Heat: ").append(getHeat());
        for(Heat heat : heats)
        {
            stringBuilder.append("\n").append("    ").append(heat);
        }
        return stringBuilder.toString();
    }

    protected abstract String additionalToString();
}
