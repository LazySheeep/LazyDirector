package io.lazysheeep.lazydirector.heat;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.util.MathUtils;

public class Heat
{
    private final HeatType type;
    private float value;

    public HeatType getType()
    {
        return type;
    }

    public float getValue()
    {
        return value;
    }

    public Heat(HeatType type)
    {
        this.type = type;
        this.value = 0.0f;
    }

    public void increase(float multiplier)
    {
        value += type.getHeatEachIncrement() * multiplier;
        value = MathUtils.Clamp(value, 0.0f, type.getMaxHeat());
    }

    public void coolDown()
    {
        value -= type.getCoolingRate() / LazyDirector.GetPlugin().getServer().getServerTickManager().getTickRate();
        value = MathUtils.Clamp(value, 0.0f, type.getMaxHeat());
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
        return "Heat{type=" + type + ",value=" + value + "}";
    }
}

