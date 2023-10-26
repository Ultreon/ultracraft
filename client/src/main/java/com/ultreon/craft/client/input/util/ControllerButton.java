package com.ultreon.craft.client.input.util;

import com.badlogic.gdx.controllers.ControllerMapping;

public enum ControllerButton {
    A, B, X, Y, DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, LEFT_STICK, RIGHT_STICK, LEFT_BUTTON, RIGHT_BUTTON;

    public int get(ControllerMapping mapping) {
        return switch (this) {
            case A -> mapping.buttonA;
            case B -> mapping.buttonB;
            case X -> mapping.buttonX;
            case Y -> mapping.buttonY;
            case DPAD_UP -> mapping.buttonDpadUp;
            case DPAD_DOWN -> mapping.buttonDpadDown;
            case DPAD_LEFT -> mapping.buttonDpadLeft;
            case DPAD_RIGHT -> mapping.buttonDpadRight;
            case LEFT_STICK -> mapping.buttonLeftStick;
            case RIGHT_STICK -> mapping.buttonRightStick;
            case LEFT_BUTTON -> mapping.buttonL1;
            case RIGHT_BUTTON -> mapping.buttonR1;
            default -> throw new IllegalStateException("Unexpected value: " + this);
        };
    }
}
