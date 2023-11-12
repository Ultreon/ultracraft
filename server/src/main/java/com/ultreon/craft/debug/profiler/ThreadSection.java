package com.ultreon.craft.debug.profiler;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ThreadSection {
    private final Profiler profiler;
    @Nullable
    private Section current;
    private final Map<String, Section> data = new HashMap<>();
    long lastUpdate = System.currentTimeMillis();

    public ThreadSection(Profiler profiler) {
        this.profiler = profiler;
    }

    void start(String name) {
        if (this.current != null) {
            this.current.start(name);
            return;
        }
        this.current = this.data.computeIfAbsent(name, s -> new Section(name, this.profiler));
        this.current.startThis();
    }

    void end() {
        if (this.current == null) return;
        if (this.current.hasCurrent()) {
            this.current.end();
            return;
        }
        this.current.endThis();
        this.data.put(this.current.getName(), this.current);
        this.current = null;
    }

    public Map<String, Section> getData() {
        return Map.copyOf(this.data);
    }

    private Map<String, Section.FinishedSection> collect() {
        Map<String, Section.FinishedSection> map = new HashMap<>();

        for (Map.Entry<String, Section> entry : this.data.entrySet()) {
            map.put(entry.getKey(), Section.FinishedSection.create(entry.getValue()));
        }

        return map;
    }

    public static class FinishedThreadSection {
        private final Map<String, Section.FinishedSection> data;

        public FinishedThreadSection(ThreadSection section) {
            this.data = section.collect();
        }

        public static FinishedThreadSection create(ThreadSection section) {
            return new FinishedThreadSection(section);
        }

        public Map<String, Section.FinishedSection> getData() {
            return Collections.unmodifiableMap(this.data);
        }
    }
}
