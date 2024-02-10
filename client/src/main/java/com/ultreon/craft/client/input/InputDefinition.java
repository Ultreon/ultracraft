package com.ultreon.craft.client.input;

import com.ultreon.craft.client.input.gamepad.Icon;

public interface InputDefinition<T> {
    Icon getIcon();

    T getValue();
}
