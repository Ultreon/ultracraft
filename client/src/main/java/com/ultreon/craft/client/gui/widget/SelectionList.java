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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ApiStatus.NonExtendable
public class SelectionList<T> extends UIContainer<SelectionList<T>> {
    private static final int SCROLLBAR_WIDTH = 5;
    private final List<Entry<T>> entries = new ArrayList<>();
    private float scrollY = 0;
    private int itemHeight = 20;
    private Entry<T> selected;
    private boolean selectable;
    protected Entry<T> hoveredWidget;
    protected @Nullable Entry<T> pressingWidget;
    int innerXOffset;
    int innerYOffset;

    private ItemRenderer<T> itemRenderer = null;
    private Callback<T> onSelected = value -> {
    };
    private final int gap = 0;

    public SelectionList(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        super(width, height);

    }

    public SelectionList(Position position, Size size) {
        this(position.x, position.y, size.width, size.height);
    }

    public SelectionList() {
        super(400, 500);
    }

    public SelectionList(int itemHeight) {
        this();
        this.itemHeight(itemHeight);
    }

    public boolean isSelectable() {
        return this.selectable;
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        renderer.fill(this.pos.x, this.pos.y, this.size.width, this.size.height, Color.argb(0x40000000));

        renderer.pushMatrix();
        if (renderer.pushScissors(this.getBounds())) {
            this.renderChildren(renderer, mouseX, mouseY, deltaTime);
            renderer.popScissors();
        }
        renderer.popMatrix();
    }

    @Override
    public void renderChildren(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        for (Entry<T> entry : this.entries) {
            if (entry.visible) {
                entry.render(renderer, 0, mouseX, mouseY - (int) this.scrollY, this.selectable && this.selected == entry, deltaTime);
            }
        }
    }

    @Override
    public String getName() {
        return "SelectionList";
    }

    @Nullable
    public Entry<T> getEntryAt(int x, int y) {
        if (!this.isWithinBounds(x, y)) return null;
        List<Entry<T>> entries = this.entries;
        for (int i = entries.size() - 1; i >= 0; i--) {
            Entry<T> entry = entries.get(i);
            if (!entry.enabled || !entry.visible) continue;
            if (entry.isWithinBounds(x, y)) return entry;
        }
        return null;
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        if (this.selectable) {
            Entry<T> entryAt = this.getEntryAt(x, y);

            if (entryAt != null) {
                this.selected = entryAt;
                this.onSelected.call(this.selected.value);
                return true;
            }
        }
        @Nullable Entry<T> widgetAt = this.getEntryAt(x, y);
        return widgetAt != null && widgetAt.mouseClick(x - widgetAt.getX(), y - widgetAt.getY(), button, count);
    }

    @Override
    public boolean mousePress(int x, int y, int button) {
        @Nullable Entry<T> widgetAt = this.getEntryAt(x, y);
        this.pressingWidget = widgetAt;
        return widgetAt != null && widgetAt.mousePress(x - widgetAt.getX(), y - widgetAt.getY(), button);
    }

    @Override
    public boolean mouseRelease(int x, int y, int button) {
        @Nullable Entry<T> widgetAt = this.pressingWidget;
        return widgetAt != null && widgetAt.mouseRelease(x - widgetAt.getX(), y - widgetAt.getY(), button);
    }

