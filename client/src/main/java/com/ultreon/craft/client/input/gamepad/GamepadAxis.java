package com.ultreon.craft.client.input.gamepad;

import com.studiohartman.jamepad.ControllerAxis;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.input.InputDefinition;

public enum GamepadAxis implements InputDefinition<Float> {
    LeftStickX,
    LeftStickY,
    RightStickX,
    RightStickY,
    LeftTrigger,
    RightTrigger,
    DpadX,
    DpadY;

    private final UltracraftClient client = UltracraftClient.get();

    public static GamepadAxis fromSDL(ControllerAxis value) {
        return switch (value) {
            case LEFTX -> LeftStickX;
            case LEFTY -> LeftStickY;
            case RIGHTX -> RightStickX;
            case RIGHTY -> RightStickY;
            case TRIGGERLEFT -> LeftTrigger;
            case TRIGGERRIGHT -> RightTrigger;
        };
    }

    public ControllerAxis sdlAxis() {
        return switch (this) {
            case LeftStickX -> ControllerAxis.LEFTX;
            case LeftStickY -> ControllerAxis.LEFTY;
            case RightStickX -> ControllerAxis.RIGHTX;
            case RightStickY -> ControllerAxis.RIGHTY;
            case LeftTrigger -> ControllerAxis.TRIGGERLEFT;
            case RightTrigger -> ControllerAxis.TRIGGERRIGHT;
            default -> null;
        };
    }

    @Override
    public Icon getIcon() {
        return switch (this) {
            case LeftStickX -> Icon.LeftJoyStickX;
            case LeftStickY -> Icon.LeftJoyStickY;
            case RightStickX -> Icon.RightJoyStickX;
            case RightStickY -> Icon.RightJoyStickY;
            case LeftTrigger -> Icon.LeftTrigger;
            case RightTrigger -> Icon.RightTrigger;
            case DpadX -> Icon.DpadLeftRight;
            case DpadY -> Icon.DpadUpDown;
            default -> Icon.AnyJoyStick;
        };
    }

    @Override
    public Float getValue() {
        return client.gamepadInput.getAxis(this);
    }
}
