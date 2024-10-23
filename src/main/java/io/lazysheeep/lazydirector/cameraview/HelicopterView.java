package io.lazysheeep.lazydirector.cameraview;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

public class HelicopterView extends CameraView
{
    private final float minEnginePower;
    private final float maxEnginePower;
    private final float minPowerDistance;
    private final float maxPowerDistance;
    private final float helicopterMass;
    private final float fragFactor;
    private final float hoverHeight;
    private final float hoverRadius;

    private static final double yawTolerance = Math.toRadians(2.0);
    private static final float heightTolerance = 2.0f;

    private Location helicopterLocation;
    private Vector helicopterVelocity;

    public HelicopterView(@NotNull ConfigurationNode configNode)
    {
        minEnginePower = configNode.node("minEnginePower").getFloat(0.0f);
        maxEnginePower = configNode.node("maxEnginePower").getFloat(0.0f);
        minPowerDistance = configNode.node("minPowerDistance").getFloat(0.0f);
        maxPowerDistance = configNode.node("maxPowerDistance").getFloat(0.0f);
        helicopterMass = configNode.node("helicopterMass").getFloat(0.0f);
        fragFactor = configNode.node("fragFactor").getFloat(0.0f);
        hoverHeight = configNode.node("hoverHeight").getFloat(0.0f);
        hoverRadius = configNode.node("hoverRadius").getFloat(0.0f);

        reset();
    }

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
        helicopterVelocity = new Vector(0.0, 0.0, 0.0);
    }
}
