package io.lazysheeep.lazydirector.util;

import java.util.ArrayDeque;
import java.util.Deque;

public class FixedSizeQueue<E>
{
    private final int maxSize;
    private final Deque<E> deque;

    public FixedSizeQueue(int maxSize)
    {
        this.maxSize = maxSize;
        this.deque = new ArrayDeque<>(maxSize);
    }

    public void add(E element)
    {
        if (deque.size() >= maxSize)
        {
            deque.removeFirst();
        }
        deque.addLast(element);
    }

    public E removeFirst()
    {
        return deque.removeFirst();
    }

    public E peek()
    {
        return deque.peekFirst();
    }

    public boolean contains(E element)
    {
        return deque.contains(element);
    }

    public int size()
    {
        return deque.size();
    }

    public boolean isEmpty()
    {
        return deque.isEmpty();
    }

    public boolean isFull()
    {
        return deque.size() >= maxSize;
    }
}
