package com.ultreon.craft.client.gui.widget;

import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.screens.tabs.TabBuilder;
import com.ultreon.craft.client.gui.screens.tabs.TabContent;
import com.ultreon.craft.client.gui.screens.tabs.TabbedUI;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.ElementID;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Tab extends Button<Tab> {
    private static final ElementID TEXTURE = new ElementID("textures/gui/tabs.png");
    private final TabbedUI parent;
    private final boolean bottom;
    private final int index;
    private final Consumer<TabBuilder> builder;
    public boolean enabled = true;
    public boolean visible = true;
    boolean selected = false;
    private ElementID icon;
    private TabContent content;
    private TextObject title;

    public Tab(TextObject title, TabbedUI parent, boolean bottom, int index, Consumer<TabBuilder> builder) {
        super(21, 18);
        this.title = title;
        this.parent = parent;
        this.bottom = bottom;
        this.index = index;
        this.builder = builder;

        this.content = new TabContent(this.parent, 0, 0, 0, 0, title);
        this.content.bounds(() -> new Bounds(this.parent.getContentX(), this.parent.getContentY(), this.parent.getContentWidth(), this.parent.getContentHeight()));
        ((UIContainer<?>) parent).defineRoot(this.content);
        this.build();
    }

    @Override
    public boolean mouseRelease(int x, int y, int button) {
        super.mouseRelease(x, y, button);
        return true;
    }

    @Override
    public Tab position(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    void onSelected(TabbedUI parent) {
        if (parent != this.parent) return;
        this.selected = true;
    }

    @Override
    public void revalidate() {
        this.x(this.parent.getTabX() + this.parent.getContentX() + 1 + this.index * 22);
        this.y(this.parent.getContentY() - 22 + (this.bottom ? (this.parent.getHeight() - 18) : 0));

        if (this.content != null) {
            this.content.revalidate();
        }

        super.revalidate();
    }

    private void build() {
        TabBuilder tabBuilder = new TabBuilder(this.content);
        this.builder.accept(tabBuilder);
    }

    @Override
    public Tab bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> this.setBounds(position.get()));
        return this;
    }

    public TextObject name() {
        return title;
    }

    public Screen screen() {
        return parent;
    }

    public boolean bottom() {
        return bottom;
    }

    public int index() {
        return index;
    }

    public boolean selected() {
        return selected;
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        renderer.blit(TEXTURE, this.getX(), this.getY() - 3, 21, 21, (hovered ? 21 : 0), (bottom ? 42 : 0) + (selected ? 21 : 0), 21, 21, 63, 84);

        if (this.icon != null) {
            renderer.blit(this.icon.mapPath(s -> "textures/" + s + ".png"), this.getX() + 3, this.getY() + 3, 16, 16, 0, 0, 16, 16, 16, 16);
        }
    }

    public Tab icon(ElementID icon) {
        this.icon = icon;
        return this;
    }

    public TabContent content() {
        return content;
    }

    public Tab title(TextObject title) {
        this.title = title;
        this.content.title(title);
        return this;
    }

    public TextObject title() {
        return title;
    }
}
