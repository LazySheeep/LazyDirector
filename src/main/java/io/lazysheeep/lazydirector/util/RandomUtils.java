package io.lazysheeep.lazydirector.util;

import java.util.List;
import java.util.Random;

public class RandomUtils
{
    private static final Random random = new Random();

    public static <T> T RandomPickOne(List<T> list)
    {
        return list.get(Math.abs(random.nextInt()) % list.size());
    }
}
