package com.ultreon.craft.client.input.gamepad;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.screens.ChatScreen;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.text.TextObject;

public class MenuGamepadContext extends GamepadContext {
    public static final MenuGamepadContext INSTANCE = new MenuGamepadContext();
    public final GamepadMapping<?> joystickMove;
    public final GamepadMapping<?> dpadMove;
    public final GamepadMapping<?> activate;
    public final GamepadMapping<?> scrollY;

    protected MenuGamepadContext() {
        super();

        this.joystickMove = mappings.register(new GamepadMapping<>(new GamepadAction.Joystick(GamepadJoystick.Left), GamepadMapping.Side.LEFT, TextObject.translation("ultracraft.gamepad.action.menu.joystick_move")));
        this.dpadMove = mappings.register(new GamepadMapping<>(new GamepadAction.Joystick(GamepadJoystick.Dpad), GamepadMapping.Side.LEFT, TextObject.translation("ultracraft.gamepad.action.menu.dpad_move")));
        this.activate = mappings.register(new GamepadMapping<>(new GamepadAction.Button(GamepadButton.A), GamepadMapping.Side.RIGHT, TextObject.translation("ultracraft.gamepad.action.menu.activate")));
        this.scrollY = mappings.register(new GamepadMapping<>(new GamepadAction.Axis(GamepadAxis.RightStickY), GamepadMapping.Side.RIGHT, TextObject.translation("ultracraft.gamepad.action.menu.scroll_y")));
    }

    @Override
    public int getYOffset() {
        Screen screen = UltracraftClient.get().screen;
        if (screen instanceof ChatScreen) {
            return 32;
        }

        return super.getYOffset();
    }
}
