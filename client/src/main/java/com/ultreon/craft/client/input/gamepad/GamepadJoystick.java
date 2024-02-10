package com.ultreon.craft.client.input.gamepad;

import com.badlogic.gdx.math.Vector2;
import com.studiohartman.jamepad.ControllerAxis;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.input.InputDefinition;

public enum GamepadJoystick implements InputDefinition<Vector2> {
    Left(GamepadAxis.LeftStickX, GamepadAxis.LeftStickY),
    Right(GamepadAxis.RightStickX, GamepadAxis.RightStickY),
    Dpad(GamepadAxis.DpadX, GamepadAxis.DpadY);

    public final GamepadAxis xAxis;
    public final GamepadAxis yAxis;

    GamepadJoystick(GamepadAxis xAxis, GamepadAxis yAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }

    @Override
    public Icon getIcon() {
        return switch (this) {
            case Left -> Icon.LeftJoyStickMove;
            case Right -> Icon.RightJoyStickMove;
            case Dpad -> Icon.Dpad;
        };
    }

    public float getAxisX() {
        return xAxis.getValue();
    }

    public float getAxisY() {
        return yAxis.getValue();
    }

    @Override
    public Vector2 getValue() {
        return new Vector2(getAxisX(), getAxisY());
    }

    public ControllerAxis sdlAxisX() {
        return xAxis.sdlAxis();
    }

    public ControllerAxis sdlAxisY() {
        return yAxis.sdlAxis();
    }

    public GamepadAxis getXAxis() {
        return xAxis;
    }

    public GamepadAxis getYAxis() {
        return yAxis;
    }

    public float getJoystickLength() {
        return (float) Math.sqrt(getAxisX() * getAxisX() + getAxisY() * getAxisY());
    }

    public float getJoystickAngle() {
        return (float) Math.atan2(getAxisY(), getAxisX());
    }

    public boolean isPressed() {
        GamepadInput input = UltracraftClient.get().gamepadInput;
        return switch (this) {
            case Left -> input.isButtonPressed(GamepadButton.LEFT_STICK);
            case Right -> input.isButtonPressed(GamepadButton.RIGHT_STICK);
            case Dpad -> input.isButtonPressed(GamepadButton.DPAD_UP) ||
                    input.isButtonPressed(GamepadButton.DPAD_DOWN) ||
                    input.isButtonPressed(GamepadButton.DPAD_LEFT) ||
                    input.isButtonPressed(GamepadButton.DPAD_RIGHT);
        };
    }
}
