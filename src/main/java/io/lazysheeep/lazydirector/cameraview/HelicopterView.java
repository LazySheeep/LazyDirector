package io.lazysheeep.lazydirector.cameraview;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import io.lazysheeep.lazydirector.util.RandomUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.logging.Level;

public class HelicopterView extends CameraView
{
    private final float hoverHeight;
    private final float hoverRadius;
    private final float chaseDistance;
    private final float criticalDistance;
    private final float hoverSpeed;
    private final float CASSpeed;
    private final float maxSpeed;
    private final double radarAngleVertical;
    private final double radarAngleHorizontal;
    private final double radarScanStepVertical;
    private final double radarScanStepHorizontal;
    private final int radarScanStepCountVertical;
    private final int radarScanStepCountHorizontal;
    private final float radarRangeFrontMultiplier;
    private final boolean showRadarRayGreenParticle;
    private final boolean showRadarRayRedParticle;
    private final boolean showPropellerParticle;
    private final boolean enableVisibilityCheck;
    private final float maxBadViewTime;
    private final int retriesWhenBadView;

    private static final float heightTolerance = 2.0f;

    public HelicopterView(@NotNull ConfigurationNode configNode)
    {
        hoverHeight = configNode.node("hoverHeight").getFloat(0.0f);
        hoverRadius = configNode.node("hoverRadius").getFloat(0.0f);
        chaseDistance = configNode.node("chaseDistance").getFloat(0.0f);
        criticalDistance = configNode.node("criticalDistance").getFloat(Float.MAX_VALUE);
        hoverSpeed = configNode.node("hoverSpeed").getFloat(0.0f);
        CASSpeed = configNode.node("CASSpeed").getFloat(0.0f);
        maxSpeed = configNode.node("maxSpeed").getFloat(0.0f);
        radarAngleVertical = Math.toRadians(configNode.node("radarAngleVertical").getDouble(0.0));
        radarAngleHorizontal = Math.toRadians(configNode.node("radarAngleHorizontal").getDouble(0.0));
        radarScanStepVertical = Math.toRadians(configNode.node("radarScanStepVertical").getDouble(0.0));
        radarScanStepHorizontal = Math.toRadians(configNode.node("radarScanStepHorizontal").getDouble(0.0));
        radarScanStepCountVertical = (int) Math.ceil(radarAngleVertical / radarScanStepVertical) + 1;
        radarScanStepCountHorizontal = (int) Math.ceil(radarAngleHorizontal / radarScanStepHorizontal) + 1;
        radarFlags = new boolean[radarScanStepCountVertical][radarScanStepCountHorizontal];
        showRadarRayGreenParticle = configNode.node("showRadarRayGreenParticle").getBoolean(false);
        showRadarRayRedParticle = configNode.node("showRadarRayRedParticle").getBoolean(false);
        showPropellerParticle = configNode.node("showPropellerParticle").getBoolean(false);
        radarRangeFrontMultiplier = configNode.node("radarRangeFrontMultiplier").getFloat(0.0f);
        enableVisibilityCheck = configNode.node("enableVisibilityCheck").getBoolean(false);
        maxBadViewTime = configNode.node("maxBadViewTime").getFloat(Float.MAX_VALUE);
        retriesWhenBadView = configNode.node("retriesWhenBadView").getInt(1);
    }

    private Location currentCameraLocation = null;
    private Vector currentCameraVelocity = null;
    private float badViewTimer = 0.0f;
    private boolean cannotFindGoodView = false;

    private int lastRadarScanStep = 0;
    private final boolean[][] radarFlags;

