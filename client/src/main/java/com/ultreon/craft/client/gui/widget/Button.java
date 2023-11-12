package com.ultreon.craft.client.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Callback;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.properties.CallbackProperty;
import com.ultreon.craft.client.gui.widget.properties.ColorProperty;
import com.ultreon.craft.client.gui.widget.properties.TextColorProperty;
import com.ultreon.craft.client.gui.widget.properties.TextProperty;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public class Button<T extends Button<T>> extends Widget<T> implements ColorProperty, TextProperty<T>, TextColorProperty, CallbackProperty<T> {
    private Callback<T> callback = caller -> {
    };
    private @Nullable @IntRange(from = 0, to = 359) Color color = Color.LIGHT_GRAY;
    private boolean pressed;
    private TextObject text = TextObject.empty();
    private Color textColor = Color.WHITE;

    /**
     * @param x the X position of the button
     * @param y the Y position of the button
     * @param width the width of the button
     */
    @SafeVarargs
    public Button(int x, int y, @IntRange(from = 21) int width, T... typeGetter) {
        this(x, y, width, 21, typeGetter);
    }

    /**
     * @param x the X position of the button
     * @param y the Y position of the button
     * @param width the width of the button
     * @param height the height of the button
     */
    @SafeVarargs
    public Button(int x, int y, @IntRange(from = 21) int width, @IntRange(from = 21) int height, T... typeGetter) {
        super(x, y, width, height, typeGetter);
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        Texture texture = this.client.getTextureManager().getTexture(UltracraftClient.id("textures/gui/widgets.png"));

        int x = this.pos.x;
        int y = this.pos.y;

        int u;
        if (this.enabled) u = this.isWithinBounds(mouseX, mouseY) ? 21 : 0;
        else u = 42;
        int v = this.isPressed() ? 21 : 0;

        renderer.setTextureColor(Color.WHITE);
        renderer.blit(texture, x, y, 7, 7, u, v, 7, 7);
        renderer.blit(texture, x + 7, y, this.size.width - 14, 7, 7 + u, v, 7, 7);
        renderer.blit(texture, x + this.size.width - 7, y, 7, 7, 14 + u, v, 7, 7);
        renderer.blit(texture, x, y + 7, 7, this.size.height - 14, u, 7 + v, 7, 7);
        renderer.blit(texture, x + 7, y + 7, this.size.width - 14, this.size.height - 14, 7 + u, 7 + v, 7, 7);
        renderer.blit(texture, x + this.size.width - 7, y + 7, 7, this.size.height - 14, 14 + u, 7 + v, 7, 7);
        renderer.blit(texture, x, y + this.size.height - 7, 7, 7, u, 14 + v, 7, 7);
        renderer.blit(texture, x + 7, y + this.size.height - 7, this.size.width - 14, 7, 7 + u, 14 + v, 7, 7);
        renderer.blit(texture, x + this.size.width - 7, y + this.size.height - 7, 7, 7, 14 + u, 14 + v, 7, 7);

        renderer.drawTextCenter(this.text, x + this.size.width / 2, y + (this.size.height / 2 - this.font.lineHeight + (this.isPressed() ? 2 : 0)), this.enabled ? this.textColor : this.textColor.withAlpha(0x80));
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        return !this.click();
    }

    @ApiStatus.OverrideOnly
    public boolean click() {
        if (!this.enabled) return false;

        Callback<T> callback = this.callback;
        if (callback == null) {
            return true;
        }
        callback.call((T) this);
        return false;
    }

    @Override
    public boolean mousePress(int x, int y, int button) {
        if (!this.enabled) return false;

        this.pressed = true;
        return super.mousePress(x, y, button);
    }

    @Override
    public boolean mouseRelease(int x, int y, int button) {
        this.pressed = false;
        return super.mouseRelease(x, y, button);
    }

    public boolean isPressed() {
        return this.pressed && this.enabled;
    }

    @Override
    public String getName() {
        return "Button";
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    @Override
    @Nullable
    @IntRange(from = 0, to = 359)
    public Color getColor() {
        return this.color;
    }

    @Override
    public T color(@Nullable @IntRange(from = 0, to = 359) Color color) {
        this.color = color;
        return (T) this;
    }

    @Override
    @CanIgnoreReturnValue
    public T textColor(@NotNull Color textColor) {
        this.textColor = textColor;
        return (T) this;
    }

    @Override
    public @NotNull Color getTextColor() {
        return this.textColor;
    }

    @Override
    public T text(TextObject text) {
        this.text = text;
        return (T) this;
    }

    @Override
    public TextObject getText() {
        return this.text;
    }

    @Override
    public T callback(Callback<T> callback) {
        Preconditions.checkNotNull(callback, "callback");
        this.callback = callback;
        return (T) this;
    }

    @Override
    public void _callback(Object widget) {
        CallbackProperty.super._callback(widget);
    }

    @Override
    public Callback<T> getCallback() {
        return this.callback;
    }

    @Override
    public String getRawText() {
        return this.text.getText();
    }
}
