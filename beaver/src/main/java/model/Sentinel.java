package model;

import java.util.concurrent.atomic.AtomicInteger;

public class Sentinel {
    private static Sentinel singleton = null;

    public AtomicInteger count;
    public AtomicInteger inBuffer;

    private Sentinel() {
        count = new AtomicInteger(0);
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

    public synchronized boolean finished() {
        return true;
        //return (count.get() == 0 && inBuffer.get() == 0);
    }
}
