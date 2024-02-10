package com.ultreon.craft.client.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.gui.*;

import java.util.function.Supplier;

public class KeyboardButton extends Button<KeyboardButton> {
    private final KeyMappingIcon icon;

    public KeyboardButton(KeyMappingIcon icon, Callback<KeyboardButton> callback) {
        super(icon.width, icon.height);
        this.icon = icon;

        this.callback(callback);
    }

    @Override
    protected void renderButton(Renderer renderer, int mouseX, int mouseY, Texture texture, int x, int y) {
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        icon.render(renderer, pos.x, pos.y, focused);
    }

    @Override
    public KeyboardButton position(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    @Override
    public KeyboardButton bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> this.setBounds(position.get()));
        return this;
    }

    @Override
    public KeyboardButton callback(Callback<KeyboardButton> callback) {
        this.callback.set(callback);
        return this;
    }
}
