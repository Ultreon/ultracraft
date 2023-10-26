package com.ultreon.craft.client.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.util.Color;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.Callback;
import com.ultreon.craft.client.gui.GuiComponent;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.Nullable;

public class Button extends GuiComponent {
    private Callback<Button> callback = caller -> {};
    private @Nullable @IntRange(from = 0, to = 359) Color color = Color.LIGHT_GRAY;
    private boolean pressed;
    private String message;
    private Color textColor = Color.WHITE;

    /**
     * @param x       position create the widget
     * @param y       position create the widget
     * @param width   size create the widget
     */
    public Button(int x, int y, @IntRange(from = 21) int width,  String message) {
        this(x, y, width, 21, message);
    }

    /**
     * @param x       position create the widget
     * @param y       position create the widget
     * @param width   size create the widget
     */
    public Button(int x, int y, @IntRange(from = 21) int width, @IntRange(from = 21) int height, String message) {
        super(x, y, width, height);
        this.message = message;
    }

    /**
     * @param x        position create the widget
     * @param y        position create the widget
     * @param width    size create the widget
     */
    public Button(int x, int y, @IntRange(from = 21) int width,  String message, Callback<Button> callback) {
        this(x, y, width, 21, message, callback);
    }

    /**
     * @param x       position create the widget
     * @param y       position create the widget
     * @param width   size create the widget
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
        int u = this.isWithinBounds(mouseX, mouseY) ? 21 : 0;
        int v = this.isPressed() ? 21 : 0;

        renderer.setTextureColor(this.color == null ? Color.WHITE : this.color);
        renderer.blit(texture, x, y, 7, 7, u, v, 7, 7);
        renderer.blit(texture, x+7, y, this.width - 14, 7, 7 + u, v, 7, 7);
        renderer.blit(texture, x+ this.width -7, y, 7, 7, 14 + u, v, 7, 7);
        renderer.blit(texture, x, y+7, 7, this.height - 14, u, 7 + v, 7, 7);
        renderer.blit(texture, x+7, y+7, this.width - 14, this.height - 14, 7 + u, 7 + v, 7, 7);
        renderer.blit(texture, x+ this.width -7, y+7, 7, this.height - 14, 14 + u, 7 + v, 7, 7);
        renderer.blit(texture, x, y + this.height - 7, 7, 7, u, 14 + v, 7, 7);
        renderer.blit(texture, x+7, y + this.height - 7, this.width - 14, 7, 7 + u, 14 + v, 7, 7);
        renderer.blit(texture, x+ this.width -7, y + this.height - 7, 7, 7, 14 + u, 14 + v, 7, 7);
        renderer.setTextureColor(Color.rgb(0xffffff));

        renderer.drawCenteredText(this.message, x + this.width / 2, y + (this.height / 2 - this.font.lineHeight + (this.isPressed() ? 2 : 0)), this.textColor);
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        return !this.click();
    }

    public boolean click() {
        Callback<Button> callback = this.callback;
        if (callback == null) {
            return true;
        }
        callback.call(this);
        return false;
    }

    @Override
    public boolean mousePress(int x, int y, int button) {
        this.pressed = true;
        return super.mousePress(x, y, button);
    }

    @Override
    public boolean mouseRelease(int x, int y, int button) {
        this.pressed = false;
        return super.mouseRelease(x, y, button);
    }

    public boolean isPressed() {
        return this.pressed;
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
