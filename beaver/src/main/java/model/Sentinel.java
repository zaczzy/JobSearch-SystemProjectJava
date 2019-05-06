package model;

import java.util.concurrent.atomic.AtomicInteger;

public class Sentinel {
    private static Sentinel singleton = null;

    private AtomicInteger count;

    private Sentinel() {
        count = new AtomicInteger(0);
    }

    public static Sentinel getInstance() {
        if(singleton == null) {
            singleton = new Sentinel();
        }
        return singleton;
    }

    public void inc() {
        count.incrementAndGet();
    }

    public void dec() {
        count.decrementAndGet();
    }

    public int getCount() {
        return count.intValue();
    }
}
