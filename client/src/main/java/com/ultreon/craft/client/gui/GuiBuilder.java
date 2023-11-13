package com.ultreon.craft.client.gui;

import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.Widget;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.Experimental
public record GuiBuilder(Screen screen) {
    public <T extends Widget> T addWithPos(T widget, Supplier<Position> pos) {
        T add = this.screen.add(widget);
        add.onRevalidate(caller -> caller.setPos(pos.get()));
        return add;
    }

    public <T extends Widget> T addWithBounds(T widget, Supplier<Bounds> bounds) {
        T add = this.screen.add(widget);
        add.onRevalidate(caller -> caller.setBounds(bounds.get()));
        return add;
    }

    public <T extends Widget> T add(T widget) {
        return this.screen.add(widget);
    }
}
