package io.lazysheeep.lazydirector.heat;

import io.lazysheeep.lazydirector.LazyDirector;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;
import java.util.logging.Level;

public class HeatType
{
    private static final List<HeatType> HeatTypes = new ArrayList<>();

    private static void RegisterHeatType(String name, float maxHeat, float heatEachIncrement, float coolingRate)
    {
        HeatType heatType = new HeatType(name, maxHeat, heatEachIncrement, coolingRate);
        if (HeatTypes.contains(heatType))
        {
            LazyDirector.GetPlugin().getLogger().log(Level.WARNING, "Duplicate heatType: " + name);
        }
        HeatTypes.add(heatType);
    }

    public static void LoadConfig(ConfigurationNode configNode) throws ConfigurateException
    {
        for (ConfigurationNode heatTypeNode : configNode.node("basic").childrenList())
        {
            String name = heatTypeNode.node("name").getString();
            if(name == null)
            {
                throw new ConfigurateException(heatTypeNode, "");
            }
            float maxHeat = heatTypeNode.node("maxHeat").getFloat(0.0f);
            float heatEachIncrement = heatTypeNode.node("heatEachIncrement").getFloat(0.0f);
            float coolingRate = heatTypeNode.node("coolingRate").getFloat(0.0f);
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
