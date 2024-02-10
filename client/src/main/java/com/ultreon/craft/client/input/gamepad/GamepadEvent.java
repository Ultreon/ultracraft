package com.ultreon.craft.client.input.gamepad;

import com.ultreon.craft.events.api.Event;

public class GamepadEvent {
    public static final Event<ConnectionEvent> GAMEPAD_CONNECTED = Event.create();
    public static final Event<ConnectionEvent> GAMEPAD_DISCONNECTED = Event.create();

    @FunctionalInterface
    public interface ConnectionEvent {
        void onConnectionStatus(Gamepad gamepad);
    }
}
