package com.ultreon.craft.client.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.util.Color;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.Callback;
import com.ultreon.craft.client.gui.GuiComponent;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class Button extends GuiComponent {
    private Callback<Button> callback = caller -> {};
    private @Nullable @IntRange(from = 0, to = 359) Color color = Color.LIGHT_GRAY;
    private boolean pressed;
    private String message;
    private Color textColor = Color.WHITE;

    /**
     * @param x       the X position of the button
     * @param y       the Y position of the button
     * @param width   the width of the button
     */
    public Button(int x, int y, @IntRange(from = 21) int width,  String message) {
        this(x, y, width, 21, message);
    }

    /**
     * @param x       the X position of the button
     * @param y       the Y position of the button
     * @param width   the width of the button
     * @param message the button message.
     */
    public Button(int x, int y, @IntRange(from = 21) int width, @IntRange(from = 21) int height, String message) {
        super(x, y, width, height);
        this.message = message;
    }

    /**
     * @param x        the X position of the button
     * @param y        the Y position of the button
     * @param width    the width of the button
     * @param callback the callback of the button, gets called when the button activates.
     */
    public Button(int x, int y, @IntRange(from = 21) int width,  String message, Callback<Button> callback) {
        this(x, y, width, 21, message, callback);
    }

    /**
     * @param x       the X position of the button
     * @param y       the Y position of the button
     * @param width   the width of the button
     * @param message the button message.
     * @param callback the callback of the button, gets called when the button activates.
     */
    public Button(int x, int y, @IntRange(from = 21) int width, @IntRange(from = 21) int height, String message, Callback<Button> callback) {
        super(x, y, width, height);
        this.callback = callback;
        this.message = message;
    }

    @Nullable
    @IntRange(from = 0, to = 359)
    public Color getColor() {
        return this.color;
    }

    public void setColor(@Nullable @IntRange(from = 0, to = 359) Color color) {
        this.color = color;
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        Texture texture = this.client.getTextureManager().getTexture(UltracraftClient.id("textures/gui/widgets.png"));

        int x = this.x;
        int y = this.y;
        int u = this.enabled ? this.isWithinBounds(mouseX, mouseY) ? 21 : 0 : 42;
        int v = this.isPressed() ? 21 : 0;

        renderer.setTextureColor(Color.WHITE);
        renderer.blit(texture, x, y, 7, 7, u, v, 7, 7);
        renderer.blit(texture, x+7, y, this.width - 14, 7, 7 + u, v, 7, 7);
        renderer.blit(texture, x+ this.width -7, y, 7, 7, 14 + u, v, 7, 7);
        renderer.blit(texture, x, y+7, 7, this.height - 14, u, 7 + v, 7, 7);
        renderer.blit(texture, x+7, y+7, this.width - 14, this.height - 14, 7 + u, 7 + v, 7, 7);
        renderer.blit(texture, x+ this.width -7, y+7, 7, this.height - 14, 14 + u, 7 + v, 7, 7);
        renderer.blit(texture, x, y + this.height - 7, 7, 7, u, 14 + v, 7, 7);
        renderer.blit(texture, x+7, y + this.height - 7, this.width - 14, 7, 7 + u, 14 + v, 7, 7);
        renderer.blit(texture, x+ this.width -7, y + this.height - 7, 7, 7, 14 + u, 14 + v, 7, 7);

        renderer.drawCenteredText(this.message, x + this.width / 2, y + (this.height / 2 - this.font.lineHeight + (this.isPressed() ? 2 : 0)), this.enabled ? this.textColor : this.textColor.withAlpha(0x80));
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        return !this.click();
    }

    @ApiStatus.OverrideOnly
    public boolean click() {
        if (!this.enabled) return false;

        Callback<Button> callback = this.callback;
        if (callback == null) {
            return true;
        }
        callback.call(this);
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

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getTextColor() {
        return this.textColor;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
