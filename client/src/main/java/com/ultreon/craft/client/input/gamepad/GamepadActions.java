package com.ultreon.craft.client.input.gamepad;

import static com.ultreon.craft.client.input.gamepad.GamepadAction.*;

public class GamepadActions {
    public static final Button A = new Button(GamepadButton.A);
    public static final Button B = new Button(GamepadButton.B);
    public static final Button X = new Button(GamepadButton.X);
    public static final Button Y = new Button(GamepadButton.Y);
    public static final Button DPAD_UP = new Button(GamepadButton.DPAD_UP);
    public static final Button DPAD_DOWN = new Button(GamepadButton.DPAD_DOWN);
    public static final Button DPAD_LEFT = new Button(GamepadButton.DPAD_LEFT);
    public static final Button DPAD_RIGHT = new Button(GamepadButton.DPAD_RIGHT);
    public static final Button LEFT_SHOULDER = new Button(GamepadButton.LEFT_SHOULDER);
    public static final Button RIGHT_SHOULDER = new Button(GamepadButton.RIGHT_SHOULDER);
    public static final Button PRESS_LEFT_STICK = new Button(GamepadButton.LEFT_STICK);
    public static final Button PRESS_RIGHT_STICK = new Button(GamepadButton.RIGHT_STICK);
    public static final Button START = new Button(GamepadButton.START);
    public static final Button BACK = new Button(GamepadButton.BACK);
    public static final Button GUIDE = new Button(GamepadButton.GUIDE);
    public static final Trigger LEFT_TRIGGER = new Trigger(GamepadTrigger.Left);
    public static final Trigger RIGHT_TRIGGER = new Trigger(GamepadTrigger.Right);
    public static final Joystick MOVE_LEFT_STICK = new Joystick(GamepadJoystick.Left);
    public static final Joystick MOVE_RIGHT_STICK = new Joystick(GamepadJoystick.Right);
    public static final Axis MOVE_LEFT_STICK_X = new Axis(GamepadAxis.LeftStickX);
    public static final Axis MOVE_LEFT_STICK_Y = new Axis(GamepadAxis.LeftStickY);
    public static final Axis MOVE_RIGHT_STICK_X = new Axis(GamepadAxis.RightStickX);
    public static final Axis MOVE_RIGHT_STICK_Y = new Axis(GamepadAxis.RightStickY);
    public static final Axis MOVE_LEFT_TRIGGER = new Axis(GamepadAxis.LeftTrigger);
    public static final Axis MOVE_RIGHT_TRIGGER = new Axis(GamepadAxis.RightTrigger);
}
