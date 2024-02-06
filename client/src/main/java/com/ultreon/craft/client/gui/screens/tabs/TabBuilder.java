package com.ultreon.craft.client.gui.screens.tabs;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.text.TextObject;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.function.Supplier;

@ApiStatus.Experimental
public class TabBuilder {
    private final TabContent tabContent;
    private final UltracraftClient client = UltracraftClient.get();

    public TabBuilder(TabContent tabContent) {
        this.tabContent = tabContent;
    }

    @Deprecated
    public <T extends Widget> T addWithPos(T widget, Supplier<Position> pos) {
        T add = this.tabContent.add(widget);
        add.onRevalidate(caller -> caller.setPos(pos.get()));
        return add;
    }

    @Deprecated
    public <T extends Widget> T addWithBounds(T widget, Supplier<Bounds> bounds) {
        T add = this.tabContent.add(widget);
        add.onRevalidate(caller -> caller.setBounds(bounds.get()));
        return add;
    }

    public <T extends Widget> T add(T widget) {
        return this.tabContent.add(widget);
    }

    public <T extends Widget> T add(TextObject label, T widget) {
        T add = this.tabContent.add(widget);
        add(Label.of(label)
                .alignment(Alignment.LEFT)
                .position(() -> new Position(content().getX(), widget.getY())));

        return add;
    }

    public TabContent tabContent() {
        return tabContent;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TabBuilder) obj;
        return Objects.equals(this.tabContent, that.tabContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tabContent);
    }

    @Override
    public String toString() {
        return "GuiBuilder[" +
                "tabContent=" + tabContent + ']';
    }

    /**
     * @deprecated Use {@link #content()} instead
     */
    @Deprecated
    public TabbedUI screen() {
        return tabContent.parent();
    }

    public TabContent content() {
        return tabContent;
    }

    public UltracraftClient client() {
        return this.client;
    }

    public TextObject title() {
        return tabContent.title();
    }
}
