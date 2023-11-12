package com.ultreon.craft.client.gui.widget;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Callback;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.properties.CallbackProperty;
import com.ultreon.craft.client.gui.widget.properties.NumberValueProperty;
import com.ultreon.libs.commons.v0.Identifier;

public class Slider extends Widget<Slider> implements CallbackProperty<Slider>, NumberValueProperty<Slider> {
    private static final Identifier TEXTURE = UltracraftClient.id("textures/gui/slider.png");
    private Callback<Slider> callback = it -> {};
    private double value;

    public Slider() {
        super(0, 0, 14, 21);
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        renderer.blit(TEXTURE, this.getX(), this.getY(), 7, 21, 0, 0, 7, 28);
        renderer.blit(TEXTURE, this.getX() + 7, this.getY(), this.getWidth() - 14, 21, 14, 0, 7, 28);
        renderer.blit(TEXTURE, this.getX() + 14, this.getY(), 7, 21, 21, 0, 7, 28);
    }

    @Override
    public Callback<Slider> getCallback() {
        return callback;
    }

    @Override
    public Slider callback(Callback<Slider> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public Slider value(double v) {
        this.value = v;
        return this;
    }
}
