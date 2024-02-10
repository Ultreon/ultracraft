package com.ultreon.craft.client.input.gamepad;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.input.InputDefinition;

public enum GamepadTrigger implements InputDefinition<Float> {
    Left(GamepadAxis.LeftTrigger),
    Right(GamepadAxis.RightTrigger);

    private final GamepadAxis axis;
    private final UltracraftClient ultracraftClient = UltracraftClient.get();

    GamepadTrigger(GamepadAxis axis) {
        this.axis = axis;
    }

    @Override
    public Icon getIcon() {
        return switch (this) {
            case Left -> Icon.LeftTrigger;
            case Right -> Icon.RightTrigger;
        };
    }

    @Override
    public Float getValue() {
        return ultracraftClient.gamepadInput.getAxis(axis);
    }

    public GamepadAxis getAxis() {
        return axis;
    }

    public boolean isPressed() {
        return ultracraftClient.gamepadInput.isAxisPressed(axis);
    }

    public boolean isJustPressed() {
        return ultracraftClient.gamepadInput.isTriggerJustPressed(axis);
    }
}
