package com.ultreon.craft.client.gui.widget;

import com.ultreon.craft.client.gui.GuiComponent;
import com.ultreon.craft.client.gui.Renderer;
import org.checkerframework.common.value.qual.IntRange;

public class Panel extends GuiComponent {
    public Panel(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        super(x, y, width, height);
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        renderer.fill(this.x, this.y, this.width, this.height, this.backgroundColor);
    }
}
