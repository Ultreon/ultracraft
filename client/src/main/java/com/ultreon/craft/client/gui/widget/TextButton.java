package com.ultreon.craft.client.gui.widget;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.Callback;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.components.ColorComponent;
import com.ultreon.craft.client.gui.widget.components.TextComponent;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import org.checkerframework.common.value.qual.IntRange;

import java.util.function.Supplier;

import static com.ultreon.craft.client.UltracraftClient.id;

public class TextButton extends Button<TextButton> {
    private final TextComponent text;
    private final ColorComponent textColor;

    protected TextButton() {
        this(200);
    }

    /**
     * @param width the width of the button
     */
    protected TextButton(@IntRange(from = 21) int width) {
        this(width, 21);
    }

    /**
     * @param width  the width of the button
     * @param height the height of the button
     */
    protected TextButton(@IntRange(from = 21) int width, @IntRange(from = 21) int height) {
        super(width, height);

        this.text = this.register(id("text"), new TextComponent(TextObject.empty()));
        this.textColor = this.register(id("text_color"), new ColorComponent(Color.WHITE));
    }

    public static TextButton of(TextObject text) {
        return TextButton.of(text, 200);
    }

    public static TextButton of(TextObject text, int width) {
        return TextButton.of(text, width, 21);
    }

    public static TextButton of(TextObject text, int width, int height) {
        TextButton button = new TextButton(width, height);
        button.text.set(text);
        return button;
    }

    public static TextButton of(String text) {
        return TextButton.of(text, 200);
    }

    public static TextButton of(String text, int width) {
        return TextButton.of(text, width, 21);
    }

    public static TextButton of(String text, int width, int height) {
        TextButton button = new TextButton(width, height);
        button.text.setRaw(text);
        return button;
    }

    @Override
    public TextButton position(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    @Override
    public TextButton bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> this.setBounds(position.get()));
        return this;
    }

    @Override
    public TextButton callback(Callback<TextButton> callback) {
        this.callback.set(callback);
        return this;
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        Texture texture = this.client.getTextureManager().getTexture(id("textures/gui/widgets.png"));

        int x = this.pos.x;
        int y = this.pos.y;

        this.renderButton(renderer, mouseX, mouseY, texture, x, y);

        renderer.textCenter(this.text.get(), x + this.size.width / 2, y + (this.size.height / 2 - this.font.lineHeight + (this.isPressed() ? 2 : 0)), this.enabled ? this.textColor.get() : this.textColor.get().withAlpha(0x80));
    }

    @Override
    public String getName() {
        return "TextButton";
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    public TextComponent text() {
        return this.text;
    }

    public ColorComponent textColor() {
        return this.textColor;
    }

    public TextButton translation(String path, Object... args) {
        this.text.translate(path, args);
        return this;
    }
}
