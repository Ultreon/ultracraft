package com.ultreon.craft.client.input.gamepad;

import com.ultreon.craft.client.input.InputDefinition;
import com.ultreon.craft.text.TextObject;

public record GamepadMapping<T extends InputDefinition<?>>(GamepadAction<T> action,
                                                           Side side, TextObject name, boolean visible) {
    public GamepadMapping(GamepadAction<T> action, Side side, TextObject name) {
        this(action, side, name, true);
    }

    @Override
    public String toString() {
        return "GamepadMapping[" +
                "action=" + action + ", " +
                "side=" + side + ", " +
                "name=" + name + ']';
    }

    public enum Side {
        LEFT,
        RIGHT
    }
}
