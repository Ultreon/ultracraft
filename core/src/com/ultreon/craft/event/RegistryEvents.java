package com.ultreon.craft.event;

import com.ultreon.craft.registry.Registry;

import java.util.*;

public class RegistryEvents {
    private static final Map<Registry<?>, Event<?>> EVENTS = new HashMap<>();

    public static Set<Registry<?>> getEvents() {
        return EVENTS.keySet();
    }

    @SuppressWarnings("unchecked")
    public static <T> Event<RegistryEvent<T>> get(Registry<T> registry) {
        if (EVENTS.containsKey(registry)) {
            return (Event<RegistryEvent<T>>) EVENTS.get(registry);
        }

        Event<RegistryEvent<T>> event = Event.create();
        EVENTS.put(registry, event);
        return event;
    }

    @FunctionalInterface
    public interface RegistryEvent<T> {
        void onRegister(Registry<T> registry);
    }
}
