package com.ultreon.craft.client.gui.widget;

import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.properties.BackgroundColorProperty;
import com.ultreon.craft.util.Color;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;

public class Panel extends Widget<Panel> implements BackgroundColorProperty {
    private Color backgroundColor = Color.BLACK.withAlpha(0x80);

    public Panel(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        super(x, y, width, height);
    }

    public Panel() {
        super(0, 0, 0, 0);
    }

    @Override
    public @NotNull Color getBackgroundColor() {
        return this.backgroundColor;
    }

    @Override
    public Widget<?> backgroundColor(@NotNull Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    @Override
    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        renderer.fill(this.pos.x, this.pos.y, this.size.width, this.size.height, this.backgroundColor);
    }

    @Override
    public String getName() {
        return "Panel";
    }
}
