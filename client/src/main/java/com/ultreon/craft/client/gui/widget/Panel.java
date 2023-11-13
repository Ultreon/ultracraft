package com.ultreon.craft.client.gui.widget;

import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.components.ColorComponent;
import com.ultreon.craft.util.Color;
import org.checkerframework.common.value.qual.IntRange;

import static com.ultreon.craft.client.UltracraftClient.id;

public class Panel extends Widget {
    private final ColorComponent backgroundColor;

    public Panel(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        super(width, height);

        this.backgroundColor = this.register(id("background_color"), new ColorComponent(Color.BLACK.withAlpha(0x80)));
    }

    public Panel() {
        this(0, 0, 0, 0);
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
