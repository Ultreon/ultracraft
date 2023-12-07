package com.ultreon.craft.registry.event;

import com.ultreon.libs.events.v1.Event;
import com.ultreon.craft.registry.Registry;

public class RegistryEvents {

    public static final Event<RegistryDump> REGISTRY_DUMP = Event.create();
    public static final Event<AutoRegister> AUTO_REGISTER = Event.create();

    @FunctionalInterface
    public interface RegistryDump {
        void onRegistryDump();
    }

    @FunctionalInterface
    public interface AutoRegister {
        void onAutoRegister(Registry<?> registry);
    }
}
