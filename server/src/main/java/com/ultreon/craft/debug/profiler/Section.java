package com.ultreon.craft.debug.profiler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public final class Section {
    private long start;
    private final String name;
    private long end;
    private final Cache<String, Section> data = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build();
    private final Profiler profiler;
    @Nullable
    private Section current;

    public Section(String name, Profiler profiler) {
        this.profiler = profiler;
        this.name = name;
    }

    void startThis() {
        this.start = System.nanoTime();
        this.end = 0;
    }

    void endThis() {
        this.end = System.nanoTime();
    }

    public long getStart() {
        return this.start;
    }

    public long getEnd() {
        return this.end;
    }

    public long getNanos() {
        return this.end - this.start;
    }

    void start(String name) {
        if (this.current != null) {
            this.current.start(name);
            return;
        }
        try {
            this.current = this.data.get(name, () -> new Section(name, this.profiler));
        } catch (ExecutionException ignored) {
            // ignore
        }

        if (this.current == null) return;
        this.current.startThis();
    }

    void end() {
        if (this.current == null) return;
        if (this.current.hasCurrent()) {
            this.current.end();
            return;
        }
        this.current.endThis();
        this.data.put(this.name, this.current);
        this.current = null;
    }

    public Map<String, Section> getData() {
        return this.data.asMap();
    }

    public String getName() {
        return this.name;
    }

    public boolean hasCurrent() {
        return this.current != null;
    }

    @Override
    public String toString() {
        return "Node{" +
                "name='" + this.name + '\'' +
                '}';
    }

    public Map<String, FinishedSection> collect() {
        Map<String, FinishedSection> map = new HashMap<>();
        for (var entry : this.data.asMap().entrySet()) {
            map.put(entry.getKey(), FinishedSection.create(entry.getValue()));
        }

        return map;
    }

    public static class FinishedSection {
        private final Map<String, FinishedSection> data = new HashMap<>();
        private final long nanos;
        private final String name;

        public FinishedSection(Section section) {
            for (var entry : section.getData().entrySet()) {
                this.data.put(entry.getKey(), FinishedSection.create(entry.getValue()));
            }
            this.nanos = section.getNanos();
            this.name = section.getName();
        }

        static FinishedSection create(Section section) {
            return new FinishedSection(section);
        }

        public Map<String, FinishedSection> getData() {
            return Collections.unmodifiableMap(this.data);
        }

        public long getNanos() {
            return this.nanos;
        }

        public String getName() {
            return this.name;
        }
    }
}
