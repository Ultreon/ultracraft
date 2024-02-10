package com.ultreon.craft.client.input.gamepad;

import com.ultreon.craft.text.TextObject;

public class ChatGamepadContext extends GamepadContext {
    public static final GamepadContext INSTANCE = new ChatGamepadContext();
    public final GamepadMapping<?> send;
    public final GamepadMapping<?> openKeyboard;
    public final GamepadMapping<?> close;

    public ChatGamepadContext() {
        super();

        this.send = mappings.register(new GamepadMapping<>(GamepadActions.A, GamepadMapping.Side.RIGHT, TextObject.translation("ultracraft.gamepad.action.chat.send")));
        this.openKeyboard = mappings.register(new GamepadMapping<>(GamepadActions.Y, GamepadMapping.Side.RIGHT, TextObject.translation("ultracraft.gamepad.action.chat.open_keyboard")));
        this.close = mappings.register(new GamepadMapping<>(GamepadActions.B, GamepadMapping.Side.RIGHT, TextObject.translation("ultracraft.gamepad.action.inGameMenu.close")));
    }

    @Override
    public int getYOffset() {
        return 32;
    }
}
