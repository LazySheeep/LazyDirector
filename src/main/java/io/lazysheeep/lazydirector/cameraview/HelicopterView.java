package io.lazysheeep.lazydirector.cameraview;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import io.lazysheeep.lazydirector.util.RandomUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

public class HelicopterView extends CameraView
{
    private final float minEnginePower;
    private final float maxEnginePower;
    private final float minPowerDistance;
    private final float maxPowerDistance;
    private final float criticalDistance;
    private final float helicopterMass;
    private final float fragFactor;
    private final float hoverRadius;
    private final float hoverHeight;
    private final float minDistanceToDownwardTerrain;
    private final float minDistanceToUpwardTerrain;
    private final boolean enableVisibilityCheck;
    private final float maxBadViewTime;
    private final int retriesWhenBadView;

    private static final double yawTolerance = Math.toRadians(2.0);
    private static final float heightTolerance = 2.0f;

    public HelicopterView(@NotNull ConfigurationNode configNode)
    {
        minEnginePower = configNode.node("minEnginePower").getFloat(0.0f);
        maxEnginePower = configNode.node("maxEnginePower").getFloat(0.0f);
        minPowerDistance = configNode.node("minPowerDistance").getFloat(0.0f);
        maxPowerDistance = configNode.node("maxPowerDistance").getFloat(Float.MAX_VALUE);
        criticalDistance = configNode.node("criticalDistance").getFloat(Float.MAX_VALUE);
        helicopterMass = configNode.node("helicopterMass").getFloat(0.0f);
        fragFactor = configNode.node("fragFactor").getFloat(0.0f);
        hoverRadius = configNode.node("hoverRadius").getFloat(0.0f);
        hoverHeight = configNode.node("hoverHeight").getFloat(0.0f);
        minDistanceToDownwardTerrain = configNode.node("minDistanceToDownwardTerrain").getFloat(0.0f);
        minDistanceToUpwardTerrain = configNode.node("minDistanceToUpwardTerrain").getFloat(0.0f);
        enableVisibilityCheck = configNode.node("enableVisibilityCheck").getBoolean(false);
        maxBadViewTime = configNode.node("maxBadViewTime").getFloat(Float.MAX_VALUE);
        retriesWhenBadView = configNode.node("retriesWhenBadView").getInt(1);
    }

    private Location currentCameraLocation = null;
    private Vector currentCameraVelocity = null;
    private float badViewTimer = 0.0f;
    private boolean cannotFindGoodView = false;

    @Override
    public @NotNull Location getCurrentCameraLocation()
    {
        if(currentCameraLocation == null)
        {
            throw new IllegalStateException("Camera location is not initialized.");
        }
        return currentCameraLocation;
    }

    @Override
    public void newCameraLocation(@NotNull Hotspot focus)
    {
        int retries = 0;
        Location newCameraLocation = null;
        cannotFindGoodView = true;
        while (retries < retriesWhenBadView)
        {
            newCameraLocation = focus.getLocation()
                                         .clone()
                                         .add(RandomUtils.NextDouble(-hoverRadius, hoverRadius), hoverHeight, RandomUtils.NextDouble(-hoverRadius, hoverRadius));
            if (!enableVisibilityCheck || MathUtils.IsVisible(newCameraLocation, focus.getLocation()))
            {
                currentCameraLocation = newCameraLocation;
                currentCameraVelocity = new Vector(RandomUtils.NextDouble(0.0, 0.5), 0.0, RandomUtils.NextDouble(0.0, 0.5));
                badViewTimer = 0.0f;
                cannotFindGoodView = false;
                break;
            }
            retries++;
        }
        if(currentCameraLocation == null)
        {
            currentCameraLocation = newCameraLocation;
            currentCameraVelocity = new Vector(RandomUtils.NextDouble(0.0, 0.5), 0.0, RandomUtils.NextDouble(0.0, 0.5));
        }
    }

