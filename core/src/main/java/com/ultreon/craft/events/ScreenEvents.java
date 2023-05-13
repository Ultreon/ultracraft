package com.ultreon.craft.events;

import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.libs.events.v1.Event;
import com.ultreon.libs.events.v1.EventResult;
import com.ultreon.libs.events.v1.ValueEventResult;

public class ScreenEvents {
    public static final Event<Open> OPEN = Event.withValue();
    public static final Event<Close> CLOSE = Event.withResult();

    @FunctionalInterface
    public interface Open {
        ValueEventResult<Screen> onOpenScreen(Screen open);
    }

    @FunctionalInterface
    public interface Close {
        EventResult onCloseScreen(Screen toClose);
    }
}
