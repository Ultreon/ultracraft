package com.ultreon.craft.input.util;

import com.badlogic.gdx.controllers.ControllerMapping;

public enum ControllerButton {
    A, B, X, Y, DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, LEFT_STICK, RIGHT_STICK, LEFT_BUTTON, RIGHT_BUTTON;

    public int get(ControllerMapping mapping) {
        switch (this) {
            case A:
                return mapping.buttonA;
            case B:
                return mapping.buttonB;
            case X:
                return mapping.buttonX;
            case Y:
                return mapping.buttonY;
            case DPAD_UP:
                return mapping.buttonDpadUp;
            case DPAD_DOWN:
                return mapping.buttonDpadDown;
            case DPAD_LEFT:
                return mapping.buttonDpadLeft;
            case DPAD_RIGHT:
                return mapping.buttonDpadRight;
            case LEFT_STICK:
                return mapping.buttonLeftStick;
            case RIGHT_STICK:
                return mapping.buttonRightStick;
            case LEFT_BUTTON:
                return mapping.buttonL1;
            case RIGHT_BUTTON:
                return mapping.buttonR1;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
