package io.lazysheeep.lazydirector.heat;

import io.lazysheeep.lazydirector.LazyDirector;

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
        if(value > type.getMaxHeat())
        {
            value = type.getMaxHeat();
        }
    }

    public void coolDown()
    {
        value -= type.getCoolingRate() / LazyDirector.getPlugin().getServer().getServerTickManager().getTickRate();
        if(value < 0.0f)
        {
            value = 0.0f;
        }
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
}

