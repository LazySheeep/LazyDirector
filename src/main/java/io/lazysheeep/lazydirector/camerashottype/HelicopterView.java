package io.lazysheeep.lazydirector.camerashottype;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class HelicopterView extends CameraView
{
    private final float minEnginePower = 10.0f;
    private final float maxEnginePower = 100.0f;
    private final float minPowerDistance = 16.0f;
    private final float maxPowerDistance = 64.0f;
    private final float helicopterMass = 500.0f;
    private final float fragFactor = 5.0f;

    private final float minHeight = 16.0f;
    private final float maxHeight = 64.0f;
    private final float prefHeight = 32.0f;
    private final float hoverRadius = 16.0f;

    private Location helicopterLocation;
    private Vector helicopterVelocity;

    @Override
    public @Nullable Location updateCameraLocation(@NotNull Hotspot focus)
    {
        Location focusLocation = focus.getLocation();

        if (helicopterLocation == null || helicopterVelocity == null || MathUtils.Distance(helicopterLocation, focusLocation) > 256.0)
        {
            initHelicopter(focus);
        }

        Vector resultantForce = new Vector(0.0, 0.0, 0.0);
        resultantForce.add(helicopterVelocity.clone().multiply(-fragFactor));

        Vector f = focusLocation.toVector().subtract(helicopterLocation.toVector()).setY(0.0);
        Vector fNorm = f.clone().normalize();
        Vector vNorm = helicopterVelocity.clone().setY(0.0).normalize();
        Vector propellerForceDirection;

        double theta = Math.asin(hoverRadius / f.length());
        if(fNorm.angle(vNorm) > theta + Math.toRadians(2.0))
        {
            propellerForceDirection = vNorm.rotateAroundY((Math.PI / 2.0) * (fNorm.getX() * vNorm.getZ() - fNorm.getZ() * vNorm.getX() > 0.0 ? 1.0 : -1.0));
        }
        else if(fNorm.angle(vNorm) < theta - Math.toRadians(2.0))
        {
            propellerForceDirection = vNorm.rotateAroundY((Math.PI / 2.0) * (fNorm.getX() * vNorm.getZ() - fNorm.getZ() * vNorm.getX() < 0.0 ? 1.0 : -1.0));
        }
        else
        {
            propellerForceDirection = vNorm;
        }

        float enginePower = MathUtils.Map((float)f.length(), minPowerDistance, maxPowerDistance, minEnginePower, maxEnginePower);
        enginePower = MathUtils.Clamp(enginePower, minEnginePower, maxEnginePower);

        double vc = Math.abs(helicopterVelocity.dot(propellerForceDirection));
        double propellerForce = (-vc + Math.sqrt(vc * vc + 2 * LazyDirector.GetServerTickDeltaTime() * enginePower / helicopterMass)) / (LazyDirector.GetServerTickDeltaTime() / helicopterMass);
        resultantForce.add(propellerForceDirection.clone().multiply(propellerForce));

        helicopterVelocity.add(resultantForce.clone().multiply(1.0f / helicopterMass));
        helicopterLocation.add(helicopterVelocity.clone().multiply(LazyDirector.GetServerTickDeltaTime()));

        MathUtils.LookAt(helicopterLocation, focusLocation);

        return helicopterLocation;
    }

    @Override
    public void reset()
    {
        helicopterLocation = null;
        helicopterVelocity = null;
    }

    private void initHelicopter(@NotNull Hotspot focus)
    {
        helicopterLocation = focus.getLocation().clone().add(5.0, prefHeight, 5.0);
        helicopterVelocity = new Vector(0.0, 0.0, 0.2);
    }
}
