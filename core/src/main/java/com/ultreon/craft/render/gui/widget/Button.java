package com.ultreon.craft.render.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.Callback;
import com.ultreon.craft.render.gui.GuiComponent;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.Nullable;

public class Button extends GuiComponent {
    private Callback<Button> callback = caller -> {};
    private @Nullable @IntRange(from = 0, to = 359) Color color = Color.white;
    private static final Int2ReferenceMap<TextureRegion> REGION_CACHE = new Int2ReferenceArrayMap<>();
    private boolean pressed;
    private String message;
    private GlyphLayout layout = new GlyphLayout();
    private Color textColor = Color.white;

    /**
     * @param x       position create the widget
     * @param y       position create the widget
     * @param width   size create the widget
     * @param message
     */
    public Button(int x, int y, @IntRange(from = 21) int width,  String message) {
        this(x, y, width, 21, message);
    }

    /**
     * @param x       position create the widget
     * @param y       position create the widget
     * @param width   size create the widget
     * @param message
     */
    public Button(int x, int y, @IntRange(from = 21) int width, @IntRange(from = 21) int height, String message) {
        super(x, y, width, height);
        this.message = message;

        updateLayout();
    }

    /**
     * @param x        position create the widget
     * @param y        position create the widget
     * @param width    size create the widget
     * @param message
     * @param callback
     */
    public Button(int x, int y, @IntRange(from = 21) int width,  String message, Callback<Button> callback) {
        this(x, y, width, 21, message, callback);
    }

    /**
     * @param x       position create the widget
     * @param y       position create the widget
     * @param width   size create the widget
     * @param message
     */
    public Button(int x, int y, @IntRange(from = 21) int width, @IntRange(from = 21) int height, String message, Callback<Button> callback) {
        super(x, y, width, height);
        this.callback = callback;
        this.message = message;

        updateLayout();
    }

    private void updateLayout() {
        this.layout.setText(font, message);
    }

    @Nullable
    @IntRange(from = 0, to = 359)
    public Color getColor() {
        return color;
    }

    public void setColor(@Nullable @IntRange(from = 0, to = 359) Color color) {
        this.color = color;
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        Texture texture = game.getTextureManager().getTexture(UltreonCraft.id("textures/gui/widgets.png"));

        var x = this.x;
        var y = this.y;
        var u = isHovered() ? 21 : 0;
        var v = isPressed() ? 21 : 0;

        renderer.setTextureColor(color == null ? Color.white : this.color);
        renderer.texture(texture, x, y+height-7, 7, 7, u, v, 7, 7);
        renderer.texture(texture, x+7, y+height-7, width - 14, 7, 7 + u, v, 7, 7);
        renderer.texture(texture, x+width-7, y+height-7, 7, 7, 14 + u, v, 7, 7);
        renderer.texture(texture, x, y+7, 7, height - 14, u, 7 + v, 7, 7);
        renderer.texture(texture, x+7, y+7, width - 14, height - 14, 7 + u, 7 + v, 7, 7);
        renderer.texture(texture, x+width-7, y+7, 7, height - 14, 14 + u, 7 + v, 7, 7);
        renderer.texture(texture, x, y, 7, 7, u, 14 + v, 7, 7);
        renderer.texture(texture, x+7, y, width - 14, 7, 7 + u, 14 + v, 7, 7);
        renderer.texture(texture, x+width-7, y, 7, 7, 14 + u, 14 + v, 7, 7);
        renderer.setTextureColor(Color.rgb(0xffffff));
        renderer.setColor(textColor.darker().darker());
        renderer.text(message, x + width / 2 - (int)layout.width / 2, y + (height / 2 + font.getLineHeight() - 1 - (isPressed() ? 2 : 0)));
        renderer.setColor(textColor);
        renderer.text(message, x + width / 2 - (int)layout.width / 2, y + (height / 2 + font.getLineHeight() - (isPressed() ? 2 : 0)));
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        if (click()) return false;
        return true;
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
        pressed = true;
        return super.mousePress(x, y, button);
    }

    @Override
    public boolean mouseRelease(int x, int y, int button) {
        pressed = false;
        return super.mouseRelease(x, y, button);
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setMessage(String message) {
        this.message = message;
        this.layout.setText(this.font, this.message);
    }

    public String getMessage() {
        return message;
    }
}
