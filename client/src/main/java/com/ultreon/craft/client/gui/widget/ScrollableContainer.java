package com.ultreon.craft.client.gui.widget;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.client.gui.*;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.Mth;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ApiStatus.NonExtendable
public class ScrollableContainer extends UIContainer<ScrollableContainer> {
    private static final int SCROLLBAR_WIDTH = 5;
    private float scrollY = 0;
    private Widget selected;
    private boolean selectable;
    protected Widget hoveredWidget;
    int innerXOffset;
    int innerYOffset;

    protected int contentHeight;
    protected int contentWidth;
    private Color backgroundColor = Color.argb(0x40000000);

    public ScrollableContainer(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        super(width, height);
    }

    public ScrollableContainer(Position position, Size size) {
        this(position.x, position.y, size.width, size.height);
    }

    public ScrollableContainer() {
        super(400, 500);
    }

    public boolean isSelectable() {
        return this.selectable;
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        renderer.fill(this.pos.x, this.pos.y, this.size.width, this.size.height, backgroundColor);

        renderer.pushMatrix();
        if (renderer.pushScissors(this.getBounds())) {
            this.renderChildren(renderer, mouseX, mouseY, deltaTime);
            renderer.popScissors();
        }
        renderer.popMatrix();
    }

    @Override
    public String getName() {
        return "SelectionList";
    }

    @Nullable
    public Widget getWidgetAt(int x, int y) {
        if (!this.isWithinBounds(x, y)) return null;
        List<? extends Widget> entries = this.children();
        for (int i = entries.size() - 1; i >= 0; i--) {
            Widget widget = entries.get(i);
            if (!widget.enabled || !widget.visible) continue;
            if (widget.isWithinBounds(x, y)) return widget;
        }
        return null;
    }

    @Override
    public void mouseMove(int x, int y) {
        @Nullable Widget widgetAt = this.getWidgetAt(x, y);
        boolean widgetChanged = false;
        if (this.hoveredWidget != null && !this.hoveredWidget.isWithinBounds(x, y)) {
            this.hoveredWidget.mouseExit();
        }

        if (widgetAt != this.hoveredWidget) widgetChanged = true;
        this.hoveredWidget = widgetAt;

        if (this.hoveredWidget != null) {
            this.hoveredWidget.mouseMove(x - widgetAt.getX(), y - widgetAt.getY());

            if (widgetChanged) {
                this.hoveredWidget.mouseEnter(x - widgetAt.getX(), y - widgetAt.getY());
            }
        }
        super.mouseMove(x, y);
    }

    @Override
    public void mouseEnter(int x, int y) {
        @Nullable Widget widgetAt = this.getWidgetAt(x, y);
        boolean widgetChanged = false;
        if (this.hoveredWidget != null && !this.hoveredWidget.isWithinBounds(x, y)) {
            this.hoveredWidget.mouseExit();
        }

        if (widgetAt != this.hoveredWidget) widgetChanged = true;
        this.hoveredWidget = widgetAt;

        if (this.hoveredWidget != null) {
            x -= this.pos.x + this.innerXOffset;
            y -= this.pos.y + this.innerYOffset;
            if (widgetChanged) {
                this.hoveredWidget.mouseEnter(x - widgetAt.getX(), y - widgetAt.getY());
            }
        }
        super.mouseMove(x, y);
    }

    @Override
    public boolean mouseDrag(int x, int y, int dragX, int dragY, int pointer) {
        @Nullable Widget widgetAt = this.getWidgetAt(x, y);
        dragX -= this.pos.x + this.innerXOffset;
        dragY -= this.pos.y + this.innerYOffset;
        if (widgetAt != null)
            return widgetAt.mouseDrag(x, y, dragX, dragY, pointer);
        return super.mouseDrag(x, y, dragX, dragY, pointer);
    }

    @Override
    public void mouseExit() {
        if (this.hoveredWidget != null) {
            this.hoveredWidget.mouseExit();
            this.hoveredWidget = null;
        }
    }

    @Override
    public boolean mouseWheel(int x, int y, double rotation) {
        this.scrollY = this.getContentHeight() > this.size.height ? Mth.clamp((float) (this.scrollY + rotation * 10), 0, this.getContentHeight() - this.size.height) : 0;
        return true;
    }

    public int getContentHeight() {
        return this.contentHeight;
    }

    public void removeWidget(Widget value) {
        this.removeWidgetIf(Predicate.isEqual(value));
    }

    public void removeWidgetIf(Predicate<Widget> predicate) {
        Preconditions.checkNotNull(predicate, "predicate");

        int found = -1;
        int idx = 0;
        for (Widget widget : this.widgets) {
            if (predicate.test(widget)) {
                found = idx;
                break;
            }
            idx++;
        }

        if (found == -1) return;
        this.widgets.remove(found);
    }

    @CanIgnoreReturnValue
    public ScrollableContainer selectable(boolean selectable) {
        this.selectable = selectable;
        return this;
    }

    @Override
    public ScrollableContainer position(Supplier<Position> position) {
        this.onRevalidate(widget -> widget.setPos(position.get()));
        return this;
    }

    @Override
    public ScrollableContainer bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> widget.setBounds(position.get()));
        return this;
    }

    public ScrollableContainer backgroundColor(Color color) {
        this.backgroundColor = color;
        return this;
    }

    public Color backgroundColor() {
        return this.backgroundColor;
    }
}
