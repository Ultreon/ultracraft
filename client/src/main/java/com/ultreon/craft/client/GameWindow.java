package com.ultreon.craft.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import org.jetbrains.annotations.ApiStatus;

import java.awt.*;

public class GameWindow {
    private final Lwjgl3Window window;
    private final long handle;

    public GameWindow() {
        Lwjgl3Graphics graphics = (Lwjgl3Graphics) (Gdx.graphics);
        this.window = graphics.getWindow();
        this.handle = window.getWindowHandle();
    }

    public long getHandle() {
        return handle;
    }

    public Lwjgl3Window getWindow() {
        return window;
    }

    public void close() {
        window.closeWindow();
    }

    public void setVisible(boolean visible) {
        window.setVisible(visible);
    }

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
