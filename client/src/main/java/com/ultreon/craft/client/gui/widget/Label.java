package com.ultreon.craft.client.gui.widget;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.properties.AlignmentProperty;
import com.ultreon.craft.client.gui.widget.properties.BackgroundColorProperty;
import com.ultreon.craft.client.gui.widget.properties.TextColorProperty;
import com.ultreon.craft.client.gui.widget.properties.TextProperty;
import com.ultreon.craft.client.util.Color;
import com.ultreon.craft.text.TextObject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public class Label extends Widget<Label> implements BackgroundColorProperty, TextColorProperty, TextProperty<Label>, AlignmentProperty {
    private TextObject text = TextObject.EMPTY;
    private Alignment alignment;
    private Color textColor;
    private Color backgroundColor;
    private int scale = 1;

    public Label(int x, int y) {
        this(x, y, Alignment.LEFT, Color.WHITE);
    }

    public Label(int x, int y, Color textColor) {
        this(x, y, Alignment.LEFT, textColor);
    }

    public Label(int x, int y, Alignment alignment) {
        this(x, y, alignment, Color.WHITE);
    }

    public Label(int x, int y, Alignment alignment, Color textColor) {
        super(x, y, 0, 0);
        this.alignment = alignment;
        this.textColor = textColor;
    }

    @Override
    public TextObject getText() {
        return this.text;
    }

    @Override
    public Label text(TextObject text) {
        this.text = text;
        return this;
    }

    @Override
    public String getRawText() {
        return this.text.getText();
    }

    @Override
    public @NotNull Color getTextColor() {
        return this.textColor;
    }

    @Override
    public Label textColor(@NotNull Color textColor) {
        this.textColor = textColor;
        return this;
    }

    public @NotNull Alignment getAlignment() {
        return this.alignment;
    }

    public Label alignment(@NotNull Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public int getScale() {
        return this.scale;
    }

    public Label scale(int scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public void renderBackground(Renderer renderer, float deltaTime) {
        this.size.idt();

        switch (this.alignment) {
            case LEFT:
                renderer.drawTextScaledLeft(this.text, this.scale, this.pos.x, this.pos.y, Color.WHITE);
                break;

            case CENTER:
                renderer.drawTextScaledCenter(this.text, this.scale, this.pos.x, this.pos.y, Color.WHITE);
                break;

            case RIGHT:
                renderer.drawTextScaledRight(this.text, this.scale, this.pos.x, this.pos.y, Color.WHITE);
                break;
        }
    }

    @Override
    public String getName() {
        return "Label";
    }

    @Override
    public Label backgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    @Override
    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

}
