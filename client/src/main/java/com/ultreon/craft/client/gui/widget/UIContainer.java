package com.ultreon.craft.client.gui.widget;

import com.badlogic.gdx.utils.Array;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.layout.Layout;
import com.ultreon.craft.client.gui.widget.layout.StandardLayout;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UIContainer<T extends UIContainer<T>> extends Widget<T> {
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
    protected final Array<Widget<?>> widgets = new Array<>();

    private Layout layout = new StandardLayout();
    protected Widget<?> focused;

    public UIContainer(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        super(x, y, width, height);
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

    public Array<? extends Widget<?>> children() {
        return this.widgets;
    }

    public void renderChildren(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        for (var widget : this.widgets) {
            if (!widget.visible) continue;
            if (widget.ignoreBounds) {
                this.renderChild(renderer, mouseX, mouseY, deltaTime, widget);
                continue;
            }
            if (renderer.pushScissors(this.getBounds())) {
                this.renderChild(renderer, mouseX, mouseY, deltaTime, widget);
                renderer.popScissors();
            }
        }
    }

    public void renderChild(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime, Widget<?> widget) {
        widget.render(renderer, mouseX, mouseY, deltaTime);
    }

    public Array<? extends Widget<?>> getWidgets() {
        return this.widgets;
    }

    public Layout getLayout() {
        return this.layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public Widget<?> getExactWidgetAt(int x, int y) {
        for (int i = this.widgets.size - 1; i >= 0; i--) {
            var widget = this.widgets.get(i);
            if (!widget.visible) continue;
            if (widget.isWithinBounds(x, y)) {
                if (widget instanceof UIContainer<?> uiContainer) return uiContainer.getExactWidgetAt(x, y);
                return widget;
            }
        }
        return null;
    }

    public @NotNull List<Widget<?>> getWidgetsAt(int x, int y) {
        List<Widget<?>> output = new ArrayList<>();
        for (int i = this.widgets.size - 1; i >= 0; i--) {
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

    public <C extends Widget<C>> C add(C widget) {
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
            if (!widget.visible) continue;
            if (widget.ignoreBounds) {
                if (widget.mouseClick(mouseX, mouseY, button, clicks)) return true;
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY)) {
                if (widget.mouseClick(mouseX, mouseY, button, clicks)) return true;
            }
        }
        return super.mouseClick(mouseX, mouseY, button, clicks);
    }

    @Override
    public boolean mousePress(int mouseX, int mouseY, int button) {
        for (var widget : this.widgets) {
            if (!widget.visible) continue;
            if (widget.ignoreBounds) {
                if (widget.mousePress(mouseX, mouseY, button)) return true;
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY)) {
                if (widget.mousePress(mouseX, mouseY, button)) return true;
            }
        }
        return super.mousePress(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        for (var widget : this.widgets) {
            if (!widget.visible) continue;
            if (widget.ignoreBounds) {
                if (widget.mouseRelease(mouseX, mouseY, button)) return true;
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY)) {
                if (widget.mouseRelease(mouseX, mouseY, button)) return true;
            }
        }
        return super.mouseRelease(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        for (var widget : this.widgets) {
            if (!widget.visible) continue;
            if (widget.ignoreBounds) {
                if (widget.mouseWheel(mouseX, mouseY, rotation)) return true;
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY)) {
                if (widget.mouseWheel(mouseX, mouseY, rotation)) return true;
            }
        }
        return super.mouseWheel(mouseX, mouseY, rotation);
    }

    @Override
    public void mouseMove(int mouseX, int mouseY) {
        for (var widget : this.widgets) {
            if (!widget.visible) continue;
            if (widget.ignoreBounds) {
                widget.mouseMove(mouseX, mouseY);
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY)) {
                widget.mouseMove(mouseX, mouseY);
            }
        }
        super.mouseMove(mouseX, mouseY);
    }

    @Override
    public boolean mouseDrag(int mouseX, int mouseY, int deltaX, int deltaY, int button) {
        for (var widget : this.widgets) {
            if (!widget.visible) continue;
            if (widget.ignoreBounds) {
                if (widget.mouseDrag(mouseX, mouseY, deltaX, deltaY, button)) return true;
                continue;
            }
            if (widget.isWithinBounds(mouseX, mouseY)) {
                if (widget.mouseDrag(mouseX, mouseY, deltaX, deltaY, button)) return true;
            }
        }
        return super.mouseDrag(mouseX, mouseY, deltaX, deltaY, button);
    }

    @Override
    public boolean keyPress(int keyCode) {
        var widget = this.focused;

        if (widget != null) {
            if (widget.keyPress(keyCode)) return true;
        }

        return super.keyPress(keyCode);
    }

    @Override
    public boolean keyRelease(int keyCode) {
        var widget = this.focused;

        if (widget != null) {
            if (widget.keyRelease(keyCode)) return true;
        }

        return super.keyRelease(keyCode);
    }

    @Override
    public boolean charType(char character) {
        var widget = this.focused;

        if (widget != null) {
            if (widget.charType(character)) return true;
        }

        return super.charType(character);
    }
}
