package com.ultreon.craft.client.api.events.gui;

import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.events.api.EventResult;
import com.ultreon.craft.events.api.ValueEventResult;

public class ScreenEvents {
    public static final Event<Open> OPEN = Event.withValue();
    public static final Event<Close> CLOSE = Event.withResult();
    public static final Event<KeyPress> KEY_PRESS = Event.withResult();
    public static final Event<KeyRelease> KEY_RELEASE = Event.withResult();
    public static final Event<CharType> CHAR_TYPE = Event.withResult();
    public static final Event<MouseClick> MOUSE_CLICK = Event.withResult();
    public static final Event<MousePress> MOUSE_PRESS = Event.withResult();
    public static final Event<MouseRelease> MOUSE_RELEASE = Event.withResult();
    public static final Event<MouseDrag> MOUSE_DRAG = Event.withResult();
    public static final Event<MouseEnter> MOUSE_ENTER = Event.withResult();
    public static final Event<MouseExit> MOUSE_EXIT = Event.withResult();
    public static final Event<MouseWheel> MOUSE_WHEEL = Event.withResult();

    @FunctionalInterface
    public interface Open {
        ValueEventResult<Screen> onOpenScreen(Screen open);
    }

    @FunctionalInterface
    public interface Close {
        EventResult onCloseScreen(Screen toClose);
    }

    @FunctionalInterface
    public interface KeyPress {
        EventResult onKeyPressScreen(int keyCode);
    }

    @FunctionalInterface
    public interface KeyRelease {
        EventResult onKeyReleaseScreen(int keyCode);
    }

    @FunctionalInterface
    public interface CharType {
        EventResult onCharTypeScreen(char character);
    }

    @FunctionalInterface
    public interface MouseClick {
        EventResult onMouseClickScreen(int x, int y, int button, int count);
    }

    @FunctionalInterface
    public interface MousePress {
        EventResult onMousePressScreen(int x, int y, int button);
    }

    @FunctionalInterface
    public interface MouseRelease {
        EventResult onMouseReleaseScreen(int x, int y, int button);
    }

    @FunctionalInterface
    public interface MouseDrag {
        EventResult onMouseDragScreen(int x, int y, int nx, int ny, int button);
    }

    @FunctionalInterface
    public interface MouseEnter {
        EventResult onMouseEnterScreen(int x, int y);
    }

    @FunctionalInterface
    public interface MouseExit {
        EventResult onMouseExitScreen();
    }

    @FunctionalInterface
    public interface MouseWheel {
        EventResult onMouseWheelScreen(int x, int y, double rotation);
    }
}
