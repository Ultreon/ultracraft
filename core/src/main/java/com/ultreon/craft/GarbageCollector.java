package com.ultreon.craft;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class GarbageCollector {
    private static final Marker MARKER = MarkerFactory.getMarker("GC");
    private final ScheduledExecutorService service = Executors.newScheduledThreadPool(3, r -> {
        Thread thread = new Thread(r);
        thread.setPriority(1);
        return thread;
    });

    public GarbageCollector() {
        this.service.scheduleAtFixedRate(System::gc, 10, 5, TimeUnit.SECONDS);
    }

    public void shutdown() {
        this.service.shutdownNow();

        UltreonCraft.LOGGER.info(MARKER, "Shutting down garbage collector.");
    }
}
