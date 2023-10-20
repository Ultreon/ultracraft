package com.ultreon.craft.render.gui;

import com.ultreon.craft.render.Renderer;

import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class GuiContainer extends GuiComponent implements IGuiContainer {
    protected final List<GuiComponent> children = new CopyOnWriteArrayList<>();
    protected final List<Widget> statics = new CopyOnWriteArrayList<>();
    protected GuiComponent hoveredWidget;
    protected GuiComponent pressingWidget;
    int innerXOffset;
    int innerYOffset;

    public GuiContainer(int x, int y, @IntRange(from = 0) int width, @IntRange(from = 0) int height) {
        super(x, y, width, height);
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        renderChildren(renderer, mouseX, mouseY, deltaTime);
    }

    public void renderChildren(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        for (GuiComponent child : this.children) {
            if (child.visible) {
                child.render(renderer, mouseX, mouseY, deltaTime);
            }
        }
    }

    @Nullable
    public GuiComponent getExactWidgetAt(int x, int y) {
        GuiComponent widgetAt = getWidgetAt(x, y);
        if (widgetAt instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) widgetAt;
            return container.getExactWidgetAt(x, y);
        }
        return widgetAt;
    }

    /**
     * Adds a {@link GuiComponent gui component } to the screen, including initializing it with {@link GuiStateListener#make()}.
     *
     * @param child the gui component to add.
     * @param <T>   the component's type.
     * @return the same as the parameter.
     */
    public <T extends GuiComponent> T add(T child) {
        this.children.add(child);
        child.make();
        return child;
    }

    public void remove(GuiComponent child) {
        this.children.remove(child);
    }

    @Nullable
    public GuiComponent getWidgetAt(int x, int y) {
        List<GuiComponent> guiComponents = this.children;
        for (int i = guiComponents.size() - 1; i >= 0; i--) {
            GuiComponent child = guiComponents.get(i);
            if (!child.enabled || !child.visible) continue;
            if (child.isWithinBounds(x, y)) return child;
        }
        return null;
    }

    @Override
    public boolean mouseClick(int x, int y, int button, int count) {
        GuiComponent widgetAt = getWidgetAt(x, y);
        return widgetAt != null && widgetAt.mouseClick(x - widgetAt.getX(), y - widgetAt.getY(), button, count);
    }

    @Override
    public boolean mousePress(int x, int y, int button) {
        GuiComponent widgetAt = getWidgetAt(x, y);
        x -= this.x + this.innerXOffset;
        y -= this.y + this.innerYOffset;
        pressingWidget = widgetAt;
        return widgetAt != null && widgetAt.mousePress(x - widgetAt.getX(), y - widgetAt.getY(), button);
    }

    @Override
    public boolean mouseRelease(int x, int y, int button) {
        GuiComponent widgetAt = pressingWidget;
        x -= this.x + this.innerXOffset;
        y -= this.y + this.innerYOffset;
        return widgetAt != null && widgetAt.mouseRelease(x - widgetAt.getX(), y - widgetAt.getY(), button);
    }

    @Override
    public void mouseMove(int x, int y) {
        GuiComponent widgetAt = this.getWidgetAt(x, y);
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
        GuiComponent widgetAt = this.getWidgetAt(x, y);
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
        GuiComponent widgetAt = getWidgetAt(x, y);
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
        GuiComponent widgetAt = this.getWidgetAt(x, y);
        System.out.println("x = " + x + ", y = " + y + ", rotation = " + rotation);
        x -= this.x + this.innerXOffset;
        y -= this.y + this.innerYOffset;
        System.out.println("widgetAt = " + widgetAt);
        if (widgetAt != null) return widgetAt.mouseWheel(x - widgetAt.getX(), y - widgetAt.getY(), rotation);
        return false;
    }

    protected final void clearWidgets() {
        for (GuiComponent widget : this.children) {
            widget.destroy();
        }
        this.children.clear();
    }

    public GuiComponent getHoveredWidget() {
        return hoveredWidget;
    }

    @Override
    public List<GuiComponent> children() {
        return this.children;
    }
}