    @Override
    public void mouseMove(int x, int y) {
        @Nullable Entry<T> widgetAt = this.getEntryAt(x, y);
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
        @Nullable Entry<T> widgetAt = this.getEntryAt(x, y);
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
    public boolean mouseDrag(int x, int y, int drawX, int dragY, int pointer) {
        @Nullable Entry<T> widgetAt = this.getEntryAt(x, y);
        x -= this.pos.x + this.innerXOffset;
        y -= this.pos.y + this.innerYOffset;
        drawX -= this.pos.x + this.innerXOffset;
        dragY -= this.pos.y + this.innerYOffset;
        if (widgetAt != null)
            return widgetAt.mouseDrag(x - widgetAt.getX(), y - widgetAt.getY(), drawX, dragY, pointer);
        return super.mouseDrag(x, y, drawX, dragY, pointer);
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
        return this.itemHeight * this.entries.size();
    }

    public int getItemHeight() {
        return this.itemHeight;
    }

    public T getSelected() {
        if (this.selected == null) return null;
        return this.selected.value;
    }

    public int getGap() {
        return this.gap;
    }

    @CanIgnoreReturnValue
    public Entry<T> removeEntry(Entry<T> entry) {
        this.entries.remove(entry);
        return entry;
    }

    public void removeEntry(T value) {
        this.removeEntryIf(Predicate.isEqual(value));
    }

    public void removeEntryIf(Predicate<T> predicate) {
        Preconditions.checkNotNull(predicate, "predicate");

        int found = -1;
        int idx = 0;
        for (Entry<T> entry : this.entries) {
            if (predicate.test(entry.value)) {
                found = idx;
                break;
            }
            idx++;
        }

        if (found == -1) return;
        this.entries.remove(found);
    }

    @Override
    public List<Entry<T>> children() {
        return this.entries;
    }

    @CanIgnoreReturnValue
    public SelectionList<T> itemRenderer(ItemRenderer<T> itemRenderer) {
        this.itemRenderer = itemRenderer;
        return this;
    }

    @CanIgnoreReturnValue
    public SelectionList<T> selectable(boolean selectable) {
        this.selectable = selectable;
        return this;
    }

    @CanIgnoreReturnValue
    public Entry<T> entry(T value) {
        Entry<T> entry = new Entry<>(value, this);
        this.entries.add(entry);
        return entry;
    }

    public SelectionList<T> entries(Collection<? extends T> values) {
        values.forEach(this::entry);
        return this;
    }

    public SelectionList<T> callback(Callback<T> onSelected) {
        this.onSelected = onSelected;
        return this;
    }

    public SelectionList<T> itemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
        return this;
    }

    @Override
    public SelectionList<T> position(Supplier<Position> position) {
        this.onRevalidate(widget -> widget.setPos(position.get()));
        return this;
    }

    @Override
    public SelectionList<T> bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> widget.setBounds(position.get()));
        return this;
    }

    public static class Entry<T> extends Widget {
        private final T value;
        private final SelectionList<T> list;

        public Entry(T value, SelectionList<T> list) {
            super(list.size.width, list.itemHeight);
            this.value = value;
            this.list = list;
        }

        public void render(Renderer renderer, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
            this.pos.x = this.list.pos.x;
            this.pos.y = (int) (this.list.pos.y - this.list.scrollY + (this.list.itemHeight + this.list.gap) * this.list.entries.indexOf(this));
            this.size.width = this.list.size.width - SelectionList.SCROLLBAR_WIDTH;
            this.size.height = this.list.itemHeight;
            ItemRenderer<T> itemRenderer = this.list.itemRenderer;
            if (itemRenderer != null && renderer.pushScissors(this.pos.x, this.pos.y, this.size.width, this.size.height)) {
                if (selected)
                    renderer.box(this.pos.x, this.pos.y, this.size.width - 2, this.size.height - 2, Color.rgb(0xffffff));

                itemRenderer.render(renderer, this.value, this.pos.y, mouseX, mouseY, selected, deltaTime);
                renderer.popScissors();
            }
        }

        public T getValue() {
            return this.value;
        }

        @Override
        public Entry<T> position(Supplier<Position> position) {
            return this;
        }

        @Override
        public Entry<T> bounds(Supplier<Bounds> position) {
            return this;
        }

        @Override
        public String getName() {
            return "SelectionListEntry";
        }

        public void select() {
            select(false);
        }

        public void select(boolean emitEvent) {
            this.list.selected = this;

            if (this.list.onSelected != null && emitEvent) {
                this.list.onSelected.call(this.value);
            }
        }
    }

    @FunctionalInterface
    public interface ItemRenderer<T> {
        void render(Renderer renderer, T value, int y, int mouseX, int mouseY, boolean selected, float deltaTime);
    }
}
