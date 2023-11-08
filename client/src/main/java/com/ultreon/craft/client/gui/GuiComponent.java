package com.ultreon.craft.client.gui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.font.Font;
import com.ultreon.craft.client.gui.old.Widget;
import com.ultreon.craft.client.init.Fonts;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import org.checkerframework.common.value.qual.IntRange;

import java.nio.file.Path;

/**
 * Controllable widget, a widget that can be controlled by the user.
 * This widget contains input event handlers like {@link #keyPress(int)} and {@link #mouseClick(int, int, int, int)}
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
@Deprecated
@SuppressWarnings("unused")
public abstract class GuiComponent implements GuiStateListener, Widget {
    private static final Path ROOT = Path.of("/");
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    public boolean enabled = true;
    public boolean visible = true;
    public boolean focused = false;
    private boolean valid;
    protected Color backgroundColor = Color.TRANSPARENT;
    protected final UltracraftClient client = UltracraftClient.get();
    public final Font font = Fonts.DEFAULT;
    private boolean hovered = false;
    private int lastMouseX;
    private int lastMouseY;
    private final long creationTime = System.nanoTime();
    GuiComponent parent;

    /**
     * @param x      the x position of the widget
     * @param y      the y position of the widget
     * @param width  the width of the widget
     * @param height the height of the widget
     */
    @SuppressWarnings("ConstantValue")
    public GuiComponent(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        if (width < 0) throw new IllegalArgumentException("Width is negative");
        if (height < 0) throw new IllegalArgumentException("Height is negative");

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Handler for mouse clicking.<br>
     * Should only be overridden, and not called unless you know what you are doing.
     *
     * @param x      the x position when clicked.
     * @param y      the y position when clicked.
     * @param button the button used.
     * @param count  the number of sequential clicks.
     */
    public boolean mouseClick(int x, int y, int button, int count) {
        return false;
    }

    /**
     * Handler for mouse button press.<br>
     * Should only be overridden, and not called unless you know what you are doing.
     *
     * @param x      the x position when pressed.
     * @param y      the y position when pressed.
     * @param button the button used.
     */
    public boolean mousePress(int x, int y, int button) {
        return false;
    }

    /**
     * Handler for mouse button release.<br>
     * Should only be overridden, and not called unless you know what you are doing.
     *
     * @param x      the x position when released.
     * @param y      the y position when released.
     * @param button the button used.
     */
    public boolean mouseRelease(int x, int y, int button) {
        return false;
    }

    /**
     * Handler for mouse motion.<br>
     * Should only be overridden, and not called unless you know what you are doing.
     *
     * @param x the x position where the mouse moved to.
     * @param y the y position where the mouse moved to.
     */
    public void mouseMove(int x, int y) {
        this.lastMouseX = x;
        this.lastMouseY = y;
    }

    /**
     * Handler for mouse pressing.<br>
     * Should only be overridden, and not called unless you know what you are doing.
     *
     * @param x      the x position when pressed.
     * @param y      the y position when pressed.
     * @param nx     the x position dragged to.
     * @param ny     the y position dragged to.
     * @param button the button used.
     */
    public void mouseDrag(int x, int y, int nx, int ny, int button) {

    }

    /**
     * Called when the mouse exits the widget.
     */
    public void mouseExit() {
        this.hovered = false;
    }

    /**
     * Called when the mouse enters the widget.
     *
     * @param x x position where it entered.
     * @param y y position where it entered.
     */
    public void mouseEnter(int x, int y) {
        this.hovered = true;
    }

    public boolean mouseWheel(int x, int y, double rotation) {
        return false;
    }

    /**
     * Key press handler.
     *
     * @param keyCode the code for the key pressed.
     * @return to cancel out other usage of this method.
     */
    public boolean keyPress(int keyCode) {
        if (keyCode == Input.Keys.ESCAPE && this.client.screen != null && this.client.screen.canClose()) {
            this.client.showScreen(null);
            return true;
        }

        return false;
    }

    /**
     * Key release handler.
     *
     * @param keyCode the code for the key released.
     * @return to cancel out other usage of this method.
     */
    public boolean keyRelease(int keyCode) {
        return false;
    }

    /**
     * Key type handler.
     *
     * @param character the character typed.
     * @return to cancel out other usage of this method.
     */
    public boolean charType(char character) {
        return false;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        if (width < 0) {
            throw new IllegalArgumentException("Width should be positive.");
        }
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        if (this.width < 0) {
            throw new IllegalArgumentException("Height should be positive.");
        }
        this.height = height;
    }

    public Vec2i getPos() {
        return new Vec2i(this.x, this.y);
    }

    public void setPos(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    public void setPos(Vec2i pos) {
        this.setPos(pos.x, pos.y);
    }

    public Vec2i getSize() {
        return new Vec2i(this.width, this.height);
    }

    public void setSize(Vec2i size) {
        this.setSize(size.x, size.y);
    }

    public void setSize(int width, int height) {
        this.setWidth(width);
        this.setHeight(height);
    }

    public Rectangle getBounds() {
        return new Rectangle(this.x, this.y, this.width, this.height);
    }

    public void setBounds(int x, int y, int width, int height) {
        this.setPos(x, y);
        this.setSize(width, height);
    }

    public void setBounds(Rectangle bounds) {
        this.x = (int) bounds.x;
        this.y = (int) bounds.y;
        this.width = (int) bounds.width;
        this.height = (int) bounds.height;
    }

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = Color.argb(color);
    }

    public void setBackgroundColor(int red, int green, int blue) {
        this.backgroundColor = Color.rgb(red, green, blue);
    }

    public void setBackgroundColor(int red, int green, int blue, int alpha) {
        this.backgroundColor = Color.rgba(red, green, blue, alpha);
    }

    public void setBackgroundColor(float red, float green, float blue) {
        this.backgroundColor = Color.rgb(red, green, blue);
    }

    public void setBackgroundColor(float red, float green, float blue, float alpha) {
        this.backgroundColor = Color.rgba(red, green, blue, alpha);
    }

    public void setBackgroundColor(String hex) {
        this.backgroundColor = Color.hex(hex);
    }

    /**
     * Check if a position is withing the bounds create the widget
     *
     * @param x position to check for.
     * @param y position to check for.
     * @return true, if the x and y position given is withing, the bounds create the widget
     */
    public boolean isWithinBounds(int x, int y) {
        return x >= this.getX() && y >= this.getY() && x <= this.getX() + this.getWidth() && y <= this.getY() + this.getHeight();
    }

    /**
     * Check if a position is withing the bounds create the widget
     *
     * @param pos position to check for.
     * @return true, if the x and y position given is withing, the bounds create the widget
     */
    public boolean isWithinBounds(Vec2i pos) {
        return pos.getX() >= this.getX() && pos.getY() >= this.getY() && pos.getX() <= this.getX() + this.getWidth() && pos.getY() <= this.getY() + this.getHeight();
    }

    public void renderComponent(Renderer renderer) {

    }

    public void tick() {

    }

    @Override
    public void make() {
        this.valid = true;
    }

    @Override
    public void destroy() {
        this.valid = false;
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }

    protected boolean isWithinBounds(float x, float y) {
        return x >= this.x && y >= this.y && x <= this.x + this.width && y <= this.y + this.height;
    }

    protected final int getLastMouseX() {
        return this.lastMouseX;
    }

    protected final int getLastMouseY() {
        return this.lastMouseY;
    }


    public static void fill(Renderer renderer, int x, int y, int width, int height, int color) {
        renderer.setColor(color);
        renderer.rect(x, y, width, height);
    }

    public static void fill(Renderer renderer, int x, int y, int width, int height, Color color) {
        renderer.setColor(color);
        renderer.rect(x, y, width, height);
    }

    /**
     * Should only be overridden when the component is dynamically created.
     *
     * @return the path to the component
     */
    public Path path() {
        Path path = this.parent == null ? GuiComponent.ROOT : this.parent.path();
        return path.resolve("%s[%d]".formatted(this.getClass().getSimpleName(), this.creationTime));
    }
}
