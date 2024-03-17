package com.ultreon.craft.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import org.jetbrains.annotations.ApiStatus;

import java.awt.*;

/**
 * Represents the game window using LWJGL3.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class GameWindow {
    private final Lwjgl3Window window;
    private final long handle;

    /**
     * Initializes the game window by getting the window handle from LibGDX.
     */
    public GameWindow() {
        Lwjgl3Graphics graphics = (Lwjgl3Graphics) (Gdx.graphics);
        this.window = graphics.getWindow();
        this.handle = window.getWindowHandle();
    }

    /**
     * Get the handle of the game window.
     *
     * @return The handle of the window.
     */
    public long getHandle() {
        return handle;
    }

    /**
     * Get the LWJGL3 window object.
     *
     * @return The LWJGL3 window object.
     */
    public Lwjgl3Window getWindow() {
        return window;
    }

    /**
     * Closes the game window.
     */
    public void close() {
        window.closeWindow();
    }

    /**
     * Sets the visibility of the game window.
     *
     * @param visible True to make the window visible, false to hide it.
     */
    public void setVisible(boolean visible) {
        window.setVisible(visible);
    }

    /**
     * Requests attention to the game window by flashing it.
     */
    public void requestAttention() {
        window.flash();
    }

    @ApiStatus.Experimental
    public boolean isHovered() {
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        Point location = pointerInfo.getLocation();
        int x = location.x;
        int y = location.y;

        return x >= 0 && x < Gdx.graphics.getWidth() && y >= 0 && y < Gdx.graphics.getHeight();
    }
}
