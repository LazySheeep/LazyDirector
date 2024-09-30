package io.lazysheeep.lazydirector.hotspot;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.heat.Heat;
import io.lazysheeep.lazydirector.heat.HeatType;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class Hotspot implements Comparable<Hotspot>
{
    private final List<Heat> heats = new ArrayList<>();

    public Hotspot() {}

    protected abstract void destroy();

    public abstract boolean isValid();

    protected abstract void additionalUpdate();

    public abstract Location getLocation();

    @Override
    public abstract String toString();

    final void update()
    {
        for(Heat heat : heats)
        {
            heat.coolDown();
        }
        heats.removeIf(heat -> heat.getValue() <= 0);

        additionalUpdate();
    }

    public final float getTotalHeat()
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

    public final void increase(String heatTypeName)
    {
        increase(heatTypeName, 1.0f);
    }

    public final void increase(String heatTypeName, float multiplier)
    {
        // Get the heat type
        HeatType type = HeatType.GetHeatType(heatTypeName);
        if(type == null)
        {
            LazyDirector.GetPlugin().getLogger().warning("Unknown heat type: " + heatTypeName);
            return;
        }
        // Increase the heat of the specified type
        for(Heat heat : heats)
        {
            if(heat.getType().equals(type))
            {
                heat.increase(multiplier);
                return;
            }
        }
        // If not exists, create a new heat of the specified type and increase it
        Heat heat = new Heat(type);
        heats.add(heat);
        heat.increase(multiplier);
    }

    @Override
    public int compareTo(@NotNull Hotspot other)
    {
        return Float.compare(getTotalHeat(), other.getTotalHeat());
    }
}
