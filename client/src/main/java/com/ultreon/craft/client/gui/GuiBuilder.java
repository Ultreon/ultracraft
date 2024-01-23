package com.ultreon.craft.client.gui;

import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.Widget;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.function.Supplier;

@ApiStatus.Experimental
public class GuiBuilder {
    private final Screen screen;

    public GuiBuilder(Screen screen) {
        this.screen = screen;
    }

    @Deprecated
    public <T extends Widget> T addWithPos(T widget, Supplier<Position> pos) {
        T add = this.screen.add(widget);
        add.onRevalidate(caller -> caller.setPos(pos.get()));
        return add;
    }

    @Deprecated
    public <T extends Widget> T addWithBounds(T widget, Supplier<Bounds> bounds) {
        T add = this.screen.add(widget);
        add.onRevalidate(caller -> caller.setBounds(bounds.get()));
        return add;
    }

    public <T extends Widget> T add(T widget) {
        return this.screen.add(widget);
    }

    public Screen screen() {
        return screen;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GuiBuilder) obj;
        return Objects.equals(this.screen, that.screen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(screen);
    }

    @Override
    public String toString() {
        return "GuiBuilder[" +
                "screen=" + screen + ']';
    }

}
