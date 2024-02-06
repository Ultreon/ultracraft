package com.ultreon.craft.client.gui.screens.tabs;

import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.Tab;
import com.ultreon.craft.client.gui.widget.UIContainer;
import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.text.TextObject;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class TabbedUI extends Screen {
    private int selected;
    private boolean bottomSelected;
    private List<Tab> tabs;
    @Nullable
    private Tab tab;
    private int tabX;
    private int contentX;
    private int contentY;
    private int contentWidth;
    private int contentHeight;
    private Supplier<Bounds> contentBounds;

    protected TabbedUI(String title) {
        super(title);
    }

    protected TabbedUI(TextObject title) {
        super(title);
    }

    protected TabbedUI(String title, TabbedUI parent) {
        super(title, parent);
    }

    protected TabbedUI(TextObject title, TabbedUI parent) {
        super(title, parent);
    }

    @Override
    public final void build(GuiBuilder builder) {
        TabbedUIBuilder tabbedUIBuilder = new TabbedUIBuilder(builder, this);
        this.build(tabbedUIBuilder);

        this.selected = 0;
        this.bottomSelected = false;

        this.contentBounds = tabbedUIBuilder.contentBounds;
        this.tabs = tabbedUIBuilder.tabs;
        this.tab = this.tabs.isEmpty() ? null : tabbedUIBuilder.tabs.get(0);

        for (Tab tab : this.tabs) {
            this.defineRoot(tab);
        }
    }

    @Override
    public void revalidate() {
        super.revalidate();

        Bounds contentBounds = this.contentBounds.get();
        this.contentX = contentBounds.getX();
        this.contentY = contentBounds.getY();
        this.contentWidth = contentBounds.getWidth();
        this.contentHeight = contentBounds.getHeight();

        for (Tab tab : this.tabs) {
            tab.revalidate();
            tab.content().revalidate();
            tab.content().x(this.contentX);
            tab.content().y(this.contentY);
            tab.content().width(this.contentWidth);
            tab.content().height(this.contentHeight);
        }
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {

        for (Tab tab : this.tabs) {
            if (!tab.bottom()) {
                tab.render(renderer, mouseX, mouseY, deltaTime);
            }
        }

        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        renderer.renderFrame(contentX - 4, contentY - 4, contentWidth + 8, contentHeight + 8);

        if (this.tab != null && renderer.pushScissors(contentX, contentY, contentWidth, contentHeight)) {
            TabContent content = this.tab.content();
            content.x(this.contentX);
            content.y(this.contentY);
            content.width(this.contentWidth);
            content.height(this.contentHeight);
            content.render(renderer, mouseX, mouseY, deltaTime);
            renderer.popScissors();
        }

        for (Tab tab : this.tabs) {
            if (tab.bottom()) {
                tab.render(renderer, mouseX, mouseY, deltaTime);
            }
        }
    }

    @Override
    public void renderChildren(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderChildren(renderer, mouseX, mouseY, deltaTime);
    }

    public final Tab getTab() {
        return this.tab;
    }

    public final int getSelected() {
        return this.selected;
    }

    public final boolean isBottomSelected() {
        return this.bottomSelected;
    }

    public final List<Tab> getTabs() {
        return Collections.unmodifiableList(this.tabs);
    }

    public final void setTabs(List<Tab> tabs) {
        this.tabs.clear();
        this.tabs.addAll(tabs);
    }

    public final void setSelected(int selected) {
        this.selected = selected;

        this.tab = this.tabs.get(selected);
        this.bottomSelected = this.tabs.get(selected).bottom();
    }

    public final void setSelected(TextObject name) {
        for (int i = 0; i < this.tabs.size(); i++) {
            if (this.tabs.get(i).name().equals(name)) {
                this.setSelected(i);
                break;
            }
        }
    }

    public final void setSelected(String name) {
        for (int i = 0; i < this.tabs.size(); i++) {
            if (this.tabs.get(i).name().getText().equals(name)) {
                this.setSelected(i);
                break;
            }
        }
    }

    public final void setSelected(Tab tab) {
        for (int i = 0; i < this.tabs.size(); i++) {
            if (this.tabs.get(i) == tab) {
                this.setSelected(i);
                break;
            }
        }
    }

    protected void setTabX(int tabX) {
        this.tabX = tabX;
    }

    public abstract void build(TabbedUIBuilder builder);

    public int getTabX() {
        return this.tabX;
    }

    public int getContentX() {
        return contentX;
    }

    public int getContentY() {
        return contentY;
    }

    public int getContentWidth() {
        return contentWidth;
    }

    public int getContentHeight() {
        return contentHeight;
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button, int clicks) {
        for (Tab tab : this.tabs) {
            if (tab.isWithinBounds(mouseX, mouseY) && tab.mouseClick(mouseX, mouseY, button, clicks)) return true;
            if (tab == this.tab && tab.content().isWithinBounds(mouseX, mouseY) && tab.content().mouseClick(mouseX, mouseY, button, clicks)) return true;
        }

        return super.mouseClick(mouseX, mouseY, button, clicks);
    }

    @Override
    public boolean mousePress(int mouseX, int mouseY, int button) {
        for (Tab tab : this.tabs) {
            if (tab.isWithinBounds(mouseX, mouseY) && tab.mousePress(mouseX, mouseY, button)) return true;
            if (tab == this.tab && tab.content().isWithinBounds(mouseX, mouseY) && tab.content().mousePress(mouseX, mouseY, button)) return true;
        }

        return super.mousePress(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseRelease(int mouseX, int mouseY, int button) {
        for (Tab tab : this.tabs) {
            if (tab.isWithinBounds(mouseX, mouseY) && tab.mouseRelease(mouseX, mouseY, button)) {
                this.selected = this.tabs.indexOf(tab);
                this.bottomSelected = tab.bottom();
                this.tab = tab;
                return true;
            }
            if (tab == this.tab && tab.content().isWithinBounds(mouseX, mouseY) && tab.content().mouseRelease(mouseX, mouseY, button)) return true;
        }

        return super.mouseRelease(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheel(int mouseX, int mouseY, double rotation) {
        for (Tab tab : this.tabs) {
            if (tab.isWithinBounds(mouseX, mouseY) && tab.mouseWheel(mouseX, mouseY, rotation)) return true;
            if (tab == this.tab && tab.content().isWithinBounds(mouseX, mouseY) && tab.content().mouseWheel(mouseX, mouseY, rotation)) return true;
        }

        return super.mouseWheel(mouseX, mouseY, rotation);
    }

    @Override
    public void mouseMove(int mouseX, int mouseY) {
        for (Tab tab : this.tabs) {
            if (tab.isWithinBounds(mouseX, mouseY))
                tab.mouseMove(mouseX, mouseY);
            if (tab == this.tab && tab.content().isWithinBounds(mouseX, mouseY))
                tab.content().mouseMove(mouseX, mouseY);
        }

        super.mouseMove(mouseX, mouseY);
    }

    @Override
    public boolean mouseDrag(int mouseX, int mouseY, int deltaX, int deltaY, int pointer) {
        for (Tab tab : this.tabs) {
            if (tab.isWithinBounds(mouseX, mouseY) && tab.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer))
                return true;
            if (tab == this.tab && tab.content().isWithinBounds(mouseX, mouseY) && tab.content().mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer)) return true;
        }

        return super.mouseDrag(mouseX, mouseY, deltaX, deltaY, pointer);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class TabbedUIBuilder extends GuiBuilder {
        private final GuiBuilder guiBuilder;
        private final List<Tab> tabs = new ArrayList<>();
        private Supplier<Bounds> contentBounds = () -> new Bounds(0, 0, screen().getWidth(), screen().getHeight());
        private final TabbedUI parent;

        public TabbedUIBuilder(GuiBuilder guiBuilder, TabbedUI parent) {
            super(guiBuilder.screen());

            this.guiBuilder = guiBuilder;
            this.parent = parent;
        }

        public Tab add(TextObject name, boolean bottom, int index, Consumer<TabBuilder> builder) {
            Tab tab = new Tab(name, parent, bottom, index, builder);
            this.tabs.add(tab);
            return tab;
        }

        public Tab add(TextObject name, boolean bottom, int index, UIContainer<?> container) {
            Tab tab = new Tab(name, parent, bottom, index, builder -> builder.add(container));
            this.tabs.add(tab);
            return tab;
        }

        @Deprecated
        public <T extends Widget> T addWithPos(T widget, Supplier<Position> pos) {
            T add = this.guiBuilder.screen().add(widget);
            add.onRevalidate(caller -> caller.setPos(pos.get()));
            return add;
        }

        @Deprecated
        public <T extends Widget> T addWithBounds(T widget, Supplier<Bounds> bounds) {
            T add = this.guiBuilder.screen().add(widget);
            add.onRevalidate(caller -> caller.setBounds(bounds.get()));
            return add;
        }

        public <T extends Widget> T add(T widget) {
            return this.guiBuilder.screen().add(widget);
        }

        public TabbedUIBuilder contentBounds(Supplier<Bounds> bounds) {
            this.contentBounds = bounds;
            return this;
        }

        public TabbedUI screen() {
            return this.parent;
        }
    }
}
