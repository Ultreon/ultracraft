package com.ultreon.craft.client.input.gamepad;

import com.ultreon.craft.text.TextObject;

public class InGameMenuGamepadContext extends MenuGamepadContext {
    public final GamepadMapping<?> close;

    public InGameMenuGamepadContext() {
        super();

        this.close = mappings.register(new GamepadMapping<>(GamepadActions.START, GamepadMapping.Side.RIGHT, TextObject.translation("ultracraft.gamepad.action.inGameMenu.close")));
    }
}
