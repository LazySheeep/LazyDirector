package io.lazysheeep.lazydirector.heat;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 *     Represents a heat with a {@link HeatType} and a value
 * </p>
 * <p>
 *     All heat instances are managed by {@link Hotspot} instances
 * </p>
 */
public class Heat
{
    private final HeatType type;
    private float value;
    private boolean increased;

    public @NotNull HeatType getType()
    {
        return type;
    }

    public float getValue()
    {
        return value;
    }

    /**
     * <p>
     *     Creates a new heat with initial value of 0.0f
     * </p>
     * <p>
     *     Should only be called by {@link Hotspot}
     * </p>
     * @param type The {@link HeatType} of the heat
     */
    public Heat(@NotNull HeatType type)
    {
        this.type = type;
        this.value = 0.0f;
        this.increased = false;
    }

    /**
     * <p>
     *     Increases the heat by the configured amount in the {@link HeatType}
     * </p>
     * @param multiplier The multiplier to apply to the heat increment
     */
    public void heat(float multiplier)
    {
        value += type.getHeatEachIncrement() * multiplier;
        value = MathUtils.Clamp(value, 0.0f, type.getMaxHeat());
        increased = true;
    }

    /**
     * <p>
     *     Decreases the heat by the configured amount in the {@link HeatType}
     * </p>
     * <p>
     *     This method is called once every tick by {@link Hotspot}
     * </p>
     */
    public void coolDown()
    {
        if(!increased)
        {
            value -= type.getCoolingRate() / LazyDirector.GetServerTickRate();
            value = MathUtils.Clamp(value, 0.0f, type.getMaxHeat());
        }
        increased = false;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof Heat otherHeat)
        {
            return type.equals(otherHeat.type) && value == otherHeat.value;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return type + ": " + value;
    }
}

