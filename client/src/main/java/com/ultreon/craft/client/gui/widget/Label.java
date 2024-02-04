package com.ultreon.craft.client.gui.widget;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.components.AlignmentComponent;
import com.ultreon.craft.client.gui.widget.components.ColorComponent;
import com.ultreon.craft.client.gui.widget.components.ScaleComponent;
import com.ultreon.craft.client.gui.widget.components.TextComponent;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

import static com.ultreon.craft.client.UltracraftClient.id;

@ApiStatus.NonExtendable
public class Label extends Widget {
    private final AlignmentComponent alignment;
    private final ColorComponent textColor;
    private final TextComponent text;
    private final ScaleComponent scale;

    public Label() {
        this(Alignment.LEFT, Color.WHITE);
    }

    public Label(Color textColor) {
        this(Alignment.LEFT, textColor);
    }

    public Label(Alignment alignment) {
        this(alignment, Color.WHITE);
    }

    public Label(Alignment alignment, Color textColor) {
        super(0, 0);
        this.alignment = this.register(id("alignment"), new AlignmentComponent(alignment));
        this.textColor = this.register(id("text_color"), new ColorComponent(textColor));
        this.text = this.register(id("text"), new TextComponent(null));
        this.scale = this.register(id("scale"), new ScaleComponent(1));
    }

    public static Label of(TextObject text) {
        Label label = new Label();
        label.text.set(text);
        return label;
    }

    public static Label of(String text) {
        Label label = new Label();
        label.text.setRaw(text);
        return label;
    }

    public static Label of() {
        Label label = new Label();
        label.text.set(TextObject.empty());
        return label;
    }

    @Override
    public Label position(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    @Override
    public Label bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> this.setBounds(position.get()));
        return this;
    }

    public Label alignment(Alignment alignment) {
        this.alignment.set(alignment);
        return this;
    }

    public Label textColor(Color textColor) {
        this.textColor.set(textColor);
        return this;
    }

    public Label scale(int scale) {
        this.scale.set(scale);
        return this;
    }

    @Override
    public void renderBackground(Renderer renderer, float deltaTime) {
        this.size.idt();
        int scale = this.scale.get();
        var text = this.text.get();
        var textColor = this.textColor.get();

        if (text == null) return;

        switch (this.alignment.get()) {
            case LEFT -> renderer.textLeft(text, scale, this.pos.x, this.pos.y, textColor);
            case CENTER -> renderer.textCenter(text, scale, this.pos.x, this.pos.y, textColor);
            case RIGHT -> renderer.textRight(text, scale, this.pos.x, this.pos.y, textColor);
        }
    }

    @Override
    public String getName() {
        return "Label";
    }

    public ScaleComponent scale() {
        return this.scale;
    }

    public TextComponent text() {
        return this.text;
    }

    public AlignmentComponent alignment() {
        return this.alignment;
    }
}
