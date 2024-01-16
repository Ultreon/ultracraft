package com.ultreon.xeox.loader;

import com.ultreon.craft.events.api.Event;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptableObject;

/**
 * Xeox Events for Ultracraft.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 * @see XeoxLoader
 */
public class XeoxEvents {
    public static final Event<InitBindings> INIT_BINDINGS = Event.create();
    public static final Event<EventRegistration> EVENT_REGISTRATION = Event.create();
    public static final Event<EventError> EVENT_ERROR = Event.create();

    @FunctionalInterface
    public interface InitBindings {
        void onInitBindings(ScriptableObject scope);
    }

    @FunctionalInterface
    public interface EventRegistration {
        void onRegister();
    }

    @FunctionalInterface
    public interface EventError {
        void onError(RhinoException e);
    }
}
