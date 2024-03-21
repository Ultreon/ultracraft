package com.ultreon.craft.events;

import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.resources.ResourcePackage;

public class ResourceEvent {
    public static final Event<PackageImported> IMPORTED = Event.create();

    @FunctionalInterface
    public interface PackageImported {
        void onImported(ResourcePackage pkg);
    }
}
