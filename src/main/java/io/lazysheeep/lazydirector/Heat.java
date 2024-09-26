package io.lazysheeep.lazydirector;

public class Heat
{
    private final HeatType type;
    private float value;

    public Heat(HeatType type)
    {
        this.type = type;
        this.value = 0.0f;
    }

    public float getValue()
    {
        return value;
    }

    public void charge(float multiplier)
    {
        value += type.heatEachCharge * multiplier;
        if(value > type.maxHeat)
        {
            value = type.maxHeat;
        }
    }

    public void coolDown()
    {
        value -= type.coolingRate;
        if(value < 0.0f)
        {
            value = 0.0f;
        }
    }
}

