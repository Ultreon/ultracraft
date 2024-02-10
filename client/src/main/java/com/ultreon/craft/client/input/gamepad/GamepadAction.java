package com.ultreon.craft.client.input.gamepad;

import com.badlogic.gdx.math.Vector2;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.input.InputDefinition;

public sealed interface GamepadAction<T extends InputDefinition<?>> {
    /**
     * Get the mapping of the action
     *
     * @return the mapping
     */
    T getMapping();

    /**
     * Set the mapping of the action
     *
     * @param mapping the mapping
     */
    void setMapping(T mapping);

    /**
     * Check if the action is pressed
     *
     * @return true if the action is pressed, false otherwise.
     */
    boolean isPressed();

    /**
     * Get the value of the action
     *
     * @return a value between 0..1
     */
    float getValue();

    /**
     * Get the axis value of the action
     *
     * @return a value between -1..1
     */
    float getAxisValue();

    /**
     * Get the 2D value of the action
     *
     * @return a 2D vector with values between -1..1
     */
    default Vector2 get2DValue() {
        return new Vector2(0, 0);
    }

    default boolean isJustPressed() {
        return false;
    }

    final class Button implements GamepadAction<GamepadButton> {
        private GamepadButton button;

        public Button(GamepadButton button) {
            this.button = button;
        }

        public boolean isPressed() {
            return UltracraftClient.get().gamepadInput.isButtonPressed(button);
        }

        @Override
        public float getValue() {
            return isPressed() ? 1 : 0;
        }

        @Override
        public float getAxisValue() {
            return switch (button) {
                case DPAD_LEFT, DPAD_DOWN -> -1;
                case DPAD_RIGHT, DPAD_UP -> 1;
                default -> 0;
            };
        }

        @Override
        public Vector2 get2DValue() {
            return switch (button) {
                case DPAD_LEFT, DPAD_RIGHT -> new Vector2(getAxisValue(), 0);
                case DPAD_UP, DPAD_DOWN -> new Vector2(0, getAxisValue());
                default -> new Vector2(0, 0);
            };
        }

        @Override
        public GamepadButton getMapping() {
            return button;
        }

        @Override
        public void setMapping(GamepadButton mapping) {
            this.button = mapping;
        }

        @Override
        public boolean isJustPressed() {
            return UltracraftClient.get().gamepadInput.isButtonJustPressed(button);
        }
    }

    final class Axis implements GamepadAction<GamepadAxis> {
        private GamepadAxis axis;

        public Axis(GamepadAxis axis) {
            this.axis = axis;
        }

        @Override
        public GamepadAxis getMapping() {
            return axis;
        }

        @Override
        public void setMapping(GamepadAxis mapping) {
            this.axis = mapping;
        }

        public float getValue() {
            return UltracraftClient.get().gamepadInput.getAxis(axis);
        }

        @Override
        public float getAxisValue() {
            return axis.getValue();
        }

        @Override
        public Vector2 get2DValue() {
            return UltracraftClient.get().gamepadInput.tryGetAxis(axis);
        }

        public boolean isPressed() {
            return UltracraftClient.get().gamepadInput.isAxisPressed(axis);
        }
    }

    final class Joystick implements GamepadAction<GamepadJoystick> {
        private GamepadJoystick joystick;

        public Joystick(GamepadJoystick joystick) {
            this.joystick = joystick;
        }

        @Override
        public GamepadJoystick getMapping() {
            return joystick;
        }

        @Override
        public void setMapping(GamepadJoystick mapping) {
            this.joystick = mapping;
        }

        public boolean isPressed() {
            return joystick.getJoystickLength() > 0;
        }

        @Override
        public float getValue() {
            return joystick.getJoystickLength();
        }

        @Override
        public float getAxisValue() {
            return joystick.getAxisY();
        }

        public Vector2 get2DValue() {
            return UltracraftClient.get().gamepadInput.getJoystick(joystick);
        }
    }

    final class Trigger implements GamepadAction<GamepadTrigger> {
        private GamepadTrigger trigger;

        public Trigger(GamepadTrigger trigger) {
            this.trigger = trigger;
        }

        @Override
        public GamepadTrigger getMapping() {
            return trigger;
        }

        @Override
        public void setMapping(GamepadTrigger mapping) {
            this.trigger = mapping;
        }

        public float getValue() {
            return UltracraftClient.get().gamepadInput.getTrigger(trigger);
        }

        public boolean isPressed() {
            return trigger.isPressed();
        }

        @Override
        public boolean isJustPressed() {
            return trigger.isJustPressed();
        }

        @Override
        public float getAxisValue() {
            return getValue() * 2 - 1;
        }
    }
}
