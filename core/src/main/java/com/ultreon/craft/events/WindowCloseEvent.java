package com.ultreon.craft.events;

import com.ultreon.libs.events.v1.Event;
import com.ultreon.libs.events.v1.EventResult;

public class WindowCloseEvent {

    public static final Event<Type> EVENT = Event.withResult();

    @FunctionalInterface
    public interface Type {
        EventResult onWindowClose();
    }
}
