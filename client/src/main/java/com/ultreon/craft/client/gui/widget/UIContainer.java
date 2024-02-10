package com.ultreon.craft.client.gui.widget;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.NavDirection;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.layout.Layout;
import com.ultreon.craft.client.gui.widget.layout.StandardLayout;
import com.ultreon.craft.util.MathHelper;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class UIContainer<T extends UIContainer<T>> extends Widget {
    @SuppressWarnings("rawtypes")
    public static final UIContainer<?> ROOT = new UIContainer(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE) {
        @Override
        public Path path() {
            return Path.of("/");
        }

        @Override
        public String getName() {
            return "<ROOT>";
        }
    };

    @ApiStatus.Internal
    protected final List<Widget> widgets = new CopyOnWriteArrayList<>();

    private Layout layout = new StandardLayout();
    public Widget focused;

    public UIContainer(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        super(width, height);
    }

    @Override
    public UIContainer<T> position(Supplier<Position> position) {
        this.onRevalidate(widget -> widget.setPos(position.get()));
        return this;
    }

    @Override
    public UIContainer<T> bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> widget.setBounds(position.get()));
        return this;
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        if (renderer.pushScissors(this.getBounds())) {
            this.renderChildren(renderer, mouseX, mouseY, deltaTime);
            renderer.popScissors();
        }
    }

    @Override
    public void revalidate() {
        super.revalidate();

        for (var widget : this.widgets) {
            widget.revalidate();
        }
    }

    public List<? extends Widget> children() {
        return this.widgets;
    }

    public void renderChildren(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        for (var widget : this.widgets) {
            if (!widget.visible) {
                if (widget.ignoreBounds)
                    this.renderChild(renderer, mouseX, mouseY, deltaTime, widget);
                continue;
            }
            if (renderer.pushScissors(this.getBounds())) {
                this.renderChild(renderer, mouseX, mouseY, deltaTime, widget);
                renderer.popScissors();
            }
        }
    }

    public void renderChild(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime, Widget widget) {
        widget.render(renderer, mouseX, mouseY, deltaTime);
    }

    public List<? extends Widget> getWidgets() {
        return this.widgets;
    }

    public Layout getLayout() {
        return this.layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public Widget getExactWidgetAt(int x, int y) {
        for (int i = this.widgets.size() - 1; i >= 0; i--) {
            var widget = this.widgets.get(i);
            if (!widget.visible) continue;
            if (widget.isWithinBounds(x, y)) {
                if (widget instanceof UIContainer<?> uiContainer) return uiContainer.getExactWidgetAt(x, y);
                return widget;
            }
        }
        return null;
    }

    public @NotNull List<Widget> getWidgetsAt(int x, int y) {
        List<Widget> output = new ArrayList<>();
        for (int i = this.widgets.size() - 1; i >= 0; i--) {
            var widget = this.widgets.get(i);

            if (!widget.visible) continue;
            if (widget.isWithinBounds(x, y)) {
                if (widget instanceof UIContainer<?> container)
                    output.addAll(container.getWidgetsAt(x, y));
                output.add(widget);
            }
        }

        output.removeIf(Objects::isNull);
        return output;
    }

    public @Nullable Widget getWidgetAt(int x, int y) {
        for (int i = this.widgets.size() - 1; i >= 0; i--) {
            var widget = this.widgets.get(i);

            if (!widget.visible) continue;
            if (widget.isWithinBounds(x, y)) {
                return widget;
            }
        }

        return null;
    }

    public <C extends Widget> C add(C widget) {
        widget.parent = this;
        widget.root = this.root;
        this.widgets.add(widget);
        return widget;
    }

    @Override
    public String getName() {
        return "UIContainer";
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
        for (var widget : this.widgets) {
            if (!widget.visible) {
                if (widget.ignoreBounds && widget.mouseClick(mouseX, mouseY, button, clicks)) return true;
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY) && widget.mouseClick(mouseX, mouseY, button, clicks)) return true;
        }
        return super.mouseClick(mouseX, mouseY, button, clicks);
    }

    @Override
    public boolean mousePress(int mouseX, int mouseY, int button) {
        for (var widget : this.widgets) {
            if (!widget.visible) {
                if (widget.ignoreBounds && widget.mousePress(mouseX, mouseY, button)) return true;
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY) && widget.mousePress(mouseX, mouseY, button)) return true;
        }
        return super.mousePress(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        for (var widget : this.widgets) {
            if (!widget.visible) {
                if (widget.ignoreBounds && widget.mouseRelease(mouseX, mouseY, button)) return true;
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY) && widget.mouseRelease(mouseX, mouseY, button)) return true;
        }
        return super.mouseRelease(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        for (var widget : this.widgets) {
            if (!widget.visible) {
                if (widget.ignoreBounds && widget.mouseWheel(mouseX, mouseY, rotation)) return true;
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY) && widget.mouseWheel(mouseX, mouseY, rotation)) return true;
        }
        return super.mouseWheel(mouseX, mouseY, rotation);
    }

    @Override
    public void mouseMove(int mouseX, int mouseY) {
        for (var widget : this.widgets) {
            if (!widget.visible) {
                if (widget.ignoreBounds) {
                    widget.mouseMove(mouseX, mouseY);
                }
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY)) {
                widget.mouseMove(mouseX, mouseY);
            }
        }
        super.mouseMove(mouseX, mouseY);
    }

    @Override
    public boolean mouseDrag(int mouseX, int mouseY, int deltaX, int deltaY, int pointer) {
        for (var widget : this.widgets) {
            if (!widget.visible) {
                if (widget.ignoreBounds && widget.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer)) return true;
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY) && widget.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer))
                return true;
        }
        return super.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer);
    }

    @Override
    public boolean keyPress(int keyCode) {
        var widget = this.focused;

        if (widget != null && keyCode == Input.Keys.ENTER) {
            widget.mousePress(widget.getX(), widget.getY(), 0);
            widget.mouseRelease(widget.getX(), widget.getY(), 0);
            widget.mouseClick(widget.getX(), widget.getY(), 0, 0);
        }
        if (widget != null && widget.keyPress(keyCode)) return true;

        return super.keyPress(keyCode);
    }

    @Override
    public boolean keyRelease(int keyCode) {
        var widget = this.focused;

        if (widget != null && widget.keyRelease(keyCode)) return true;

        return super.keyRelease(keyCode);
    }

    @Override
    public boolean charType(char character) {
        var widget = this.focused;

        if (widget != null && widget.charType(character)) return true;

        return super.charType(character);
    }

    protected <C extends Widget> C defineRoot(C widget) {
        if (widget.root == null) {
            widget.root = root;
        }
        return widget;
    }

    protected void focusUp() {
        if (children().isEmpty()) return;

        if (focused == null) {
            focused = this.widgets.getFirst();
        }
        int x = focused.getX() + focused.getWidth() / 2;
        int y = focused.getY() + focused.getHeight() / 2;

        findNextFocus(new Vector2(x, y), 270, NavDirection.UP);
    }

    protected void focusDown() {
        if (children().isEmpty()) return;

        if (focused == null) {
            focused = this.widgets.getFirst();
        }
        int x = focused.getX() + focused.getWidth() / 2;
        int y = focused.getY() + focused.getHeight() / 2;

        findNextFocus(new Vector2(x, y), 90, NavDirection.DOWN);
    }

    protected void focusLeft() {
        if (children().isEmpty()) return;

        if (focused == null) {
            focused = this.widgets.getFirst();
        }
        int x = focused.getX() + focused.getWidth() / 2;
        int y = focused.getY() + focused.getHeight() / 2;

        findNextFocus(new Vector2(x, y), 0, NavDirection.LEFT);
    }

    protected void focusRight() {
        if (children().isEmpty()) return;

        if (focused == null) {
            focused = this.widgets.getFirst();
        }
        int x = focused.getX() + focused.getWidth() / 2;
        int y = focused.getY() + focused.getHeight() / 2;

        findNextFocus(new Vector2(x, y), 180, NavDirection.RIGHT);
    }

    private void findNextFocus(Vector2 vec, float angle, NavDirection direction) {
        Vector2 cur = new Vector2();
        Vector2 closestVec = new Vector2();
        float closest = Float.MAX_VALUE;

        Widget toFocus = null;
        for (Widget child : children()) {
            if (!child.focusable) continue;
            if (child == focused) continue;
            cur.set(child.getX() + child.getWidth() / 2f, child.getY() + child.getHeight() / 2f);
            float v = (MathHelper.angleToVector(cur, vec) + 180) % 360;
            if (switch (direction) {
                case UP -> v > 45 && v <= 135;
                case RIGHT -> v > 135 && v <= 225;
                case DOWN -> v > 225 && v <= 315;
                case LEFT -> v > 315 || v <= 45;
            }) {
                v += 360;
                if (Math.abs(v - angle) < closest) {
                    if (cur.dst(vec) < closestVec.dst(vec) * 1.5f) {
                        closest = Math.abs(v - angle);
                        closestVec.set(cur);
                        toFocus = child;
                    }
                }
            }
        }

        if (toFocus != null) {
            changeFocus(toFocus);
        }
    }

    protected void changeFocus(Widget toFocus) {
        if (!toFocus.focusable) return;
        if (focused != null) focused.onFocusLost();
        focused = toFocus;
        toFocus.onFocusGained();
    }
}
