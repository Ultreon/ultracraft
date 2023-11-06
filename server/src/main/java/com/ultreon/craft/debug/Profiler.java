package com.ultreon.craft.debug;

import org.intellij.lang.annotations.RegExp;

import javax.annotation.concurrent.ThreadSafe;
import java.io.Closeable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@ThreadSafe
public final class Profiler implements Closeable {
    @RegExp
    static final String SECTION_REGEX = "[a-zA-Z0-9_ \\[\\],.\\-]+";
    private final ConcurrentMap<Thread, ThreadSection> threads = new ConcurrentHashMap<>();
    private final ConcurrentMap<Thread, ThreadSection.FinishedThreadSection> finished = new ConcurrentHashMap<>();
    private boolean profiling;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Profiler() {

    }

    private void start(String name) {
        if (!this.profiling) {
            return;
        }
        var threadSection = this.threads.computeIfAbsent(Thread.currentThread(), thread -> new ThreadSection(this));
        threadSection.start(name);
    }

    private void end() {
        var cur = Thread.currentThread();
        if (this.threads.containsKey(cur)) this.threads.get(cur).end();
        else this.threads.put(cur, new ThreadSection(this));
    }

    public void update() {
        Thread cur = Thread.currentThread();
        var threadSection = this.threads.computeIfAbsent(cur, thread -> new ThreadSection(this));
        if (threadSection.lastUpdate + 2000 < System.currentTimeMillis()) {
            this.finished.put(cur, ThreadSection.FinishedThreadSection.create(threadSection));
            threadSection.lastUpdate = System.currentTimeMillis();
        }
    }

    public ProfileData collect() {
        return new ProfileData(this.finished);
    }

    public void section(String name, Runnable block) {
        this.start(name);
        block.run();
        this.end();
    }

    public boolean isProfiling() {
        return this.profiling;
    }

    public void setProfiling(boolean profiling) {
        this.profiling = profiling;
    }

    public void close() {
        this.scheduler.shutdown();
    }
}
