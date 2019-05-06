package model;

import java.util.concurrent.atomic.AtomicInteger;

public class Sentinel {
    private static Sentinel singleton = null;

    private AtomicInteger count;
    private AtomicInteger inBuffer;

    private Sentinel() {
        count = new AtomicInteger(1);
        inBuffer = new AtomicInteger(0);
    }

    public static synchronized Sentinel getInstance() {
        if(singleton == null) {
            singleton = new Sentinel();
        }
        return singleton;
    }

    public void setWorking(boolean working) {
        if(working) {
            count.incrementAndGet();
        } else {
            count.decrementAndGet();
        }
    }

    public void setBuffer(boolean add) {
        if(add) {
            inBuffer.incrementAndGet();
        } else {
            inBuffer.decrementAndGet();
        }
    }

    public boolean finished() {
        return (count.intValue() == 0 && inBuffer.intValue() == 0);
    }
}
