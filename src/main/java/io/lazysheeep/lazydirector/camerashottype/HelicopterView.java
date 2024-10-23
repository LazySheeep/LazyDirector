package io.lazysheeep.lazydirector.camerashottype;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HelicopterView extends CameraView
{
    private final float minEnginePower = 10.0f;
    private final float maxEnginePower = 200.0f;
    private final float minPowerDistance = 16.0f;
    private final float maxPowerDistance = 64.0f;
    private final float helicopterMass = 500.0f;
    private final float fragFactor = 5.0f;

    private final float hoverHeight = 32.0f;
    private final float hoverRadius = 16.0f;
    private final double yawTolerance = Math.toRadians(2.0);
    private final float heightTolerance = 2.0f;

    private Location helicopterLocation;
    private Vector helicopterVelocity;

    @Override
    public @Nullable Location updateCameraLocation(@NotNull Hotspot focus)
    {
        Location focusLocation = focus.getLocation();
        Location hoverLocation = focusLocation.clone().add(0.0, hoverHeight, 0.0);

        if (helicopterLocation == null || helicopterVelocity == null || MathUtils.Distance(helicopterLocation, hoverLocation) > 256.0)
        {
            initHelicopter(focus);
        }

        // calculate propeller force direction
        Vector f = hoverLocation.toVector().subtract(helicopterLocation.toVector()).setY(0.0);
        Vector fNorm = f.clone().normalize();
        Vector vNorm = helicopterVelocity.clone().setY(0.0).normalize();
        Vector propellerForceDirection;

        double theta = Math.asin(hoverRadius / f.length());
        if(fNorm.angle(vNorm) > theta + yawTolerance)
        {
            propellerForceDirection = vNorm.rotateAroundY((Math.PI / 3.0) * (fNorm.getX() * vNorm.getZ() - fNorm.getZ() * vNorm.getX() > 0.0 ? 1.0 : -1.0));
        }
        else if(fNorm.angle(vNorm) < theta - yawTolerance)
        {
            propellerForceDirection = vNorm.rotateAroundY((Math.PI / 3.0) * (fNorm.getX() * vNorm.getZ() - fNorm.getZ() * vNorm.getX() < 0.0 ? 1.0 : -1.0));
        }
        else
        {
            propellerForceDirection = vNorm;
        }

        double deltaHeight = hoverLocation.getY() - helicopterLocation.getY();
        if(deltaHeight > heightTolerance)
        {
            propellerForceDirection.setY(1.0).normalize();
        }
        else if(deltaHeight < -heightTolerance)
        {
            propellerForceDirection.setY(-1.0).normalize();
        }

        // calculate propeller force
        float enginePower = MathUtils.Map((float)f.length(), minPowerDistance, maxPowerDistance, minEnginePower, maxEnginePower);
        enginePower = MathUtils.Clamp(enginePower, minEnginePower, maxEnginePower);

        double vc = Math.abs(helicopterVelocity.dot(propellerForceDirection));
        double propellerForce = (-vc + Math.sqrt(vc * vc + 2 * LazyDirector.GetServerTickDeltaTime() * enginePower / helicopterMass)) / (LazyDirector.GetServerTickDeltaTime() / helicopterMass);

        // calculate resultant force
        Vector resultantForce = new Vector(0.0, 0.0, 0.0);
        resultantForce.add(helicopterVelocity.clone().multiply(-fragFactor));
        resultantForce.add(propellerForceDirection.clone().multiply(propellerForce));

        // apply force
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
        helicopterLocation = focus.getLocation().clone().add(5.0, hoverHeight, 5.0);
        helicopterVelocity = new Vector(0.0, 0.0, 0.2);
    }
}
