package io.lazysheeep.lazydirector.util;

import java.util.List;
import java.util.Random;

public class RandomUtils
{
    private static final Random random = new Random();

    public static int NextInt(int min, int max)
    {
        return random.nextInt(max - min + 1) + min;
    }

    public static float NextFloat(float min, float max)
    {
        return random.nextFloat() * (max - min) + min;
    }

    public static double NextDouble(double min, double max)
    {
        return random.nextDouble() * (max - min) + min;
    }

    public static <T> T PickOne(List<T> list)
    {
        return list.get(Math.abs(random.nextInt()) % list.size());
    }
}
