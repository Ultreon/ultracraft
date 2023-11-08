package com.ultreon.craft.client.gui.widget;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.font.Font;
import com.ultreon.craft.client.gui.*;
import com.ultreon.craft.client.gui.screens.Screen;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public abstract class Widget<T extends Widget<T>> implements StaticWidget {
    protected boolean ignoreBounds = false;
    private final Position preferredPos = new Position(0, 0);
    protected final Position pos = new Position(0, 0);
    private final Size preferredSize = new Size(0, 0);
    protected final Size size = new Size(0, 0);
    public boolean visible = true;
    public boolean enabled = true;
    public boolean hovered = false;
    public boolean focused = false;

    @ApiStatus.Internal
    protected Screen root;

    UIContainer<?> parent = UIContainer.ROOT;
    protected final long createTime = System.nanoTime();
    protected final UltracraftClient client = UltracraftClient.get();
    protected Font font = this.client.font;
    private final List<Callback<T>> revalidateListeners = new ArrayList<>();

    @SafeVarargs
    public Widget(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height, T... typeGetter) {
        this.preferredPos.x = x;
        this.preferredPos.y = y;
        this.preferredSize.width = width;
        this.preferredSize.height = height;
        this.size.set(this.preferredSize);
    }

    @Override
    public final void render(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        if (!this.visible) return;

        if (this.root.directHovered != null && this.isWithinBounds(mouseX, mouseY))
            this.root.directHovered = this;

        this.renderBackground(renderer, deltaTime);
        this.renderWidget(renderer, mouseX, mouseY, deltaTime);
    }

    public void renderWidget(Renderer renderer, int mouseX, int mouseY, float deltaTime) {

    }

    protected void renderBackground(Renderer renderer, float deltaTime) {

    }

    public int getPreferredX() {
        return this.preferredPos.x;
    }

    public int getPreferredY() {
        return this.preferredPos.y;
    }

    public int getPreferredWidth() {
        return this.preferredSize.width;
    }

    public int getPreferredHeight() {
        return this.preferredSize.height;
    }

    public T setPreferredPos(int x, int y) {
        this.preferredPos.x = x;
        this.preferredPos.y = y;
        return (T) this;
    }

    public T setPreferredSize(int width, int height) {
        this.preferredSize.width = width;
        this.preferredSize.height = height;
        return (T) this;
    }

    public T setPreferredX(int x) {
        this.preferredPos.x = x;
        return (T) this;
    }

    public T setPreferredY(int y) {
        this.preferredPos.y = y;
        return (T) this;
    }

    public T setPreferredWidth(int width) {
        this.preferredSize.width = width;
        return (T) this;
    }

    public T setPreferredHeight(int height) {
        this.preferredSize.height = height;
        return (T) this;
    }

    public int getX() {
        return this.pos.x;
    }

    public int getY() {
        return this.pos.y;
    }

    public int getWidth() {
        return this.size.width;
    }

    public int getHeight() {
        return this.size.height;
    }

    @CanIgnoreReturnValue
    public T pos(int x, int y) {
        this.pos.x = x;
        this.pos.y = y;
        return (T) this;
    }

    @CanIgnoreReturnValue
    public T pos(Position pos) {
        this.pos.x = pos.x;
        this.pos.y = pos.y;
        return (T) this;
    }

    @CanIgnoreReturnValue
    public T size(int width, int height) {
        this.size.width = width;
        this.size.height = height;
        return (T) this;
    }

    @CanIgnoreReturnValue
    public T size(Size size) {
        this.size.width = size.width;
        this.size.height = size.height;
        return (T) this;
    }

    @CanIgnoreReturnValue
    public T x(int x) {
        this.pos.x = x;
        return (T) this;
    }

    @CanIgnoreReturnValue
    public T y(int y) {
        this.pos.y = y;
        return (T) this;
    }

    @CanIgnoreReturnValue
    public T width(int width) {
        this.size.width = width;
        return (T) this;
    }

    @CanIgnoreReturnValue
    public T height(int height) {
        this.size.height = height;
        return (T) this;
    }

    @CanIgnoreReturnValue
    public T visible(boolean visible) {
        this.visible = visible;
        return (T) this;
    }

    public T enabled(boolean enabled) {
        this.enabled = enabled;
        return (T) this;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isHovered() {
        return this.hovered;
    }

    public boolean isFocused() {
        return this.focused;
    }

    /**
     * @return path to the widget.
     */
    public Path path() {
        return this.parent.path().resolve("%s[%d]".formatted(this.getName(), this.createTime));
    }

    public Bounds getBounds() {
        return new Bounds(this.pos, this.size);
    }

    public Position getPreferredPos() {
        return this.preferredPos;
    }

    public Size getPreferredSize() {
        return this.preferredSize;
    }

    protected boolean isWithinBounds(int x, int y) {
        return this.getBounds().contains(x, y);
    }

    @CanIgnoreReturnValue
    public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
        return false;
    }

    @CanIgnoreReturnValue
    public boolean mousePress(int mouseX, int mouseY, int button) {
        return false;
    }

    @CanIgnoreReturnValue
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        return false;
    }

    @CanIgnoreReturnValue
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        return false;
    }

    @CanIgnoreReturnValue
    public void mouseMove(int mouseX, int mouseY) {

    }

    @CanIgnoreReturnValue
    public boolean mouseDrag(int mouseX, int mouseY, int deltaX, int deltaY, int button) {
        return false;
    }

    @CanIgnoreReturnValue
    public boolean keyPress(int keyCode) {
        return this.focused;
    }

    @CanIgnoreReturnValue
    public boolean keyRelease(int keyCode) {
        return this.focused;
    }

    @CanIgnoreReturnValue
    public boolean charType(char character) {
        return this.focused;
    }

    public void revalidate() {
        for (var listener : this.revalidateListeners) {
            listener.call((T) this);
        }
    }

    protected void tick() {

    }

    protected void mouseExit() {

    }

    protected void mouseEnter(int x, int y) {

    }

    public String getName() {
        return "Widget";
    }

    /**
     * Check if the widget would show a click cursor.
     *
     * @return true if the widget will show a click cursor
     */
    public boolean isClickable() {
        return false;
    }

    public T onRevalidate(Callback<T> o) {
        this.revalidateListeners.add(o);
        return (T) this;
    }

    @CanIgnoreReturnValue
    public T bounds(Bounds bounds) {
        this.pos.x = bounds.pos().x;
        this.pos.y = bounds.pos().y;
        this.size.width = bounds.size().width;
        this.size.height = bounds.size().height;
        return (T) this;
    }

    public void onFocusLost() {
        this.focused = false;
    }

    public void onFocusGained() {
        this.focused = true;
    }
}