    @Override
    public void updateCameraLocation(@NotNull Hotspot focus)
    {
        Location focusLocation = focus.getLocation();
        Location hoverLocation = focusLocation.clone().add(0.0, hoverHeight, 0.0);

        if (currentCameraLocation == null || badViewTimer >= maxBadViewTime || MathUtils.Distance(currentCameraLocation, focusLocation) > criticalDistance)
        {
            newCameraLocation(focus);
        }

        // calculate propeller force direction
        Vector f = hoverLocation.toVector().subtract(currentCameraLocation.toVector()).setY(0.0);
        double fLength = f.length();
        Vector fNorm = f.clone().normalize();
        Vector vNorm = currentCameraVelocity.clone().setY(0.0).normalize();
        Vector propellerForceDirection;

        double theta = Math.asin(hoverRadius / fLength);
        double cross = fNorm.getX() * vNorm.getZ() - fNorm.getZ() * vNorm.getX();
        double turningAngle = (vNorm.angle(fNorm) < Math.toRadians(100.0)) ? Math.toRadians(60.0) : Math.toRadians(120.0);
        if (fNorm.angle(vNorm) > theta + yawTolerance)
        {
            propellerForceDirection = vNorm.clone().rotateAroundY(turningAngle * (cross > 0.0 ? 1.0 : -1.0));
        }
        else if (fNorm.angle(vNorm) < theta - yawTolerance)
        {
            propellerForceDirection = vNorm.clone().rotateAroundY(turningAngle * (cross < 0.0 ? 1.0 : -1.0));
        }
        else
        {
            propellerForceDirection = vNorm.clone();
        }

        boolean needGoUp = false;
        boolean needGoDown = false;
        double deltaHeight = hoverLocation.getY() - currentCameraLocation.getY();
        if (terrainCollision(currentCameraLocation, -(int) minDistanceToDownwardTerrain))
        {
            needGoUp = true;
        }
        else if (deltaHeight < -heightTolerance)
        {
            needGoDown = true;
        }
        if (terrainCollision(currentCameraLocation, (int) minDistanceToUpwardTerrain))
        {
            needGoDown = true;
        }
        else if (deltaHeight > heightTolerance)
        {
            needGoUp = true;
        }

        if (needGoUp && !needGoDown)
        {
            propellerForceDirection.setY(1.0).normalize();
        }
        else if (!needGoUp && needGoDown)
        {
            propellerForceDirection.setY(-1.0).normalize();
        }

        // calculate propeller force
        float enginePower = MathUtils.squareMap((float) fLength, minPowerDistance, maxPowerDistance, minEnginePower, maxEnginePower);
        enginePower = MathUtils.Clamp(enginePower, minEnginePower, maxEnginePower);

        double vc = currentCameraVelocity.dot(propellerForceDirection);
        double deltaT = LazyDirector.GetServerTickDeltaTime();
        double t0, t1;
        if (vc > 0)
        {
            t0 = (helicopterMass * vc * vc) / (2.0 * enginePower);
            t1 = t0 + deltaT;
        }
        else
        {
            t1 = (helicopterMass * vc * vc) / (2.0 * enginePower);
            t0 = t1 - deltaT;
        }
        double propellerForce = (enginePower * deltaT) / (Math.sqrt((8.0 * enginePower) / (9.0 * helicopterMass)) * (Math.pow(t1, 1.5) - (t0 > 0 ? 1 : -1) * Math.pow(Math.abs(t0), 1.5)));

        // calculate resultant force
        Vector resultantForce = new Vector(0.0, 0.0, 0.0);
        resultantForce.add(currentCameraVelocity.clone().multiply(-fragFactor));
        resultantForce.add(propellerForceDirection.clone().multiply(propellerForce));

        // apply force
        currentCameraVelocity.add(resultantForce.clone().multiply(1.0f / helicopterMass));
        currentCameraLocation.add(currentCameraVelocity.clone().multiply(LazyDirector.GetServerTickDeltaTime()));

        // set rotation
        MathUtils.LookAt(currentCameraLocation, focusLocation);

        // check view goodness
        if (fLength < maxPowerDistance && (!enableVisibilityCheck || MathUtils.IsVisible(currentCameraLocation, focus.getLocation())))
        {
            badViewTimer = 0.0f;
        }
        else if(!currentCameraLocation.getBlock().getType().isAir())
        {
            badViewTimer = maxBadViewTime;
        }
        else
        {
            badViewTimer += LazyDirector.GetServerTickDeltaTime();
        }
    }

    @Override
    public boolean isViewGood()
    {
        return badViewTimer <= 0.0f;
    }

    @Override
    public boolean cannotFindGoodView()
    {
        return cannotFindGoodView;
    }

    private static boolean terrainCollision(@NotNull Location location, int rangeY)
    {
        Location loc = location.clone();
        if (rangeY > 0)
        {
            for (int y = 0; y < rangeY; y++)
            {
                if (!loc.add(0.0, 1.0, 0.0).getBlock().getType().isAir())
                {
                    return true;
                }
            }
        }
        else
        {
            for (int y = 0; y > rangeY; y--)
            {
                if (!loc.add(0.0, -1.0, 0.0).getBlock().getType().isAir())
                {
                    return true;
                }
            }
        }
        return false;
    }
}
