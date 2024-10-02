package io.lazysheeep.lazydirector.heat;

import io.lazysheeep.lazydirector.LazyDirector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;
import java.util.logging.Level;

/**
 * <p>
 *     Represents the enum of heat types.
 * </p>
 * <p>
 *     This class works as a mutable enum.
 *     <br>
 *     The heat types are registered when loading the configuration.
 * </p>
 */
public class HeatType
{
    private static final List<HeatType> VALUES = new ArrayList<>();

    /**
     * <p>
     *     Get heat type by name.
     * </p>
     * @param name The name of the heat type
     * @return The heat type with the given name, or null if not found
     */
    public static @Nullable HeatType valueOf(@NotNull String name)
    {
        for (HeatType heatType : VALUES)
        {
            if (heatType.name.equals(name))
            {
                return heatType;
            }
        }
        return null;
    }

    /**
     * <p>
     *     Register a heat type.
     * </p>
     * @param name The name of the heat type
     * @param maxHeat The maximum heat of the heat type
     * @param heatEachIncrement The heat each increment of the heat type
     * @param coolingRate The cooling rate of the heat type
     */
    private static void RegisterHeatType(@NotNull String name, float maxHeat, float heatEachIncrement, float coolingRate)
    {
        HeatType heatType = new HeatType(name, maxHeat, heatEachIncrement, coolingRate);
        if (VALUES.contains(heatType))
        {
            LazyDirector.Log(Level.WARNING, "Duplicate heatType: " + name);
            return;
        }
        VALUES.add(heatType);
        LazyDirector.Log(Level.INFO, "Registered heatType: " + name);
    }

    /**
     * <p>
     *     Register heat types from configuration.
     * </p>
     * <p>
     *     Note that all existing heat types will be cleared before loading new configuration.
     * </p>
     * @param configNode The configuration node to load from
     * @throws ConfigurateException
     */
    public static void LoadConfig(ConfigurationNode configNode) throws ConfigurateException
    {
        VALUES.clear();
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
        LazyDirector.Log(Level.INFO, "Loaded " + VALUES.size() + " heatTypes from configuration");
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

    /**
     * <p>
     *     Private constructor.
     * </p>
     * @param name The name of the heat type
     * @param maxHeat The maximum heat of the heat type
     * @param heatEachIncrement The heat each increment of the heat type
     * @param coolingRate The cooling rate of the heat type
     */
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

    @Override
    public String toString()
    {
        return name;
    }
}