    @Override
    public @NotNull Location getCurrentCameraLocation()
    {
        if (currentCameraLocation == null)
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
        if (currentCameraLocation == null)
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

        // calculate main propeller force direction
        Vector chh = hoverLocation.toVector().subtract(currentCameraLocation.toVector()).setY(0.0);
        float chhLength = (float)chh.length();
        Vector chhNorm = chh.clone().normalize();
        Vector vhNorm = currentCameraVelocity.clone().setY(0.0).normalize();
        Vector upDirection = new Vector(0.0, 1.0, 0.0);
        double deltaHeight = hoverLocation.getY() - currentCameraLocation.getY();
        Vector chaseVelocityDirection;
        if(chhLength > hoverRadius)
        {
            double chhvTheta = Math.asin(hoverRadius / chhLength);
            double chhvCross = chhNorm.getX() * vhNorm.getZ() - chhNorm.getZ() * vhNorm.getX();
            chaseVelocityDirection = chhNorm.clone().rotateAroundY(chhvTheta * (chhvCross < 0.0 ? 1.0 : -1.0));
        }
        else
        {
            chaseVelocityDirection = vhNorm.clone();
        }
        float chaseSpeed = MathUtils.squareMap(chhLength, hoverRadius, chaseDistance, hoverSpeed, maxSpeed);

        if (deltaHeight > heightTolerance)
        {
            chaseVelocityDirection.add(upDirection);
        }
        else if (deltaHeight < -heightTolerance)
        {
            chaseVelocityDirection.subtract(upDirection);
        }
        chaseVelocityDirection.normalize();
        Vector chaseVelocity = chaseVelocityDirection.clone().multiply(chaseSpeed);

        // CAS
        Vector forwardDirection = currentCameraVelocity.clone().normalize();
        Vector rightDirection = currentCameraVelocity.getCrossProduct(upDirection).normalize();
        Vector CASDirection = new Vector(0.0, 0.0, 0.0);
        boolean CASFlag = false;
        for (int radarScanHorizontal = 0; radarScanHorizontal < radarScanStepCountHorizontal; radarScanHorizontal++)
        {
            for (int radarScanVertical = 0; radarScanVertical < radarScanStepCountVertical; radarScanVertical++)
            {
                // calculate radar ray
                double radarAngleVerticalOffset = radarScanStepVertical * radarScanVertical - radarAngleVertical / 2.0;
                double radarAngleHorizontalOffset = radarScanStepHorizontal * radarScanHorizontal - radarAngleHorizontal / 2.0;
                Vector radarRayDirection = forwardDirection.clone()
                                                           .rotateAroundAxis(rightDirection, radarAngleVerticalOffset)
                                                           .rotateAroundY(radarAngleHorizontalOffset);
                // update radar flags
                if (radarScanHorizontal == lastRadarScanStep)
                {
                    double theta = Math.max(Math.abs(radarAngleVerticalOffset), Math.abs(radarAngleHorizontalOffset));
                    Vector radarRay = radarRayDirection.clone()
                                                       .multiply(1.0f + (currentCameraVelocity.length() * radarRangeFrontMultiplier * Math.pow(Math.max(Math.cos(theta) - 0.5, 0.0), 3)) + (2.0 * Math.sin(theta)));
                    boolean rayResult = (MathUtils.RayTrace(currentCameraLocation, radarRay) != null);
                    radarFlags[radarScanVertical][radarScanHorizontal] = rayResult;
                    if (rayResult && showRadarRayRedParticle)
                    {
                        MathUtils.ForLine(currentCameraLocation, currentCameraLocation.clone()
                                                                                      .add(radarRay), 0.2f, location -> location.getWorld()
                                                                                                                                .spawnParticle(Particle.DUST, location, 1, new Particle.DustOptions(Color.RED, 0.4f)));
                    }
                    else if(showRadarRayGreenParticle)
                    {
                        MathUtils.ForLine(currentCameraLocation, currentCameraLocation.clone()
                                                                                      .add(radarRay), 0.2f, location -> location.getWorld()
                                                                                                                                .spawnParticle(Particle.DUST, location, 1, new Particle.DustOptions(Color.GREEN, 0.4f)));
                    }
                }
                // apply direction
                if (radarFlags[radarScanVertical][radarScanHorizontal])
                {
                    CASDirection.subtract(radarRayDirection);
                    CASFlag = true;
                }
            }
        }
        lastRadarScanStep = (lastRadarScanStep + 1) % radarScanStepCountHorizontal;

        Vector CASVelocity = CASFlag ? CASDirection.clone().normalize().multiply(CASSpeed) : null;

        Vector targetVelocity = CASFlag ? CASVelocity : chaseVelocity;

        // calculate resultant force
        Vector nextCameraVelocity = MathUtils.Lerp(currentCameraVelocity, targetVelocity, 0.05f);
        if(showPropellerParticle)
        {
            MathUtils.ForLine(currentCameraLocation,
                              currentCameraLocation.clone().add(currentCameraVelocity.clone().subtract(nextCameraVelocity).multiply(2.0)),
                              0.2f,
                              location -> location.getWorld().spawnParticle(Particle.DUST, location, 1, new Particle.DustOptions(Color.WHITE, 2.0f)));
        }

        // apply force and velocity
        currentCameraVelocity = nextCameraVelocity;
        currentCameraLocation.add(currentCameraVelocity.clone().multiply(LazyDirector.GetServerTickDeltaTime()));

        // set rotation
        MathUtils.LookAt(currentCameraLocation, focusLocation);

        // check view goodness
        if (chhLength < (chaseDistance + criticalDistance) / 2 && (!enableVisibilityCheck || MathUtils.IsVisible(currentCameraLocation, focus.getLocation())))
        {
            badViewTimer = 0.0f;
        }
        else if (!currentCameraLocation.getBlock().getType().isAir())
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
}
