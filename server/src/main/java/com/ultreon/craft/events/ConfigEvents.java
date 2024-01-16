package com.ultreon.craft.events;

import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.util.Env;

public class ConfigEvents {
    public static final Event<Load> LOAD = Event.create();

    @FunctionalInterface
    public interface Load {
        void onConfigLoad(Env Env);
    }
}
