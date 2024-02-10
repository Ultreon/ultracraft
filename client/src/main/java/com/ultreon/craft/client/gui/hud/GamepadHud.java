package com.ultreon.craft.client.gui.hud;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.input.gamepad.*;
import com.ultreon.craft.util.Color;

import java.util.List;

public class GamepadHud {
    private final GamepadInput input = UltracraftClient.get().gamepadInput;

    public void render(Renderer renderer, float ignoredPartialTicks) {
        GamepadContext ctx = GamepadContext.get();

        renderer.textRight("Context: " + (ctx == null ? "None" : ctx.getClass().getSimpleName()), width() - 4, 4, Color.WHITE);

        if (ctx == null) return;
        if (!input.isAvailable()) return;

        List<GamepadMapping<?>> mappings = ctx.mappings.getAllMappings();

        int leftY = 20 + ctx.getYOffset();
        int rightY = 20 + ctx.getYOffset();

        for (GamepadMapping<?> mapping : mappings) {
            if (!mapping.visible()) continue;

            GamepadMapping.Side side = mapping.side();
            int x = side == GamepadMapping.Side.LEFT ? 4 + ctx.getLeftXOffset() : width() - 24 - ctx.getRightXOffset();
            int y = height() - (side == GamepadMapping.Side.LEFT ? leftY : rightY);
            mapping.action().getMapping().getIcon().render(renderer, x, y);

            if (side == GamepadMapping.Side.LEFT) {
                renderer.textLeft(mapping.name().copy().append(" (" + mapping.action().getValue() + ")"), 28 + ctx.getLeftXOffset(), height() - leftY + 4, Color.WHITE);

                leftY += 20;
            } else {
                int textRightX = width() - 28 - UltracraftClient.get().font.width(mapping.name());
                renderer.textLeft(mapping.name().copy().append(" (" + mapping.action().getValue() + ")"), textRightX - ctx.getRightXOffset(), height() - rightY + 4, Color.WHITE);

                rightY += 20;
            }
        }

        renderer.textLeft("Left Joystick: " + UltracraftClient.get().gamepadInput.getJoystick(GamepadJoystick.Left).toString(), 4, 4, Color.RED);
        renderer.textLeft("Right Joystick: " + UltracraftClient.get().gamepadInput.getJoystick(GamepadJoystick.Right).toString(), 4, 16, Color.RED);

        renderer.textLeft("A Button: " + UltracraftClient.get().gamepadInput.isButtonPressed(GamepadButton.A), 4, 28, Color.RED);
        renderer.textLeft("B Button: " + UltracraftClient.get().gamepadInput.isButtonPressed(GamepadButton.B), 4, 40, Color.RED);
        renderer.textLeft("X Button: " + UltracraftClient.get().gamepadInput.isButtonPressed(GamepadButton.X), 4, 52, Color.RED);
        renderer.textLeft("Y Button: " + UltracraftClient.get().gamepadInput.isButtonPressed(GamepadButton.Y), 4, 64, Color.RED);

        renderer.textLeft("Left Shoulder Button: " + UltracraftClient.get().gamepadInput.isButtonPressed(GamepadButton.LEFT_SHOULDER), 4, 76, Color.RED);
        renderer.textLeft("Right Shoulder Button: " + UltracraftClient.get().gamepadInput.isButtonPressed(GamepadButton.RIGHT_SHOULDER), 4, 88, Color.RED);

        renderer.textLeft("Back Button: " + UltracraftClient.get().gamepadInput.isButtonPressed(GamepadButton.BACK), 4, 100, Color.RED);
        renderer.textLeft("Start Button: " + UltracraftClient.get().gamepadInput.isButtonPressed(GamepadButton.START), 4, 112, Color.RED);
        renderer.textLeft("Guide Button: " + UltracraftClient.get().gamepadInput.isButtonPressed(GamepadButton.GUIDE), 4, 124, Color.RED);

        renderer.textLeft("Left Stick Button: " + UltracraftClient.get().gamepadInput.isButtonPressed(GamepadButton.LEFT_STICK), 4, 136, Color.RED);
        renderer.textLeft("Right Stick Button: " + UltracraftClient.get().gamepadInput.isButtonPressed(GamepadButton.RIGHT_STICK), 4, 148, Color.RED);

        renderer.textLeft("D-Pad Up Button: " + UltracraftClient.get().gamepadInput.isButtonPressed(GamepadButton.DPAD_UP), 4, 160, Color.RED);
        renderer.textLeft("D-Pad Down Button: " + UltracraftClient.get().gamepadInput.isButtonPressed(GamepadButton.DPAD_DOWN), 4, 172, Color.RED);
        renderer.textLeft("D-Pad Left Button: " + UltracraftClient.get().gamepadInput.isButtonPressed(GamepadButton.DPAD_LEFT), 4, 184, Color.RED);
        renderer.textLeft("D-Pad Right Button: " + UltracraftClient.get().gamepadInput.isButtonPressed(GamepadButton.DPAD_RIGHT), 4, 196, Color.RED);

        renderer.textLeft("Left Trigger: " + UltracraftClient.get().gamepadInput.getTrigger(GamepadTrigger.Left), 4, 208, Color.RED);
        renderer.textLeft("Right Trigger: " + UltracraftClient.get().gamepadInput.getTrigger(GamepadTrigger.Right), 4, 220, Color.RED);
    }

    private int width() {
        return UltracraftClient.get().getScaledWidth();
    }

    private int height() {
        return UltracraftClient.get().getScaledHeight();
    }
}
