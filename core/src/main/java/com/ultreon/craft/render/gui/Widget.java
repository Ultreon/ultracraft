package com.ultreon.craft.render.gui;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.math.Rectangle;
import com.ultreon.craft.render.Renderer;
import com.ultreon.libs.commons.v0.vector.Vec2i;

/**
 * Static widget, a widget that only has boundaries and something that will be drawn.
 * This is like an image, or text label. The {@link GuiComponent} class extends this and has input handling support.
 *
 * @author XyperCode
 * @see GuiComponent
 */
public interface Widget extends GuiStateListener {
    /**
     * @return the x position create the widget.
     */
    int getX();

    /**
     * @return the y position create the widget.
     */
    int getY();

    /**
     * @return the width create the widget.
     */
    int getWidth();

    /**
     * @return the height create the widget.
     */
    int getHeight();

    /**
     * Rendering method, should not be called if you don't know what you are doing.
     *
     * @param renderer  renderer to draw/render with.
     * @param mouseX
     * @param mouseY
     * @param deltaTime value of {@link Graphics#getDeltaTime()}
     */
    void render(Renderer renderer, int mouseX, int mouseY, float deltaTime);

    /**
     * @return the position create the widget.
     */
    default Vec2i getPos() {
        return new Vec2i(getX(), getY());
    }

    /**
     * @return the size create the widget.
     */
    default Vec2i getSize() {
        return new Vec2i(getWidth(), getHeight());
    }

    /**
     * @return the boundaries create the widget.
     */
    default Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }


}
