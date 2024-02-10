package com.ultreon.craft.client.input.gamepad;

import com.studiohartman.jamepad.ControllerButton;
import com.ultreon.craft.client.input.InputDefinition;

public enum GamepadButton implements InputDefinition<GamepadButton> {
    A, B, X, Y, BACK, START, GUIDE, LEFT_STICK, RIGHT_STICK, DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, LEFT_SHOULDER, RIGHT_SHOULDER, MISC1, PADDLE1, PADDLE2, PADDLE3, PADDLE4, TOUCHPAD, MAX, INVALID, UNKNOWN;

    public static GamepadButton fromGDX(ControllerButton value) {
        return switch (value) {
            case A -> A;
            case B -> B;
            case X -> X;
            case Y -> Y;
            case BACK -> BACK;
            case START -> START;
            case GUIDE -> GUIDE;
            case LEFTSTICK -> LEFT_STICK;
            case RIGHTSTICK -> RIGHT_STICK;
            case DPAD_UP -> DPAD_UP;
            case DPAD_DOWN -> DPAD_DOWN;
            case DPAD_LEFT -> DPAD_LEFT;
            case DPAD_RIGHT -> DPAD_RIGHT;
            case LEFTBUMPER -> LEFT_SHOULDER;
            case RIGHTBUMPER -> RIGHT_SHOULDER;
            case BUTTON_MISC1 -> MISC1;
            case BUTTON_PADDLE1 -> PADDLE1;
            case BUTTON_PADDLE2 -> PADDLE2;
            case BUTTON_PADDLE3 -> PADDLE3;
            case BUTTON_PADDLE4 -> PADDLE4;
            case BUTTON_TOUCHPAD -> TOUCHPAD;
            default -> UNKNOWN;
        };
    }

    public ControllerButton sdlButton() {
        return switch (this) {
            case A -> ControllerButton.A;
            case B -> ControllerButton.B;
            case X -> ControllerButton.X;
            case Y -> ControllerButton.Y;
            case BACK -> ControllerButton.BACK;
            case START -> ControllerButton.START;
            case GUIDE -> ControllerButton.GUIDE;
            case LEFT_STICK -> ControllerButton.LEFTSTICK;
            case RIGHT_STICK -> ControllerButton.RIGHTSTICK;
            case DPAD_UP -> ControllerButton.DPAD_UP;
            case DPAD_DOWN -> ControllerButton.DPAD_DOWN;
            case DPAD_LEFT -> ControllerButton.DPAD_LEFT;
            case DPAD_RIGHT -> ControllerButton.DPAD_RIGHT;
            case LEFT_SHOULDER -> ControllerButton.LEFTBUMPER;
            case RIGHT_SHOULDER -> ControllerButton.RIGHTBUMPER;
            case MISC1 -> ControllerButton.BUTTON_MISC1;
            case PADDLE1 -> ControllerButton.BUTTON_PADDLE1;
            case PADDLE2 -> ControllerButton.BUTTON_PADDLE2;
            case PADDLE3 -> ControllerButton.BUTTON_PADDLE3;
            case PADDLE4 -> ControllerButton.BUTTON_PADDLE4;
            case TOUCHPAD -> ControllerButton.BUTTON_TOUCHPAD;
            default -> throw new IllegalStateException("Unexpected value: " + this);
        };
    }

    @Override
    public Icon getIcon() {
        return switch (this) {
            case A -> Icon.ButtonA;
            case B -> Icon.ButtonB;
            case X -> Icon.ButtonX;
            case Y -> Icon.ButtonY;
            case BACK, MAX, INVALID, UNKNOWN -> Icon.AnyButton;
            case START -> Icon.XboxMenu;
            case GUIDE -> Icon.XboxGuide;
            case LEFT_STICK -> Icon.LeftJoyStickPress;
            case RIGHT_STICK -> Icon.RightJoyStickPress;
            case DPAD_UP -> Icon.DpadUp;
            case DPAD_DOWN -> Icon.DpadDown;
            case DPAD_LEFT -> Icon.DpadLeft;
            case DPAD_RIGHT -> Icon.DpadRight;
            case LEFT_SHOULDER -> Icon.LeftTrigger;
            case RIGHT_SHOULDER -> Icon.RightTrigger;
            case MISC1, PADDLE1, PADDLE2, PADDLE3, PADDLE4 -> Icon.AnyButton;
            case TOUCHPAD -> Icon.PS4TouchPad;
        };
    }

    @Override
    public GamepadButton getValue() {
        return null;
    }
}
