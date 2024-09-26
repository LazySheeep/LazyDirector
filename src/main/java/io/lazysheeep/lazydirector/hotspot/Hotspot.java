package io.lazysheeep.lazydirector.hotspot;

import io.lazysheeep.lazydirector.Heat;
import io.lazysheeep.lazydirector.HeatType;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public abstract class Hotspot implements Comparable<Hotspot>
{
    protected final Heat[] heats = new Heat[HeatType.values().length];

    public Hotspot() {}

    protected abstract void destroy();

    public abstract void update();

    public abstract Location getLocation();

    public float getTotalHeat()
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

    public void charge(HeatType type)
    {
        charge(type, 1.0f);
    }

    public void charge(HeatType type, float multiplier)
    {
        Heat heat = heats[type.ordinal()];
        if(heat == null)
        {
            heat = new Heat(type);
            heats[type.ordinal()] = heat;
        }
        heat.charge(multiplier);
    }

    @Override
    public int compareTo(@NotNull Hotspot other)
    {
        return Float.compare(getTotalHeat(), other.getTotalHeat());
    }
}
