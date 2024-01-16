package com.ultreon.xeox.loader;

import org.mozilla.javascript.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a JavaScript event emitter.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 * @see XeoxLoader
 */
public class JSEvents {
    private static final List<String> REGISTRY = new ArrayList<>();
    private static final List<JSEvents> INSTANCES = new ArrayList<>();
    private final Scriptable scope;
    private final Context cx;
    private final Map<String, List<Function>> events = new ConcurrentHashMap<>();
    private Thread thread;

    /**
     * Creates a new JSEvents instance.
     * 
     * @param scope the scope to use
     * @param cx    the context to use
     */
    public JSEvents(Scriptable scope, Context cx) {
        this.scope = scope;
        this.cx = cx;
        this.thread = Thread.currentThread();

        INSTANCES.add(this);
    }

    /**
     * Adds an event listener to the specified event.
     * 
     * @param name the name of the event,
     * @param func the function to call when the event is emitted,
     */
    public void on(String name, Function func) {
        if (!REGISTRY.contains(name)) {
            throw new IllegalArgumentException("Event " + name + " not found!");
        }

        this.events.computeIfAbsent(name, k -> new ArrayList<>()).add(func);
    }

    /**
     * Registers an event with the specified name.
     * 
     * @param name the name of the event
     */
    public static void register(String name) {
        JSEvents.REGISTRY.add(name);
    }

    /**
     * Emits the specified event to all registered listeners.
     * 
     * @param event the name of the event
     * @param args  the arguments to pass to the event handlers
     */
    public void emit(String event, Object... args) {
        var curScope = this.scope;
        if (this.thread != Thread.currentThread()) {
            Context currentContext = Context.getCurrentContext();
            if (currentContext == null) {
                currentContext = cx.getFactory().enterContext();
            }
            curScope = currentContext.initStandardObjects((ScriptableObject) curScope, true);
        }

        System.out.println("events = " + events);
        if (this.events.containsKey(event)) {
            System.out.println("event = " + event);
            System.out.println("scope = " + curScope);
            for (Function func : this.events.get(event)) {
                System.out.println("func = " + func);
                func.call(cx, curScope, curScope, args);
            }
        }
    }

    /**
     * Emits the specified event to all registered instances.
     *
     * @param  event  the name of the event to emit
     * @param  args   the arguments to pass to the event handlers
     */
    public static void emitAll(String event, Object... args) {
        if (!JSEvents.REGISTRY.contains(event)) {
            throw new IllegalArgumentException("Event " + event + " not found!");
        }

        System.out.println("INSTANCES = " + INSTANCES);

        for (JSEvents instance : INSTANCES) {
            try {
                instance.emit(event, args);
            } catch (RhinoException e) {
                XeoxEvents.EVENT_ERROR.factory().onError(e);
                XeoxLoader.LOGGER.error("Failed to emit event {}:", event, e);
            }
        }
    }

}
