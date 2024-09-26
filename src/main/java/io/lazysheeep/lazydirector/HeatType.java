package io.lazysheeep.lazydirector;

public enum HeatType
{
    STATIC(1000.0f, 0.0f, 1000.0f),
    PLAYER_MOVEMENT(10.0f, 0.1f, 10.0f),
    PLAYER_GATHERING(100.0f, 1.0f, 5.0f),
    PLAYER_BATTLE(200.0f, 2.0f, 10.0f);

    public final float maxHeat;
    public final float coolingRate;
    public final float heatEachCharge;

    HeatType(float maxHeat, float coolingRate, float heatEachCharge)
    {
        this.maxHeat = maxHeat;
        this.coolingRate = coolingRate;
        this.heatEachCharge = heatEachCharge;
    }
}
