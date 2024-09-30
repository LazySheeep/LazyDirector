package io.lazysheeep.lazydirector.heat;

import io.lazysheeep.lazydirector.LazyDirector;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.logging.Level;

public class HeatType
{
    private static final List<HeatType> HeatTypes = new ArrayList<>();

    public static void RegisterHeatType(String name, float maxHeat, float heatEachIncrement, float coolingRate)
    {
        HeatType heatType = new HeatType(name, maxHeat, heatEachIncrement, coolingRate);
        if (HeatTypes.contains(heatType))
        {
            LazyDirector.GetPlugin().getLogger().log(Level.WARNING, "Duplicate heatType: " + name);
        }
        HeatTypes.add(heatType);
    }

    public static void RegisterHeatTypesFromConfig(ConfigurationSection configSection)
    {
        List<Map<?, ?>> heatTypes = configSection.getMapList("basic");
        for (Map<?, ?> heatTypeMap : heatTypes)
        {
            String name = (String) heatTypeMap.get("name");
            float maxHeat = ((Number) heatTypeMap.get("maxHeat")).floatValue();
            float heatEachIncrement = ((Number) heatTypeMap.get("heatEachIncrement")).floatValue();
            float coolingRate = ((Number) heatTypeMap.get("coolingRate")).floatValue();
            RegisterHeatType(name, maxHeat, heatEachIncrement, coolingRate);
        }
    }

    public static HeatType GetHeatType(String name)
    {
        for (HeatType heatType : HeatTypes)
        {
            if (heatType.name.equals(name))
            {
                return heatType;
            }
        }
        return null;
    }

    private final String name;
    private final float maxHeat;
    private final float heatEachIncrement;
    private final float coolingRate;

    public String getName()
    {
        return name;
    }

    public float getMaxHeat()
    {
        return maxHeat;
    }

    public float getHeatEachIncrement()
    {
        return heatEachIncrement;
    }

    public float getCoolingRate()
    {
        return coolingRate;
    }

    private HeatType(String name, float maxHeat, float heatEachIncrement, float coolingRate)
    {
        this.name = name;
        this.maxHeat = maxHeat;
        this.heatEachIncrement = heatEachIncrement;
        this.coolingRate = coolingRate;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof HeatType)
        {
            return name.equals(((HeatType) obj).name);
        }
        return false;
    }
}
