package com.ultreon.craft.client.gui.widget;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.font.Font;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.Size;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.components.UIComponent;
import com.ultreon.craft.component.GameComponent;
import com.ultreon.craft.component.GameComponentHolder;
import com.ultreon.craft.util.ElementID;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public abstract class Widget implements StaticWidget, GameComponentHolder<UIComponent> {
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
    private final List<RevalidateListener> revalidateListeners = new ArrayList<>();
    private final Map<ElementID, UIComponent> components = new HashMap<>();

    protected Widget(@IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        this.preferredSize.set(width, height);
        this.size.set(this.preferredSize);
    }

    public static boolean isPosWithin(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @CheckReturnValue
    protected final <C extends UIComponent> C register(ElementID id, C component) {
        this.components.put(id, component);
        return component;
    }

    @Override
    public Map<ElementID, UIComponent> componentRegistry() {
        return Collections.unmodifiableMap(this.components);
    }

    @Override
    public <T extends GameComponent<?>> T getComponent(ElementID id, T... typeGetter) {
        UIComponent component = this.components.get(id);
        if (component == null) throw new IllegalArgumentException("Component not found: " + id);
        if (!component.getClass().isAssignableFrom(typeGetter.getClass().getComponentType()))
            throw new ClassCastException(typeGetter.getClass().getComponentType().getName() + " does not extend " + component.getHolder() + ".");

        return (T) component;
    }

    @SafeVarargs
    public final <T extends GameComponent<?>> void withComponent(ElementID id, Consumer<T> consumer, T... typeGetter) {
        UIComponent uiComponent = this.getComponent(id);
        if (uiComponent == null) throw new IllegalArgumentException("Component not found: " + id);
    }

    @Override
    public final void render(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        if (!this.visible) return;


        if (this.isWithinBounds(mouseX, mouseY)) {
            this.root.directHovered = this;
            this.hovered = true;
        } else {
            this.hovered = false;
        }

        this.renderBackground(renderer, deltaTime);
        this.renderWidget(renderer, mouseX, mouseY, deltaTime);
    }

    public abstract Widget position(Supplier<Position> position);

    public abstract Widget bounds(Supplier<Bounds> position);

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

    public void setPreferredPos(int x, int y) {
        this.preferredPos.x = x;
        this.preferredPos.y = y;
    }

    public void setPreferredSize(int width, int height) {
        this.preferredSize.width = width;
        this.preferredSize.height = height;
    }

    public void setPreferredX(int x) {
        this.preferredPos.x = x;
    }

    public void setPreferredY(int y) {
        this.preferredPos.y = y;
    }

    public void setPreferredWidth(int width) {
        this.preferredSize.width = width;
    }

    public void setPreferredHeight(int height) {
        this.preferredSize.height = height;
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
    public void setPos(int x, int y) {
        this.pos.x = x;
        this.pos.y = y;
    }

    @CanIgnoreReturnValue
    public void setPos(Position pos) {
        this.pos.x = pos.x;
        this.pos.y = pos.y;
    }

    @CanIgnoreReturnValue
    public void setSize(int width, int height) {
        this.size.width = width;
        this.size.height = height;
    }

    @CanIgnoreReturnValue
    public void setSize(Size size) {
        this.size.width = size.width;
        this.size.height = size.height;
    }

    @CanIgnoreReturnValue
    public void x(int x) {
        this.pos.x = x;
    }

    @CanIgnoreReturnValue
    public void y(int y) {
        this.pos.y = y;
    }

    @CanIgnoreReturnValue
    public void width(int width) {
        this.size.width = width;
    }

    @CanIgnoreReturnValue
    public void height(int height) {
        this.size.height = height;
    }

    @CanIgnoreReturnValue
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void show() {
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
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

    public final boolean isWithinBounds(int x, int y) {
        return this.getBounds().contains(x, y) || this.ignoreBounds;
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
    public boolean mouseDrag(int mouseX, int mouseY, int deltaX, int deltaY, int pointer) {
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
            listener.revalidate(this);
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

    public void onRevalidate(RevalidateListener o) {
        this.revalidateListeners.add(o);
    }

    @CanIgnoreReturnValue
    public void setBounds(Bounds bounds) {
        this.pos.x = bounds.pos().x;
        this.pos.y = bounds.pos().y;
        this.size.width = bounds.size().width;
        this.size.height = bounds.size().height;
    }

    public void onFocusLost() {
        this.focused = false;
    }

    public void onFocusGained() {
        this.focused = true;
    }

    @FunctionalInterface
    public interface RevalidateListener {
        void revalidate(Widget widget);
    }
}
