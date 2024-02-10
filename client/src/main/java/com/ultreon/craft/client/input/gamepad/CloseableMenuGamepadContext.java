package com.ultreon.craft.client.input.gamepad;

import com.ultreon.craft.text.TextObject;

public class CloseableMenuGamepadContext extends MenuGamepadContext {
    public static final CloseableMenuGamepadContext INSTANCE = new CloseableMenuGamepadContext();
    public final GamepadMapping<?> back;

    protected CloseableMenuGamepadContext() {
        super();

        this.back = mappings.register(new GamepadMapping<>(new GamepadAction.Button(GamepadButton.B), GamepadMapping.Side.RIGHT, TextObject.translation("ultracraft.gamepad.action.menu.back")));
    }
}
