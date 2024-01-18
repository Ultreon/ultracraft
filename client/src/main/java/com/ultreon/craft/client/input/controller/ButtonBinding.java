package com.ultreon.craft.client.input.controller;

import com.google.gson.JsonObject;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.input.GameInput;
import com.ultreon.craft.client.input.util.ControllerButton;
import com.ultreon.craft.world.container.ContainerView;
import com.ultreon.libs.commons.v0.Identifier;

public class ButtonBinding {
    private final Identifier id;
    private ControllerButton button;

    public ButtonBinding(Identifier id, ControllerButton button) {
        this.id = id;
        this.button = button;
    }

    public ControllerButton getButton() {
        return button;
    }

    public void setButton(ControllerButton button) {
        this.button = button;
    }

    public boolean isPressed() {
        return GameInput.isControllerButtonDown(button);
    }

    public boolean isReleased() {
        return !GameInput.isControllerButtonDown(button);
    }

    public boolean isJustPressed() {
        return GameInput.isControllerButtonJustPressed(button);
    }

    public final void save(JsonObject object) {
        object.addProperty(id.toString(), button.name());
    }
}
