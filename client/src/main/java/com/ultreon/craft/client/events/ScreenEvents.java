package com.ultreon.craft.client.events;

import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.events.api.EventResult;
import com.ultreon.craft.events.api.ValueEventResult;

/**
 * Screen events for Ultracraft.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 * @see Screen
 * @deprecated Use {@link GuiEvents} instead
 */
@Deprecated(since = "0.1.0", forRemoval = true)
public class ScreenEvents {
    @Deprecated(since = "0.1.0", forRemoval = true) public static final Event<Open> OPEN = Event.withValue();
    @Deprecated(since = "0.1.0", forRemoval = true) public static final Event<Close> CLOSE = Event.withResult();
    @Deprecated(since = "0.1.0", forRemoval = true) public static final Event<KeyPress> KEY_PRESS = Event.withResult();
    @Deprecated(since = "0.1.0", forRemoval = true) public static final Event<KeyRelease> KEY_RELEASE = Event.withResult();
    @Deprecated(since = "0.1.0", forRemoval = true) public static final Event<CharType> CHAR_TYPE = Event.withResult();
    @Deprecated(since = "0.1.0", forRemoval = true) public static final Event<MouseClick> MOUSE_CLICK = Event.withResult();
    @Deprecated(since = "0.1.0", forRemoval = true) public static final Event<MousePress> MOUSE_PRESS = Event.withResult();
    @Deprecated(since = "0.1.0", forRemoval = true) public static final Event<MouseRelease> MOUSE_RELEASE = Event.withResult();
    @Deprecated(since = "0.1.0", forRemoval = true) public static final Event<MouseDrag> MOUSE_DRAG = Event.withResult();
    @Deprecated(since = "0.1.0", forRemoval = true) public static final Event<MouseEnter> MOUSE_ENTER = Event.withResult();
    @Deprecated(since = "0.1.0", forRemoval = true) public static final Event<MouseExit> MOUSE_EXIT = Event.withResult();
    @Deprecated(since = "0.1.0", forRemoval = true) public static final Event<MouseWheel> MOUSE_WHEEL = Event.withResult();

    @Deprecated(since = "0.1.0", forRemoval = true)
    @FunctionalInterface
    public interface Open {
        @Deprecated(since = "0.1.0", forRemoval = true)
        ValueEventResult<Screen> onOpenScreen(Screen open);
    }

    @Deprecated(since = "0.1.0", forRemoval = true)
    @FunctionalInterface
    public interface Close {
        @Deprecated(since = "0.1.0", forRemoval = true)
        EventResult onCloseScreen(Screen toClose);
    }

    @Deprecated(since = "0.1.0", forRemoval = true)
    @FunctionalInterface
    public interface KeyPress {
        @Deprecated(since = "0.1.0", forRemoval = true)
        EventResult onKeyPressScreen(int keyCode);
    }

    @Deprecated(since = "0.1.0", forRemoval = true)
    @FunctionalInterface
    public interface KeyRelease {
        @Deprecated(since = "0.1.0", forRemoval = true)
        EventResult onKeyReleaseScreen(int keyCode);
    }

    @Deprecated(since = "0.1.0", forRemoval = true)
    @FunctionalInterface
    public interface CharType {
        @Deprecated(since = "0.1.0", forRemoval = true)
        EventResult onCharTypeScreen(char character);
    }

    @Deprecated(since = "0.1.0", forRemoval = true)
    @FunctionalInterface
    public interface MouseClick {
        @Deprecated(since = "0.1.0", forRemoval = true)
        EventResult onMouseClickScreen(int x, int y, int button, int count);
    }

    @Deprecated(since = "0.1.0", forRemoval = true)
    @FunctionalInterface
    public interface MousePress {
        @Deprecated(since = "0.1.0", forRemoval = true)
        EventResult onMousePressScreen(int x, int y, int button);
    }

    @Deprecated(since = "0.1.0", forRemoval = true)
    @FunctionalInterface
    public interface MouseRelease {
        @Deprecated(since = "0.1.0", forRemoval = true)
        EventResult onMouseReleaseScreen(int x, int y, int button);
    }

    @Deprecated(since = "0.1.0", forRemoval = true)
    @FunctionalInterface
    public interface MouseDrag {
        @Deprecated(since = "0.1.0", forRemoval = true)
        EventResult onMouseDragScreen(int x, int y, int nx, int ny, int button);
    }

    @Deprecated(since = "0.1.0", forRemoval = true)
    @FunctionalInterface
    public interface MouseEnter {
        @Deprecated(since = "0.1.0", forRemoval = true)
        EventResult onMouseEnterScreen(int x, int y);
    }

    @Deprecated(since = "0.1.0", forRemoval = true)
    @FunctionalInterface
    public interface MouseExit {
        @Deprecated(since = "0.1.0", forRemoval = true)
        EventResult onMouseExitScreen();
    }

    @Deprecated(since = "0.1.0", forRemoval = true)
    @FunctionalInterface
    public interface MouseWheel {
        @Deprecated(since = "0.1.0", forRemoval = true)
        EventResult onMouseWheelScreen(int x, int y, double rotation);
    }
}
