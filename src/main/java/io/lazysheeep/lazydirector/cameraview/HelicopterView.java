package io.lazysheeep.lazydirector.cameraview;

import io.lazysheeep.lazydirector.LazyDirector;
import io.lazysheeep.lazydirector.camera.Camera;
import io.lazysheeep.lazydirector.hotspot.Hotspot;
import io.lazysheeep.lazydirector.util.MathUtils;
import io.lazysheeep.lazydirector.util.RandomUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;

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
    private final boolean playPropellerSound;
    private final boolean enableVisibilityCheck;
    private final float maxBadViewTime;
    private final int retriesWhenBadView;

    private final float predScale;
    private final float predMinTimeSec;
    private final float predMaxTimeSec;
    private final float predTimeGrowRate;     // seconds of lead gained per second when similar
    private final float predTimeShrinkRate;   // seconds of lead lost per second when dissimilar
    private final float predDirSimilarDeg;    // direction similarity threshold in degrees [0..180]
    private final float predSpeedSimilarRel;  // relative speed diff threshold (|v-p|/max(v,p)) [0..1]
    private final float predGrowAlignPower;   // growth multiplier power vs alignment
    private final float predShrinkMisalignPower; // shrink multiplier power vs misalignment
    private final float predVelAdjustBase;    // base factor for velocity change speed
    private final float predVelAdjustScale;   // extra factor scaling with difference
    private final float predDiffWeightDir;    // weight of direction difference in [0..1]
    private final float predDiffWeightSpeed;  // weight of speed difference in [0..1]
    private final float predLookAtWeight;
    private final boolean showPredictedPath;  // whether to draw the predicted path with particles

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
        playPropellerSound = configNode.node("playPropellerSound").getBoolean(false);
        radarRangeFrontMultiplier = configNode.node("radarRangeFrontMultiplier").getFloat(0.0f);
        enableVisibilityCheck = configNode.node("enableVisibilityCheck").getBoolean(false);
        maxBadViewTime = configNode.node("maxBadViewTime").getFloat(Float.MAX_VALUE);
        retriesWhenBadView = configNode.node("retriesWhenBadView").getInt(1);

        // Prediction config defaults (flat keys; override in config if needed)
        predScale = configNode.node("predictionScale").getFloat(1.0f);
        predMinTimeSec = configNode.node("predictionMinTimeSec").getFloat(0.20f);
        predMaxTimeSec = configNode.node("predictionMaxTimeSec").getFloat(2.50f);
        predTimeGrowRate = configNode.node("predictionTimeGrowRate").getFloat(1.00f);
        predTimeShrinkRate = configNode.node("predictionTimeShrinkRate").getFloat(1.20f);
        predDirSimilarDeg = configNode.node("predictionDirSimilarDeg").getFloat(30.0f);
        predSpeedSimilarRel = configNode.node("predictionSpeedSimilarRel").getFloat(0.25f);
        predGrowAlignPower = configNode.node("predictionGrowAlignPower").getFloat(1.0f);
        predShrinkMisalignPower = configNode.node("predictionShrinkMisalignPower").getFloat(1.0f);
        predVelAdjustBase = configNode.node("predictionVelAdjustBase").getFloat(1.0f);
        predVelAdjustScale = configNode.node("predictionVelAdjustScale").getFloat(2.0f);
        float wDir = configNode.node("predictionDiffWeightDir").getFloat(0.65f);
        float wSpd = configNode.node("predictionDiffWeightSpeed").getFloat(0.35f);
        // normalize weights to sum to 1 to be safe
        float sum = Math.max(1e-6f, wDir + wSpd);
        predDiffWeightDir = wDir / sum;
        predDiffWeightSpeed = wSpd / sum;
        predLookAtWeight = configNode.node("predictionLookAtWeight").getFloat(0.0f);
        showPredictedPath = configNode.node("showPredictedPath").getBoolean(false);
    }

    private Location currentCameraLocation = null;
    private Vector currentCameraVelocity = null;
    private float badViewTimer = 0.0f;
    private boolean cannotFindGoodView = false;

    private int lastRadarScanStep = 0;
    private final boolean[][] radarFlags;

    // Prediction state
    private Location lastFocusLocation = null;
    private Vector predictedVelocity = new Vector(0, 0, 0); // horizontal velocity we expect
    private float predictedTimeSec = 0.0f;

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

        // reset prediction state
        lastFocusLocation = focus.getLocation().clone();
        predictedVelocity = new Vector(0, 0, 0);
        predictedTimeSec = predMinTimeSec;
    }

    @Override
    public void updateCameraLocation(@NotNull Hotspot focus, @NotNull Camera camera)
    {
        Location focusLocation = focus.getLocation();
        float dt = Math.max(0.0f, LazyDirector.GetServerTickDeltaTime());

        // --- Compute target's current horizontal velocity ---
        Vector currentVel = new Vector(0, 0, 0);
        if (lastFocusLocation != null && dt > 1e-4f)
        {
            currentVel = focusLocation.toVector().subtract(lastFocusLocation.toVector()).multiply(1.0 / dt);
        }
        currentVel.setY(0.0); // predict only in horizontal plane; we keep fixed hover height

        // initialize predicted velocity the first time with current
        if (predictedVelocity.lengthSquared() < 1e-8 && currentVel.lengthSquared() > 0)
        {
            predictedVelocity = currentVel.clone();
        }

        // --- Similarity checks ---
        double curSpeed = currentVel.length();
        double predSpeed = predictedVelocity.length();
        double dirCos;
        if (curSpeed > 1e-6 && predSpeed > 1e-6)
        {
            dirCos = currentVel.clone().normalize().dot(predictedVelocity.clone().normalize());
        }
        else
        {
            dirCos = -1.0; // treat as dissimilar when one is near zero
        }
        dirCos = Math.max(-1.0, Math.min(1.0, dirCos));

        double relSpeedDiff = 0.0;
        if (Math.max(curSpeed, predSpeed) > 1e-6)
        {
            relSpeedDiff = Math.abs(curSpeed - predSpeed) / Math.max(curSpeed, predSpeed);
        }
        // direction similarity based on angle threshold in degrees
        double angleDeg = Math.toDegrees(Math.acos(dirCos));
        boolean directionSimilar = angleDeg <= predDirSimilarDeg;
        boolean speedSimilar = relSpeedDiff <= predSpeedSimilarRel;
        boolean similar = directionSimilar && speedSimilar;

        // --- Adjust predicted lead time ---
        // alignment in [0,1]: 1 when perfectly aligned, 0 when opposite or undefined
        float align01 = (float) Math.max(0.0, dirCos);
        if (similar)
        {
            float growthFactor = (float) Math.pow(align01, predGrowAlignPower);
            predictedTimeSec += dt * predTimeGrowRate * growthFactor;
        }
        else
        {
            float misalign01 = 1.0f - align01;
            float shrinkFactor = (float) Math.pow(misalign01, predShrinkMisalignPower);
            predictedTimeSec -= dt * predTimeShrinkRate * shrinkFactor;
        }
        predictedTimeSec = MathUtils.Clamp(predictedTimeSec, predMinTimeSec, predMaxTimeSec);

        // --- Adjust predicted velocity using weighted average with extra factor ---
        // difference score in [0,1]
        float diffDir = 1.0f - align01; // 0 when aligned, 1 when opposite
        float diffSpeed = MathUtils.Clamp((float) relSpeedDiff, 0.0f, 1.0f);
        float diffScore = MathUtils.Clamp(predDiffWeightDir * diffDir + predDiffWeightSpeed * diffSpeed, 0.0f, 1.0f);
        // Scale the contribution of current velocity by a factor that grows with difference
        double effectiveDt = dt * (predVelAdjustBase + predVelAdjustScale * diffScore);
        double denom = predictedTimeSec + effectiveDt;
        if (denom > 1e-6)
        {
            Vector num = predictedVelocity.clone().multiply(predictedTimeSec).add(currentVel.clone().multiply(effectiveDt));
            predictedVelocity = num.multiply(1.0 / denom);
        }
        // keep horizontal
        predictedVelocity.setY(0.0);

        // --- Predicted hover target ---
        Vector predictedOffset = predictedVelocity.clone().multiply(predictedTimeSec * predScale);
        Location predictedLocation = focusLocation.clone().add(predictedOffset);
        Location predictedHoverLocation = predictedLocation.clone().add(0.0, hoverHeight, 0.0);

        // Visualize predicted path (optional)
        if (showPredictedPath)
        {
            MathUtils.ForLine(focusLocation, predictedLocation, 0.5f, (location, progress) ->
            {
                for (Player player : camera.getOutputs())
                {
                    player.spawnParticle(Particle.DUST, location, 1, new Particle.DustOptions(Color.BLUE, 1.0f));
                }
            });
        }

        if (currentCameraLocation == null || badViewTimer >= maxBadViewTime || MathUtils.Distance(currentCameraLocation, focusLocation) > criticalDistance)
        {
            newCameraLocation(focus);
        }

        // calculate main propeller force direction (to predicted hover location)
        Vector chh = predictedHoverLocation.toVector().subtract(currentCameraLocation.toVector()).setY(0.0);
        float chhLength = (float) chh.length();
        Vector chhNorm = chh.clone().normalize();
        Vector vhNorm = currentCameraVelocity.clone().setY(0.0).normalize();
        Vector upDirection = new Vector(0.0, 1.0, 0.0);
        double deltaHeight = predictedHoverLocation.getY() - currentCameraLocation.getY();
        Vector chaseVelocityDirection;
        if (chhLength > hoverRadius)
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
                                                                                      .add(radarRay), 0.2f, (location, progress) -> location.getWorld()
                                                                                                                                            .spawnParticle(Particle.DUST, location, 1, new Particle.DustOptions(Color.RED, 0.4f)));
                    }
                    else if (showRadarRayGreenParticle)
                    {
                        MathUtils.ForLine(currentCameraLocation, currentCameraLocation.clone()
                                                                                      .add(radarRay), 0.2f, (location, progress) -> location.getWorld()
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
        if (showPropellerParticle)
        {
            Location cameraEntityLocation = camera.getCameraEntity().getLocation();
            Location particleStartLocation = cameraEntityLocation.clone()
                                                                 .add(currentCameraVelocity.clone().multiply(-0.15));
            Vector force = currentCameraVelocity.clone()
                                                .subtract(nextCameraVelocity)
                                                .multiply(2.5)
                                                .add(new Vector(0.0, -1.0, 0.0));
            float forceLevel = MathUtils.Clamp((float) force.length() / (maxSpeed / 10.0f), 0.0f, 1.0f);
            Location particleEndLocation = particleStartLocation.clone().add(force);
            List<Player> cameraOutputPlayers = camera.getOutputs();
            List<Player> visiblePlayers = particleStartLocation.getWorld()
                                                               .getPlayers()
                                                               .stream()
                                                               .filter(player -> !cameraOutputPlayers.contains(player))
                                                               .toList();
            float minParticleSize = MathUtils.Lerp(0.5f, 1.0f, forceLevel);
            float maxParticleSize = MathUtils.Lerp(1.5f, 3.0f, forceLevel);
            MathUtils.ForLine(particleStartLocation, particleEndLocation, 0.2f, (location, progress) ->
            {
                float particleSize = MathUtils.Lerp(maxParticleSize, minParticleSize, progress);
                float volume = MathUtils.Lerp(1.5f, 2.0f, forceLevel);
                float pitch = MathUtils.Lerp(0.5f, 2.0f, forceLevel);
                for (Player player : visiblePlayers)
                {
                    player.spawnParticle(Particle.DUST, location, 1, new Particle.DustOptions(Color.WHITE, particleSize));
                    if(playPropellerSound)
                    {
                        player.playSound(cameraEntityLocation, Sound.BLOCK_GRASS_STEP, SoundCategory.RECORDS, volume, pitch);
                    }
                }
            });
        }

        // apply force and velocity
        currentCameraVelocity = nextCameraVelocity;
        currentCameraLocation.add(currentCameraVelocity.clone().multiply(LazyDirector.GetServerTickDeltaTime()));

        // set rotation
        Location lookatLocation = focusLocation.clone().add(predictedOffset.clone().multiply(predLookAtWeight));
        MathUtils.LookAt(currentCameraLocation, lookatLocation);
        if(showPredictedPath)
        {
            for (Player player : camera.getOutputs())
            {
                player.spawnParticle(Particle.DUST, lookatLocation, 1, new Particle.DustOptions(Color.AQUA, 2.0f));
            }
        }

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

        // update last focus position for next tick
        lastFocusLocation = focusLocation.clone();
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
