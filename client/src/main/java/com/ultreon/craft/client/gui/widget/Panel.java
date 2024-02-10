package com.ultreon.craft.client.gui.widget;

import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.components.ColorComponent;
import com.ultreon.craft.util.Color;
import org.checkerframework.common.value.qual.IntRange;

import java.util.function.Supplier;

import static com.ultreon.craft.client.UltracraftClient.id;

public class Panel extends Widget {
    private final ColorComponent backgroundColor;

    public Panel(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        super(width, height);

        this.backgroundColor = this.register(id("background_color"), new ColorComponent(Color.BLACK.withAlpha(0x80)));
        this.focusable = false;
    }

    public Panel() {
        this(0, 0, 0, 0);
    }

    public static Panel of(int x, int y, int width, int height) {
        return new Panel(x, y, width, height);
    }

    public static Panel create() {
        return new Panel();
    }

    public Panel backgroundColor(Color color) {
        this.backgroundColor.set(color);
        return this;
    }

    @Override
    public Panel position(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    @Override
    public Panel bounds(Supplier<Bounds> bounds) {
        this.onRevalidate(widget -> this.setBounds(bounds.get()));
        return this;
    }

    public ColorComponent backgroundColor() {
        return this.backgroundColor;
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        renderer.fill(this.pos.x, this.pos.y, this.size.width, this.size.height, this.backgroundColor.get());
    }

    @Override
    public String getName() {
        return "Panel";
    }
}
