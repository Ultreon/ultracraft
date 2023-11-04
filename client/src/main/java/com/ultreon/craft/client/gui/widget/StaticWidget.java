package com.ultreon.craft.client.gui.widget;

import com.ultreon.craft.client.gui.Renderer;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;

/**
 * Static widget, only for rendering.
 *
 * @author XyperCode
 * @since 0.1.0
 */
public interface StaticWidget {
    /**
     * Renders this widget.
     *
     * @param renderer  The renderer
     * @param mouseX    The x position of the mouse
     * @param mouseY    The y position of the mouse
     * @param deltaTime The delta time of the rendering
     */
    void render(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime);
}
