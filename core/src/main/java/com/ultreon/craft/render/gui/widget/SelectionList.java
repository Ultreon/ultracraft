package com.ultreon.craft.render.gui.widget;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.GuiComponent;
import com.ultreon.craft.render.gui.IGuiContainer;
import com.ultreon.libs.commons.v0.Mth;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SelectionList<T> extends GuiComponent implements IGuiContainer {
    private static final int SCROLLBAR_WIDTH = 5;
    private final List<Entry> entries = new ArrayList<>();
    private float scrollY = 0;
    private final int itemHeight;
    private Entry selected;
    private boolean selectable;
    protected GuiComponent hoveredWidget;
    protected GuiComponent pressingWidget;
    int innerXOffset;
    int innerYOffset;

    private ItemRenderer<T> itemRenderer = null;
    private Consumer<T> onSelected = value -> {};
    private final int gap = 0;

    public SelectionList(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height, @IntRange(from = 0) int itemHeight) {
        super(x, y, width, height);

        this.itemHeight = itemHeight;
    }

    public void setItemRenderer(ItemRenderer<T> itemRenderer) {
        this.itemRenderer = itemRenderer;
    }

    public boolean isSelectable() {
        return this.selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    @Override
    public void renderComponent(Renderer renderer) {
        renderer.fill(this.x, this.y, this.width, this.height, Color.argb(0x40000000));
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        this.renderComponent(renderer);


        renderer.pushMatrix();
        renderer.translate(0, this.scrollY);
        if (renderer.pushScissors(this.x, this.y, this.width, this.height)) {
            this.renderChildren(renderer, mouseX, mouseY, deltaTime);
            renderer.popScissors();
        }
        renderer.popMatrix();
    }

    @Override
    public void renderChildren(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        for (Entry entry : this.entries) {
            if (entry.visible) {
                entry.render(renderer, 0, mouseX, mouseY - (int) this.scrollY, this.selectable && this.selected == entry, deltaTime);
            }
        }
    }

    @Nullable
    public Entry getEntryAt(int x, int y) {
        y += this.y;
        if (!this.isWithinBounds(x, y)) return null;
        y -= (int) this.scrollY;
        List<Entry> entries = this.entries;
        for (int i = entries.size() - 1; i >= 0; i--) {
            Entry entry = entries.get(i);
            if (!entry.enabled || !entry.visible) continue;
            if (entry.isWithinBounds(x, y)) return entry;
        }
        return null;
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        if (this.selectable) {
            Entry entryAt = this.getEntryAt(x, y);

            if (entryAt != null) {
                this.selected = entryAt;
                this.onSelected.accept(this.selected.value);
                return true;
            }
        }
        GuiComponent widgetAt = this.getEntryAt(x, y);
        return widgetAt != null && widgetAt.mouseClick(x - widgetAt.getX(), y - widgetAt.getY(), button, count);
    }

    @Override
    public boolean mousePress(int x, int y, int button) {
        GuiComponent widgetAt = this.getEntryAt(x, y);
        x -= this.x + this.innerXOffset;
        y -= this.y + this.innerYOffset;
        this.pressingWidget = widgetAt;
        return widgetAt != null && widgetAt.mousePress(x - widgetAt.getX(), y - widgetAt.getY(), button);
    }

    @Override
    public boolean mouseRelease(int x, int y, int button) {
        GuiComponent widgetAt = this.pressingWidget;
        x -= this.x + this.innerXOffset;
        y -= this.y + this.innerYOffset;
        return widgetAt != null && widgetAt.mouseRelease(x - widgetAt.getX(), y - widgetAt.getY(), button);
    }

    @Override
    public void mouseMove(int x, int y) {
        GuiComponent widgetAt = this.getEntryAt(x, y);
        boolean widgetChanged = false;
        if (this.hoveredWidget != null && !this.hoveredWidget.isWithinBounds(x, y)) {
            this.hoveredWidget.mouseExit();
        }

        if (widgetAt != this.hoveredWidget) widgetChanged = true;
        this.hoveredWidget = widgetAt;

        if (this.hoveredWidget != null) {
            x -= this.x + this.innerXOffset;
            y -= this.y + this.innerYOffset;
            this.hoveredWidget.mouseMove(x - widgetAt.getX(), y - widgetAt.getY());

            if (widgetChanged) {
                this.hoveredWidget.mouseEnter(x - widgetAt.getX(), y - widgetAt.getY());
            }
        }
        super.mouseMove(x, y);
    }

    @Override
    public void mouseEnter(int x, int y) {
        GuiComponent widgetAt = this.getEntryAt(x, y);
        boolean widgetChanged = false;
        if (this.hoveredWidget != null && !this.hoveredWidget.isWithinBounds(x, y)) {
            this.hoveredWidget.mouseExit();
        }

        if (widgetAt != this.hoveredWidget) widgetChanged = true;
        this.hoveredWidget = widgetAt;

        if (this.hoveredWidget != null) {
            x -= this.x + this.innerXOffset;
            y -= this.y + this.innerYOffset;
            if (widgetChanged) {
                this.hoveredWidget.mouseEnter(x - widgetAt.getX(), y - widgetAt.getY());
            }
        }
        super.mouseMove(x, y);
    }

    @Override
    public void mouseDrag(int x, int y, int nx, int ny, int button) {
        GuiComponent widgetAt = this.getEntryAt(x, y);
        x -= this.x + this.innerXOffset;
        y -= this.y + this.innerYOffset;
        nx -= this.x + this.innerXOffset;
        ny -= this.y + this.innerYOffset;
        if (widgetAt != null) widgetAt.mouseDrag(x - widgetAt.getX(), y - widgetAt.getY(), nx, ny, button);
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
        this.scrollY = this.getContentHeight() > this.height ? Mth.clamp((float) (this.scrollY + rotation * 10), 0, this.getContentHeight() - this.height) : 0;

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

    @CanIgnoreReturnValue
    public Entry addEntry(T value) {
        Entry entry = new Entry(value);
        this.entries.add(entry);
        return entry;
    }

    @CanIgnoreReturnValue
    public Entry removeEntry(Entry entry) {
        this.entries.remove(entry);
        return entry;
    }

    public void removeEntry(T value) {
        this.entries.removeIf(entry -> entry.value == value);
    }

    public void removeEntryIf(Predicate<T> predicate) {
        this.entries.removeIf(entry -> predicate.test(entry.value));
    }

    @Override
    public List<Entry> children() {
        return this.entries;
    }

    public void addEntries(Collection<? extends T> values) {
        values.forEach(this::addEntry);
    }

    public void setOnSelected(Consumer<T> onSelected) {
        this.onSelected = onSelected;
    }

    public class Entry extends GuiComponent {
        private final T value;
        private final SelectionList<T> list;

        public Entry(T value) {
            super(SelectionList.this.x, SelectionList.this.y, SelectionList.this.width, SelectionList.this.itemHeight);
            this.value = value;
            this.list = SelectionList.this;
        }

        @Override
        public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {

        }

        public void render(Renderer renderer, int y, int mouseX, int mouseY, boolean selected, float deltaTime) {
            this.x = this.list.x;
            this.y = this.list.y + (int) (-this.list.scrollY + (this.list.itemHeight + this.list.gap) * this.list.entries.indexOf(this));
            this.width = this.list.width - SCROLLBAR_WIDTH;
            this.height = this.list.itemHeight;
            ItemRenderer<T> itemRenderer = SelectionList.this.itemRenderer;
            if (itemRenderer != null) {
                if (renderer.pushScissors(this.x, this.y, this.width, this.height)) {
                    if (selected) {
                        renderer.box(this.x, this.y, this.width - 2, this.height - 2, Color.rgb(0xffffff));
                    }
                    itemRenderer.render(renderer, this.value, this.y, mouseX, mouseY, selected, deltaTime);
                    renderer.popScissors();
                }
            }
        }

        public T getValue() {
            return this.value;
        }
    }

    @FunctionalInterface
    public interface ItemRenderer<T> {
        void render(Renderer renderer, T value, int y, int mouseX, int mouseY, boolean selected, float deltaTime);
    }
}
